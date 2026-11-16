package net.netco.connectorcompat.mixin.automobility;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Automobility's part/vehicle item renderer (frames, wheels, engines, attachments held, dropped or framed) treats the last
 * argument of SpecialModelRenderer#submit as the model tint. In 26.2 that argument is the outline color, normally 0, so
 * the parts draw black. Not a Connector issue (same on Fabric, unfixed up to the 26.3 releases); draw them untinted.
 */
@Mixin(targets = "io.github.foundationgames.automobility.automobile.render.AutomobileItemSpecialRenderer")
public abstract class AutomobileItemSpecialRendererMixin {
    @ModifyVariable(
        method = "submit(Lio/github/foundationgames/automobility/automobile/render/AutomobileItemSpecialRenderer$Argument;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V",
        at = @At("HEAD"), argsOnly = true, ordinal = 2
    )
    private int connectorCompat$untinted(int outlineColor) {
        return -1;
    }
}
