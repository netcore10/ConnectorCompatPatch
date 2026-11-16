package net.netco.connectorcompat;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Plugin for the mod-specific mixin configs: their mixins call that mod's code, so they apply only when the mod is installed.
 * The mod id is the config's package name, as NeoForge sees it (Connector turns '-' in Fabric ids into '_', so
 * mixin.wover_events is the Fabric mod wover-events).
 */
public class RequiredModMixinPlugin implements IMixinConfigPlugin {
    private boolean modPresent;

    @Override
    public void onLoad(String mixinPackage) {
        String modId = mixinPackage.substring(mixinPackage.lastIndexOf('.') + 1);
        modPresent = FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return modPresent;
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
