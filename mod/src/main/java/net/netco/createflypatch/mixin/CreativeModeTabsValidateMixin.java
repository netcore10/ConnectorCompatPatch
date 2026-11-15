package net.netco.createflypatch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * Vanilla's CreativeModeTabs#validate (run at Bootstrap) requires every registered tab to have a unique row/column.
 * Fabric mods register their tabs before that check (e.g. Steam 'n' Rails: "Duplicate position: itemGroup.railways vs.
 * itemGroup.railways_tracks"); on Fabric, Fabric API's tab support handles this. NeoForge lays out modded tabs on its own
 * pages (CreativeModeTabRegistry) and ignores their row/column, so only vanilla tabs need to be checked.
 */
@Mixin(CreativeModeTabs.class)
public abstract class CreativeModeTabsValidateMixin {
    @WrapOperation(method = "validate", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object createFlyPatch$onlyCheckVanillaTabs(
        Map<Object, Object> positions, Object position, Object name, Operation<Object> original,
        @Local ResourceKey<CreativeModeTab> tabKey
    ) {
        if (!"minecraft".equals(tabKey.identifier().getNamespace())) {
            return null;
        }
        return original.call(positions, position, name);
    }
}
