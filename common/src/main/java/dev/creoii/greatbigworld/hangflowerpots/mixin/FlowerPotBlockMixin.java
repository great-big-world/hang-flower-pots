package dev.creoii.greatbigworld.hangflowerpots.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockMixin extends Block {
    @Unique
    private static final BooleanProperty gbw$HANGING = BlockStateProperties.HANGING;

    public FlowerPotBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gbw$registerDefaultState(Block potted, Properties properties, CallbackInfo ci) {
        registerDefaultState(stateDefinition.any().setValue(gbw$HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(gbw$HANGING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() == Direction.DOWN) {
            return defaultBlockState().setValue(gbw$HANGING, true);
        }
        return super.getStateForPlacement(context);
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean gbw$fixUseItemOn(Level instance, BlockPos blockPos, BlockState newContents, Operation<Boolean> original, @Local(name = "state", argsOnly = true) BlockState state) {
        return original.call(instance, blockPos, newContents.setValue(gbw$HANGING, state.getValue(gbw$HANGING)));
    }

    @WrapOperation(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean gbw$fixUseWithoutItem(Level instance, BlockPos blockPos, BlockState newContents, Operation<Boolean> original, @Local(name = "state", argsOnly = true) BlockState state) {
        return original.call(instance, blockPos, newContents.setValue(gbw$HANGING, state.getValue(gbw$HANGING)));
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void gbw$fixUpdateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (directionToNeighbour == Direction.UP && neighbourState.isAir()) {
            cir.setReturnValue(defaultBlockState().setValue(gbw$HANGING, false));
        }
    }
}
