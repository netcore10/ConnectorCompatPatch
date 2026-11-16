package net.netco.connectorcompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarcontents.JarResource;
import net.neoforged.fml.jarcontents.JarResourceVisitor;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JarContentsPackResources answers every listResources/getNamespaces call by walking all entries of the mod's jar(s). Under
 * Connector, many mods are composite packs over 20-50 filtered jars (Create and its addons each include the whole Create Fly
 * jar), and a world load lists resources hundreds of times (every registry folder, tag type, recipes, ...), so most of
 * "Preparing for world creation" was spent re-reading zip entries.
 * <p>
 * Walk each jar's contents once and answer later listings from a sorted index. Mod jars don't change while the game runs,
 * and the walk goes through the same JarContents#visitContent, so filtering and de-duplication are unchanged.
 */
@Mixin(JarContentsPackResources.class)
public abstract class JarContentsPackResourcesIndexMixin {
    // ponytail: every mod's non-class file handles stay in memory for the session and are never rebuilt (fine for jars;
    // a dev setup editing mod folders live would need a /reload-time clear)
    private static final Map<JarContents, NavigableMap<String, List<JarResource>>> connectorCompat$indexes = new ConcurrentHashMap<>();

    @WrapOperation(
        method = {"listResources", "getNamespaces"},
        at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/jarcontents/JarContents;visitContent(Ljava/lang/String;Lnet/neoforged/fml/jarcontents/JarResourceVisitor;)V")
    )
    private void connectorCompat$visitIndexed(JarContents contents, String prefix, JarResourceVisitor visitor, Operation<Void> original) {
        NavigableMap<String, List<JarResource>> index = connectorCompat$indexes.computeIfAbsent(contents, c -> {
            // Composite packs can yield the same path more than once (an addon overriding a file); keep every copy, in
            // order, so the listing replays exactly what visitContent produced
            NavigableMap<String, List<JarResource>> all = new TreeMap<>();
            c.visitContent("", (path, resource) -> {
                if (!path.endsWith(".class")) {
                    all.computeIfAbsent(path, k -> new ArrayList<>(1)).add(resource.retain());
                }
            });
            return all;
        });
        index.subMap(prefix, true, prefix + Character.MAX_VALUE, false)
            .forEach((path, copies) -> copies.forEach(resource -> visitor.visit(path, resource)));
    }
}
