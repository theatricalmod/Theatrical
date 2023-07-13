package dev.imabad.theatrical.blocks;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.math.Vector3d;
import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.graphs.CableNodePos;
import dev.imabad.theatrical.graphs.GlobalCableManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CableBlock extends Block implements EntityBlock {

    public static enum DirectionAxes {
        DOWN(new Vec3(1, 0, 0), new Vec3(0, 0, 1)),
        UP(new Vec3(1, 0, 0), new Vec3(0, 0, 1)),
        NORTH(new Vec3(0, 1, 0), new Vec3(0, 0, 1)),
        SOUTH(new Vec3(0, 1, 0), new Vec3(0, 0, 1)),
        WEST(new Vec3(1, 0, 0), new Vec3(0, 1, 0)),
        EAST(new Vec3(1, 0, 0), new Vec3(0, 1, 0));
        Vec3[] axes;
        DirectionAxes(Vec3... axes){
            this.axes = axes;
        }

        public Vec3[] getAxes() {
            return axes;
        }

        public static DirectionAxes fromDirection(Direction direction){
            return valueOf(direction.name());
        }
    }
    public static final EnumProperty<CableType> CABLE_TYPE = EnumProperty.create("type", CableType.class);
    public static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double h0 = 1D / 16D;
        double h1 = 1D - h0;

        double v0 = 1D / 16D;
        double v1 = 1D - v0;

        BOXES[0] = Shapes.create(new AABB(h0, 0D, h0, h1, v0, h1));
        BOXES[1] = Shapes.create(new AABB(h0, v1, h0, h1, 1D, h1));
        BOXES[2] = Shapes.create(new AABB(h0, h0, 0D, h1, h1, v0));
        BOXES[3] = Shapes.create(new AABB(h0, h0, v1, h1, h1, 1D));
        BOXES[4] = Shapes.create(new AABB(0D, h0, h0, v0, h1, h1));
        BOXES[5] = Shapes.create(new AABB(v1, h0, h0, 1D, h1, h1));
    }
    public CableBlock() {
        super(Properties.of(Material.METAL)
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn));
        this.registerDefaultState(this.getStateDefinition().any().setValue(CABLE_TYPE, CableType.BUNDLED));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CABLE_TYPE);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for(int i = 0; i < BOXES.length; i++){
            if(level.getBlockEntity(pos) instanceof CableBlockEntity cable && cable.hasSide(Direction.values()[i])){
                shape = Shapes.or(shape, BOXES[i]);
            }
        }
        return shape;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        GlobalCableManager.onCableAdded(level, pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide)
            return;
        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        GlobalCableManager.onCableRemoved(level, pos, state);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static int getSubShapeHit(CableBlockEntity cableBlockEntity, Entity entity, BlockPos pos, VoxelShape... shapes)
    {
        double distance = 5;
        Vec3 vecStart = new Vec3(entity.xOld, entity.yOld + entity.getEyeHeight(), entity.zOld);
        Vec3 vecLook = entity.getLookAngle();
        Vec3 vecEnd = vecStart.add(vecLook.x * distance, vecLook.y * distance, vecLook.z * distance);

        int shapeIndex = -1;
        double hitDistance = 100;
        for (byte index = 0; index < shapes.length; index++) {
            if(!cableBlockEntity.hasSide(Direction.values()[index])){
                continue;
            }
            BlockHitResult shapeHit = shapes[index].clip(vecStart, vecEnd, pos);
            if (shapeHit == null) {
                continue;
            }
            double shapeDistance = shapeHit.getLocation().distanceToSqr(vecStart);
            if (shapeDistance < hitDistance) {
                hitDistance = shapeDistance;
                shapeIndex = index;
            }
        }
        return shapeIndex;
    }
    public Collection<CableNodePos.DiscoveredPosition> getConnected(@Nullable CableNodePos from, BlockGetter level, BlockPos pos, BlockState state){
        Collection<CableNodePos.DiscoveredPosition> connected = new ArrayList<>();
        Vec3 center = Vec3.atBottomCenterOf(pos);
        for(Direction direction : Direction.values()){
            if(level.getBlockEntity(pos) instanceof CableBlockEntity cable){
                if(cable.hasSide(direction)){
                    Vec3[] axes = DirectionAxes.fromDirection(direction).getAxes();
                    for (Vec3 axe : axes) {
                        Vec3 firstOffset = axe.scale(0.5).add(center);
                        CableNodePos.DiscoveredPosition firstLocation = new CableNodePos.DiscoveredPosition(level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                                firstOffset)
                                .typeA(state.getValue(CABLE_TYPE))
                                .typeB(getCableType(level, new BlockPos(firstOffset)));
                        Vec3 secondOffset = axe.scale(-0.5).add(center);
                        CableNodePos.DiscoveredPosition secondLocation = new CableNodePos.DiscoveredPosition(level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                                secondOffset)
                                .typeA(state.getValue(CABLE_TYPE))
                                .typeB(getCableType(level, new BlockPos(secondOffset)));
                        boolean skipFirst = false;
                        boolean skipSecond = false;
                        if(from != null){
                            boolean firstSameAsFrom = firstLocation.equals(from);
                            boolean secondSameAsFrom = secondLocation.equals(from);

//                            if(!firstSameAsFrom && !secondSameAsFrom){
//                                continue;
//                            }

//                            if(firstSameAsFrom){
//                                skipFirst = true;
//                            }
//                            if(secondSameAsFrom){
//                                skipSecond = true;
//                            }
                        }
                        if(!skipFirst){
                            connected.add(firstLocation);
                        }
                        if(!skipSecond){
                            connected.add(secondLocation);
                        }
                    }
                }
            }
        }
        if(from != null) {
            if (connected.stream().noneMatch(dPos -> dPos.equals(from))) {
                connected.clear();
            } else {
                connected.removeIf(dPos -> dPos.equals(from));
            }
        }
        return connected;
    }

    public static Collection<CableNodePos.DiscoveredPosition> walkCable(BlockGetter level, CableNodePos cablePos){
        List<CableNodePos.DiscoveredPosition> foundCables = new ArrayList<>();
        if(cablePos == null){
            System.out.println("Oh my god, where the fuck is this null cablepos");
            return foundCables;
        }
        for(BlockPos pos : cablePos.allAdjacent()){
            BlockState blockState = level.getBlockState(pos);
            if(blockState.getBlock() instanceof CableBlock cable){
                foundCables.addAll(cable.getConnected(cablePos, level, pos, blockState));
            }
        }
        return foundCables;
    }

    public static CableType getCableType(BlockGetter level, BlockPos pos){
        if(level != null){
            Block block = level.getBlockState(pos).getBlock();
            if(block instanceof CableBlock){
                return level.getBlockState(pos).getValue(CableBlock.CABLE_TYPE);
            }
        }
        return CableType.BUNDLED;
    }

}
