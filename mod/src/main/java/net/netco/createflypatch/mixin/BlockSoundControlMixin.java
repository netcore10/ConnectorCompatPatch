package net.netco.createflypatch.mixin;

import com.zurrtum.create.foundation.block.SoundControlBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Replaces Create Fly's LevelEventHandlerMixin#getBreakSound and LivingEntityMixin#getBlockFallSound.
 * On NeoForge both sounds (and other position-aware ones) go through IBlockExtension#getSoundType(state, level, pos, entity),
 * whose default is state.getSoundType(). Overriding it on Block lets Create's SoundControlBlocks supply their per-position sound.
 */
@Mixin(Block.class)
public abstract class BlockSoundControlMixin {
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (this instanceof SoundControlBlock block) {
            return block.getSoundGroup(level, pos);
        }
        return state.getSoundType(); // NeoForge's default
    }
}
