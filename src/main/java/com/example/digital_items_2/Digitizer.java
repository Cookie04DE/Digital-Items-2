package com.example.digital_items_2;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Digitizer extends BaseEntityBlock {

    public Digitizer(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level l, @NotNull BlockPos pos) {
        ItemStack i = ((DigitizerBlockEntity) Objects.requireNonNull(l.getBlockEntity(pos))).inventory.getStackInSlot(0);
        if(i.isEmpty()) {
            return 0;
        }
        return (int) (1 + (float)i.getCount() / i.getMaxStackSize() * 14);
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return Registration.DIGITIZER_BLOCK_ENTITY.get().create(pos, state);
    }

    @Override
    @Deprecated
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (!blockState.is(newState.getBlock())) {
            if (level.getBlockEntity(blockPos) instanceof DigitizerBlockEntity blockEntity) {
                Containers.dropContents(level, blockPos, new SimpleContainer(blockEntity.inventory.getStackInSlot(0)));
                level.updateNeighbourForOutputSignal(blockPos, this);
            }
            super.onRemove(blockState, level, blockPos, newState, isMoving);
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand interactionHand, @NotNull BlockHitResult blockHitResult) {
        if (!(player instanceof ServerPlayer)) {
            return InteractionResult.SUCCESS;
        }
        NetworkHooks.openScreen((ServerPlayer) player, (DigitizerBlockEntity) level.getBlockEntity(pos), pos);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level _level, @NotNull BlockState _state, @NotNull BlockEntityType<T> _type) {
        return (level, pos, state, type) -> ((DigitizerBlockEntity) Objects.requireNonNull(level.getBlockEntity(pos))).updatePoweredState(level, pos, state);
    }
}
