package net.netco.connectorcompat.mixin.client;

import net.minecraft.client.Minecraft;
import net.netco.connectorcompat.LateConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client: Launchpad runs the Fabric entrypoints inside Minecraft's constructor; afterwards, load late configs (see LateConfigs). */
@Mixin(Minecraft.class)
public abstract class FabricEntrypointsLateConfigClientMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void connectorCompat$loadLateConfigs(CallbackInfo ci) {
        LateConfigs.load();
    }
}
