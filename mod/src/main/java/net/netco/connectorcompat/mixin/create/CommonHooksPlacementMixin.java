package net.netco.connectorcompat.mixin.create;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces Create Fly's ItemStackMixin#cacheState + #useOnBlock. On Fabric both inject into ItemStack#useOn and share the
 * BlockPlaceContext; on NeoForge, Adapter moved them into two different CommonHooks methods, so the share never arrived and
 * super glue / symmetry wand placement silently did nothing. Both halves now live in NeoForge's onPlaceItemIntoWorld.
 */
@Mixin(CommonHooks.class)
public abstract class CommonHooksPlacementMixin {
    @Inject(
        method = "onPlaceItemIntoWorld(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;")
    )
    private static void connectorCompat$cachePlacement(
        UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Share("place") LocalRef<BlockPlaceContext> place
    ) {
        if (context.getItemInHand().getItem() instanceof BlockItem && !context.getLevel().isClientSide()) {
            place.set(new BlockPlaceContext(context));
        }
    }

    @Inject(
        method = "onPlaceItemIntoWorld(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At("RETURN")
    )
    private static void connectorCompat$afterPlacement(
        UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Share("place") LocalRef<BlockPlaceContext> place
    ) {
        BlockPlaceContext placementContext = place.get();
        Player player = context.getPlayer();
        // Create ran this only for a successful item interaction; NeoForge turns a cancelled placement into FAIL
        if (placementContext == null || player == null || !(cir.getReturnValue() instanceof InteractionResult.Success)) {
            return;
        }
        ServerLevel world = (ServerLevel) context.getLevel();
        BlockPos pos = placementContext.getClickedPos();
        SuperGlueHandler.glueListensForBlockPlacement(world, player, pos);
        if (!context.getItemInHand().has(DataComponents.CONSUMABLE)) {
            SymmetryHandler.onBlockPlaced(world, player, pos, placementContext);
        }
    }
}
