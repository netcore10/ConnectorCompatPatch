package net.netco.connectorcompat.mixin.create.client;

import com.zurrtum.create.client.flywheel.backend.gl.GlStateTracker;
import com.zurrtum.create.client.flywheel.backend.gl.buffer.GlBufferType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Untracked buffer target (null from FlywheelGlBufferTypeMixin): nothing to record, as in Crankshaft's Flywheel. */
@Mixin(GlStateTracker.class)
public abstract class FlywheelGlStateTrackerMixin {
    @Inject(method = "_setBuffer", at = @At("HEAD"), cancellable = true)
    private static void connectorCompat$skipUntrackedTarget(GlBufferType type, int buffer, CallbackInfo ci) {
        if (type == null) {
            ci.cancel();
        }
    }
}
