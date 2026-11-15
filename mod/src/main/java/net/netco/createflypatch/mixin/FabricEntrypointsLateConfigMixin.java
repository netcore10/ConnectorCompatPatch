package net.netco.createflypatch.mixin;

import net.neoforged.neoforge.server.loading.ServerModLoader;
import net.netco.createflypatch.LateConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dedicated server: Launchpad runs the Fabric entrypoints inside ServerModLoader#load; afterwards, load configs they
 * registered too late for NeoForge (see LateConfigs). The client side is FabricEntrypointsLateConfigClientMixin.
 */
@Mixin(ServerModLoader.class)
public abstract class FabricEntrypointsLateConfigMixin {
    @Inject(method = "load", at = @At("RETURN"))
    private static void createFlyPatch$loadLateConfigs(CallbackInfo ci) {
        LateConfigs.load();
    }
}
