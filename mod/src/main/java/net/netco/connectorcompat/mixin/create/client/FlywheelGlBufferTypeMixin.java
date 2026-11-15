package net.netco.connectorcompat.mixin.create.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.zurrtum.create.client.flywheel.backend.gl.buffer.GlBufferType;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Create Fly bundles its own Flywheel, whose GlStateManager#_glBindBuffer hook throws for any buffer target it doesn't
 * know. Other mods bind such targets too: Crankshaft's Distant Horizons renderer binds GL_PARAMETER_BUFFER (0x80EE) and
 * crashes the game. Crankshaft's own Flywheel skips untracked targets; do the same here (see FlywheelGlStateTrackerMixin).
 */
@Mixin(GlBufferType.class)
public abstract class FlywheelGlBufferTypeMixin {
    @WrapMethod(method = "fromTarget")
    private static GlBufferType connectorCompat$unknownTargetIsUntracked(int target, Operation<GlBufferType> original) {
        try {
            return original.call(target);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
