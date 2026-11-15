package net.netco.connectorcompat.mixin.create.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.content.trains.TrainHUD;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Create Fly's HudMixin#renderMainHud. Vanilla picks the contextual bar inside extractHotbarAndDecorations(graphics,
 * deltaTracker); NeoForge moved that into a no-arg updateContextualBarRenderer() called from extractRenderState(graphics,
 * deltaTracker). Same behavior: draw the train HUD, and hide the contextual bar (XP/locator) while it is shown.
 */
@Mixin(Hud.class)
public abstract class HudTrainOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private GuiGraphicsExtractor connectorCompat$graphics;
    @Unique
    private DeltaTracker connectorCompat$deltaTracker;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void connectorCompat$captureFrame(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.connectorCompat$graphics = graphics;
        this.connectorCompat$deltaTracker = deltaTracker;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"))
    private void connectorCompat$releaseFrame(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.connectorCompat$graphics = null;
        this.connectorCompat$deltaTracker = null;
    }

    @WrapOperation(
        method = "updateContextualBarRenderer()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;nextContextualInfoState()Lnet/minecraft/client/gui/Hud$ContextualInfo;")
    )
    private Hud.ContextualInfo connectorCompat$trainHud(Hud instance, Operation<Hud.ContextualInfo> original) {
        if (this.connectorCompat$graphics != null
            && TrainHUD.renderOverlay(this.minecraft, this.connectorCompat$graphics, this.connectorCompat$deltaTracker)) {
            return Hud.ContextualInfo.EMPTY;
        }
        return original.call(instance);
    }
}
