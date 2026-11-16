package net.netco.connectorcompat.mixin.automobility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Other players' cars jump between positions (and their engine sound warbles) on a server. The 26.2 port still smooths
 * remote cars through an old {@code lerpTo} override, but 26.2 delivers positions through
 * {@code Entity#moveOrInterpolateTo}, which just teleports entities that have no {@link InterpolationHandler}. The port's
 * 26.3 branch fixed this with an interpolation handler; give the car one here, like vanilla boats: only on clients that
 * don't drive it, spread over the car's update interval + 1 ticks (what its old lerp used). Drivers and servers unchanged.
 */
@Mixin(targets = "io.github.foundationgames.automobility.entity.AutomobileEntity")
public abstract class AutomobileEntityInterpolationMixin extends Entity {
    @Unique
    private InterpolationHandler connectorCompat$interpolation;

    private AutomobileEntityInterpolationMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public InterpolationHandler getInterpolation() {
        if (!this.level().isClientSide() || this.isLocalInstanceAuthoritative()) return null;
        if (connectorCompat$interpolation == null) {
            connectorCompat$interpolation = new InterpolationHandler(this, this.getType().updateInterval() + 1);
        }
        return connectorCompat$interpolation;
    }

    @Inject(method = "positionTrackingTick", at = @At("HEAD"))
    private void connectorCompat$interpolate(CallbackInfo ci) {
        if (connectorCompat$interpolation == null) return;
        if (this.isLocalInstanceAuthoritative()) {
            connectorCompat$interpolation.cancel(); // this client started driving it
        } else {
            connectorCompat$interpolation.interpolate();
        }
    }
}
