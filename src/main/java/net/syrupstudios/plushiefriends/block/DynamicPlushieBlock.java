package net.syrupstudios.plushiefriends.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.syrupstudios.plushiefriends.block.entity.DynamicPlushieBlockEntity;
import org.jetbrains.annotations.Nullable;

public class DynamicPlushieBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape HEAD = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape TORSO = Block.box(6.0, 0.0, 7.0, 10.0, 6.0, 9.0);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            HEAD,
            TORSO,
            Block.box(5.0, 0.0, 2.0, 11.0, 2.0, 7.0),  // Legs sticking North
            Block.box(3.5, 1.0, 6.5, 12.5, 5.0, 9.0)   // Splayed Arms
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            HEAD,
            TORSO,
            Block.box(5.0, 0.0, 9.0, 11.0, 2.0, 14.0), // Legs sticking South
            Block.box(3.5, 1.0, 7.0, 12.5, 5.0, 9.5)   // Splayed Arms
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            HEAD,
            Block.box(7.0, 0.0, 6.0, 9.0, 6.0, 10.0),  // Rotated Torso
            Block.box(9.0, 0.0, 5.0, 14.0, 2.0, 11.0), // Legs sticking East
            Block.box(7.0, 1.0, 3.5, 9.5, 5.0, 12.5)   // Splayed Arms
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            HEAD,
            Block.box(7.0, 0.0, 6.0, 9.0, 6.0, 10.0),  // Rotated Torso
            Block.box(2.0, 0.0, 5.0, 7.0, 2.0, 11.0),  // Legs sticking West
            Block.box(6.5, 1.0, 3.5, 9.0, 5.0, 12.5)   // Splayed Arms
    );

    public DynamicPlushieBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case NORTH:
            default:
                return SHAPE_NORTH;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynamicPlushieBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) {
            return createTickerHelper(type, DynamicPlushieBlockEntity.TYPE, DynamicPlushieBlockEntity::serverTick);
        }
        return null;
    }
}