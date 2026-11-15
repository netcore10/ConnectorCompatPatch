package net.netco.createflypatch.mixin.client;

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
    private GuiGraphicsExtractor createFlyPatch$graphics;
    @Unique
    private DeltaTracker createFlyPatch$deltaTracker;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void createFlyPatch$captureFrame(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.createFlyPatch$graphics = graphics;
        this.createFlyPatch$deltaTracker = deltaTracker;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"))
    private void createFlyPatch$releaseFrame(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.createFlyPatch$graphics = null;
        this.createFlyPatch$deltaTracker = null;
    }

    @WrapOperation(
        method = "updateContextualBarRenderer()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;nextContextualInfoState()Lnet/minecraft/client/gui/Hud$ContextualInfo;")
    )
    private Hud.ContextualInfo createFlyPatch$trainHud(Hud instance, Operation<Hud.ContextualInfo> original) {
        if (this.createFlyPatch$graphics != null
            && TrainHUD.renderOverlay(this.minecraft, this.createFlyPatch$graphics, this.createFlyPatch$deltaTracker)) {
            return Hud.ContextualInfo.EMPTY;
        }
        return original.call(instance);
    }
}
