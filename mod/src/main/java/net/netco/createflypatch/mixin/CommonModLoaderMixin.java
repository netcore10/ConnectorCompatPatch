package net.netco.createflypatch.mixin;

import net.neoforged.neoforge.internal.CommonModLoader;
import net.neoforged.neoforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * On Fabric, registries stay open for the whole of mod initialization, so a Fabric mod's content can register whenever its
 * classes first initialize. Under Connector, Launchpad only opens them later (around the Fabric entrypoints, from
 * Minecraft's constructor). If a NeoForge mod's constructor reaches Fabric content first, that content registers into frozen
 * registries and crashes, e.g. Vanillin -> BlockEntityType#isValid -> Steam 'n' Rails' mixin -> Create's AllBlockEntityTypes
 * ("Registry is already frozen").
 * <p>
 * Open the registries for NeoForge's mod-construction phase too. NeoForge's own "Registry initialization" step, right after
 * construction, unfreezes again (no-op) and freezes everything at its normal point.
 */
@Mixin(CommonModLoader.class)
public abstract class CommonModLoaderMixin {
    @Inject(method = "begin", at = @At("HEAD"))
    private static void createFlyPatch$openRegistriesDuringConstruction(Runnable periodicTask, boolean datagen, CallbackInfo ci) {
        GameData.unfreezeData();
    }
}
