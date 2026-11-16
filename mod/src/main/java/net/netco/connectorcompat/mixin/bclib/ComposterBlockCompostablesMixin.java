package net.netco.connectorcompat.mixin.bclib;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import org.betterx.bclib.trait.Compostables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BCLib makes its own compostables work by wrapping the composter's COMPOSTABLES map lookups (ComposterBlockMixin,
 * ComposterInputContainerMixin). NeoForge replaced those lookups with the compostables data map, all behind
 * ComposterBlock#getValue, so BCLib's hooks have nothing to attach to. Apply BCLib's fallback there instead: an item the
 * data map doesn't know is still compostable if BCLib says so, with BCLib's chance.
 */
@Mixin(ComposterBlock.class)
public abstract class ComposterBlockCompostablesMixin {
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private static void connectorCompat$bclibCompostables(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValueF() < 0 && Compostables.isCompostable(stack.getItem())) {
            cir.setReturnValue(Compostables.chanceFor(stack.getItem()));
        }
    }
}
