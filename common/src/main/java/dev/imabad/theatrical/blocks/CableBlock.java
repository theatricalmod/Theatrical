package dev.imabad.theatrical.blocks;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.graphs.CableNodePos;
import dev.imabad.theatrical.graphs.GlobalCableManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CableBlock extends Block implements EntityBlock {

    public static enum DirectionAxes {
        DOWN(new Vec3(1, 0, 0), new Vec3(0, 0, 1)),
        UP(new Vec3(1, 0, 0), new Vec3(0, 0, 1)),
        NORTH(new Vec3(0, 1, 0), new Vec3(1, 0, 0)),
        SOUTH(new Vec3(0, 1, 0), new Vec3(1, 0, 0)),
        WEST(new Vec3(0, 0, 1), new Vec3(0, 1, 0)),
        EAST(new Vec3(0, 0, 1), new Vec3(0, 1, 0));
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
    // Get positions around the block specified that are connected to the current block.
    public Collection<CableNodePos.DiscoveredPosition> getConnected(@Nullable CableNodePos from, BlockGetter level, BlockPos pos, BlockState state){
        Set<CableNodePos.DiscoveredPosition> connected = new HashSet<>();
        // Grab center of the position provided
        Vec3 center = Vec3.atBottomCenterOf(pos);
        // Check if the tile is an actual cable
        if(level.getBlockEntity(pos) instanceof CableBlockEntity cable){
            // Loop through all the directions
            for(Direction direction : Direction.values()){
                // Check the cable has the side provided
                if(cable.hasSide(direction)){
                    // Get the axis' that the side has possible connections from
                    Vec3[] axes = DirectionAxes.fromDirection(direction).getAxes();
                    Vec3 dirCenter = modifyCenter(center, direction);
                    // Loop through the axis'
                    for (Vec3 axe : axes) {
                        // Add possible connection points from each axis to the list
                        addToListIfConnected(from, connected,
                                (isPositive) -> level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                                (isPositive, tPos) -> getCableType(level, new BlockPos(tPos)),
                                getOffsetPos(dirCenter, direction, axe, false));
                        addToListIfConnected(from, connected,
                                (isPositive) -> level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                                (isPositive, tPos) -> getCableType(level, new BlockPos(tPos)),
                                getOffsetPos(dirCenter, direction, axe, true));
                    }
                }
            }
        }
        return connected;
    }

    public Collection<CableNodePos.DiscoveredPosition> getPossibleNodesForSide(Direction side, BlockGetter level, BlockPos pos){
        Collection<CableNodePos.DiscoveredPosition> connected = new ArrayList<>();
        // Grab center of the position provided
        Vec3 center = Vec3.atBottomCenterOf(pos);
        // Check if the tile is an actual cable
        if(level.getBlockEntity(pos) instanceof CableBlockEntity cable){
            // Get the axis' that the side has possible connections from
            Vec3[] axes = DirectionAxes.fromDirection(side).getAxes();
            Vec3 dirCenter = modifyCenter(center, side);
            // Loop through the axis'
            for (Vec3 axe : axes) {
                // Add possible connection points from each axis to the list
                addToListIfConnected(null, connected,
                        (isPositive) -> level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                        (isPositive, tPos) -> getCableType(level, new BlockPos(tPos)),
                        getOffsetPos(dirCenter, side, axe, false));
                addToListIfConnected(null, connected,
                        (isPositive) -> level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                        (isPositive, tPos) -> getCableType(level, new BlockPos(tPos)),
                        getOffsetPos(dirCenter, side, axe, true));
            }
        }
        return connected;
    }

    @NotNull
    private static BiFunction<Double, Boolean, Vec3> getOffsetPos(Vec3 center, Direction direction, Vec3 axe, boolean isNeg) {
        return (distance, isPositive) -> modifyAxis(axe, direction).scale(isPositive ? 0 : isNeg ? -distance : distance).add(center);
    }

    public static Vec3 modifyAxis(Vec3 axe, Direction direction){
        Vector3f xyzDegrees = direction.getRotation().toXYZDegrees();
        return axe;
    }

    public static Vec3 modifyCenter(Vec3 center, Direction direction){
        switch(direction){
            case EAST -> {
                return center.add(0.5, 0.5, 0);
            }
            case WEST -> {
                return center.add(-0.5, 0.5, 0);
            }
            case NORTH -> {
                return center.add(0, 0.5, -0.5);
            }
            case SOUTH -> {
                return center.add(0, 0.5, 0.5);
            }
            case UP -> {
                return center.add(0, 1, 0);
            }
        }
        return center;
    }

    public static void addToListIfConnected(@Nullable CableNodePos from, Collection<CableNodePos.DiscoveredPosition> listOfPos,
                                            Function<Boolean, ResourceKey<Level>> dimensionGetter,
                                            BiFunction<Boolean, Vec3, CableType> typeGetter,
                                            BiFunction<Double, Boolean, Vec3> posGetter){
        // Our first position is offset by 0.5 in the positive axis
        Vec3 firstOffset = posGetter.apply(0.5, true);
        CableNodePos.DiscoveredPosition firstLocation = new CableNodePos.DiscoveredPosition(dimensionGetter.apply(true),
                firstOffset)
                .typeA(typeGetter.apply(true, posGetter.apply(0.0d, true)))
                .typeB(typeGetter.apply(true, posGetter.apply(1.0d, true)));
        // Our first position is offset by 0.5 in the negative axis
        Vec3 secondOffset = posGetter.apply(0.5, false);
        CableNodePos.DiscoveredPosition secondLocation = new CableNodePos.DiscoveredPosition(dimensionGetter.apply(false),
                secondOffset)
                .typeA(typeGetter.apply(false, posGetter.apply(0.0d, false)))
                .typeB(typeGetter.apply(false, posGetter.apply(1.0d, false)));

        boolean skipFirst = false;
        boolean skipSecond = false;
        if(from != null){
            boolean firstSameAsFrom = firstLocation.equals(from);
            boolean secondSameAsFrom = secondLocation.equals(from);

            // If neither of them came from the end we were provided then it's not possible to get to the positions.
            if(!firstSameAsFrom && !secondSameAsFrom){
                return;
            }

            if(firstSameAsFrom){
                skipFirst = true;
            }
            if(secondSameAsFrom){
                skipSecond = true;
            }
        }
        if(!skipFirst){
            listOfPos.add(firstLocation);
        }
        if(!skipSecond){
            listOfPos.add(secondLocation);
        }
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
