package net.netco.connectorcompat.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import com.zurrtum.create.foundation.block.BreakControlBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Replaces Create Fly's ServerPlayerGameModeMixin#removeBlock. Vanilla calls level.removeBlock(pos, false) in destroyBlock;
 * NeoForge routes both creative and survival breaking through its private removeBlock(pos, state, canHarvest, tool), which
 * calls state.onDestroyedByPlayer(...). Same behavior as Create's handler: a BreakControlBlock can veto the removal, and a
 * successful removal notifies the symmetry wand.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeBreakControlMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @WrapOperation(
        method = "removeBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/world/item/ItemStack;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onDestroyedByPlayer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/world/level/material/FluidState;)Z")
    )
    private boolean connectorCompat$breakControl(
        BlockState state, Level level, BlockPos pos, Player destroyer, ItemStack tool, boolean willHarvest, FluidState fluid,
        Operation<Boolean> original
    ) {
        if (state.getBlock() instanceof BreakControlBlock controlBlock && !controlBlock.onDestroyedByPlayer(state, level, pos, this.player)) {
            return false;
        }
        boolean removed = original.call(state, level, pos, destroyer, tool, willHarvest, fluid);
        if (removed) {
            SymmetryHandler.onBlockDestroyed(this.player, pos, state);
        }
        return removed;
    }
}
