package net.netco.connectorcompat.mixin.create;

import com.zurrtum.create.foundation.block.SlipperinessControlBlock;
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
 * NeoForge routes block sound and friction through position-aware IBlockExtension methods whose defaults ignore the position.
 * Overriding them on Block lets Create's control blocks answer per position for every caller, replacing several Create Fly
 * mixins that break on NeoForge.
 */
@Mixin(Block.class)
public abstract class BlockSoundControlMixin {
    /** Replaces LevelEventHandlerMixin#getBreakSound and LivingEntityMixin#getBlockFallSound (and covers other sound paths). */
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (this instanceof SoundControlBlock block) {
            return block.getSoundGroup(level, pos);
        }
        return state.getSoundType(); // NeoForge's default
    }

    /** Replaces AbstractBoatMixin, ExperienceOrbMixin and ItemEntityMixin#getSlipperiness; pos is the block below the entity. */
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (this instanceof SlipperinessControlBlock block) {
            return block.getSlipperiness(level, pos);
        }
        return ((Block) (Object) this).getFriction(); // NeoForge's default
    }
}
