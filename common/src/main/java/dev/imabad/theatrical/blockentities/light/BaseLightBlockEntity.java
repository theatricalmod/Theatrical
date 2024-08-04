package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.FixtureProvider;
import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.lighting.LambDynamicLight;
import dev.imabad.theatrical.lighting.LambDynamicLightUtil;
import dev.imabad.theatrical.lighting.LightManager;
import dev.imabad.theatrical.mixin.ClipContextAccessor;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public abstract class BaseLightBlockEntity extends ClientSyncBlockEntity implements FixtureProvider, LambDynamicLight {
    AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private double distance = 0;
    protected int pan, tilt, focus, intensity, red, green, blue = 0;
    protected int prevTilt, prevPan, prevFocus, prevIntensity, prevRed, prevGreen, prevBlue = 0;
    private long tickTimer = 0;
    private BlockPos emissionBlock, prevEmissionBlock;
    private int prevLuminance;
    private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();

    public BaseLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void write(CompoundTag compoundTag) {
        if (compoundTag == null) {
            compoundTag = new CompoundTag();
        }
        compoundTag.putInt("pan", this.pan);
        compoundTag.putInt("tilt", this.tilt);
        compoundTag.putInt("focus", this.focus);
        compoundTag.putLong("timer", tickTimer);
        compoundTag.putDouble("distance", distance);
        compoundTag.putInt("intensity", intensity);
        compoundTag.putInt("prevIntensity", prevIntensity);
        compoundTag.putInt("red", red);
        compoundTag.putInt("green", green);
        compoundTag.putInt("blue", blue);
        compoundTag.putInt("prevRed", prevRed);
        compoundTag.putInt("prevGreen", prevGreen);
        compoundTag.putInt("prevBlue", prevBlue);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        pan = compoundTag.getInt("pan");
        tilt = compoundTag.getInt("tilt");
        focus = compoundTag.getInt("focus");
        prevPan = pan;
        prevTilt = tilt;
        prevFocus = focus;
        tickTimer = compoundTag.getLong("timer");
        distance = compoundTag.getDouble("distance");
        intensity = compoundTag.getInt("intensity");
        prevIntensity = compoundTag.getInt("prevIntensity");
        red = compoundTag.getInt("red");
        green = compoundTag.getInt("green");
        blue = compoundTag.getInt("blue");
        prevRed = compoundTag.getInt("prevRed");
        prevGreen = compoundTag.getInt("prevGreen");
        prevBlue = compoundTag.getInt("prevBlue");
    }

    public double getDistance() {
        return distance;
    }

    public AABB getRenderBoundingBox(){
        return INFINITE_EXTENT_AABB;
    }

    protected boolean storePrev(){
        boolean hasChanged = false;
        if(tilt != prevTilt){
            prevTilt = tilt;
            hasChanged = true;
        }
        if(pan != prevPan){
            prevPan = pan;
            hasChanged = true;
        }
        if(focus != prevFocus){
            prevFocus = focus;
            hasChanged = true;
        }
        if(intensity != prevIntensity){
            prevIntensity =  intensity;
            hasChanged = true;
        }
        if(red != prevRed){
            prevRed = red;
            hasChanged = true;
        }
        if(green != prevGreen){
            prevGreen = green;
            hasChanged = true;
        }
        if(blue != prevBlue){
            prevBlue = blue;
            hasChanged = true;
        }
        return hasChanged;
    }

    @Override
    public float getIntensity() {
        return intensity;
    }

    @Override
    public float getMaxLightDistance() {
        return TheatricalConfig.INSTANCE.COMMON.defaultMaxLightDist;
    }

    @Override
    public boolean shouldTrace() {
        return getIntensity() > 0;
    }

    @Override
    public boolean emitsLight() {
        return !getBlockState().getValue(HangableBlock.BROKEN) && TheatricalConfig.INSTANCE.COMMON.shouldEmitLight;
    }

    @Override
    public boolean isUpsideDown(){
        return false;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        BaseLightBlockEntity tile = (BaseLightBlockEntity) be;
//        if(!level.isClientSide){
            tile.tickTimer++;
            if(tile.tickTimer >= 5){
//                if(tile.storePrev()){
//                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
//                }

                tile.tickTimer = 0;
            }
            if(tile.shouldTrace()){
                tile.distance = tile.doRayTrace();
            }
            if (level.isClientSide() && LightManager.shouldUpdateDynamicLight()) {
                if (tile.isRemoved()) {
                    tile.setDynamicLightEnabled(false);
                } else {
                    tile.dynamicLightTick();
                    LightManager.updateTracking(tile);
                }
            }
//        } else {
//        }
    }

    public static <T extends BlockEntity> void tickInContraption(Level level, T be, Vec3 position) {
        BaseLightBlockEntity tile = (BaseLightBlockEntity) be;
//        if(!level.isClientSide){
        tile.tickTimer++;
        if(tile.tickTimer >= 5){
//                if(tile.storePrev()){
//                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
//                }

            tile.tickTimer = 0;
        }
        if(tile.shouldTrace()){
            tile.distance = tile.doRayTrace(position);
        }
        if (level.isClientSide() && LightManager.shouldUpdateDynamicLight()) {
            if (tile.isRemoved()) {
                tile.setDynamicLightEnabled(false);
            } else {
                tile.dynamicLightTick();
                LightManager.updateTracking(tile);
            }
        }
//        } else {
//        }
    }

    public int getPan() {
        return pan;
    }

    public int getTilt() {
        return tilt;
    }

    public int getFocus() {
        return focus;
    }

    public int getPrevTilt() {
        return prevTilt;
    }

    public int getPrevPan() {
        return prevPan;
    }

    public int getPrevFocus() {
        return prevFocus;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getPrevIntensity() {
        return prevIntensity;
    }

    public int getPrevRed() {
        return prevRed;
    }

    public int getPrevGreen() {
        return prevGreen;
    }

    public int getPrevBlue() {
        return prevBlue;
    }

    public int getColorHex(){
        return (getRed() << 16) | (getGreen() << 8) | getBlue();
    }

    public int getPrevColor(){
        return (getPrevRed() << 16) | (getPrevGreen() << 8) | getPrevBlue();
    }

    public static Optional<BlockState> getSupportingStructure(Level level, BlockPos pos, BlockState state){
        if(level != null){
            BlockState blockState = level.getBlockState(pos
                    .relative(state.getValue(HangableBlock.HANG_DIRECTION)));
            if(blockState.getBlock() instanceof Support) {
                return Optional.of(blockState);
            }
        }
        return Optional.empty();
    }

    public int getBasePan(){
        if(isHangingNonVertically(getBlockState())){
            Direction facing = getBlockState().getValue(BaseLightBlock.FACING);
            return switch (facing) {
                case NORTH, SOUTH -> 90;
                case WEST -> 180;
                default -> 0;
            };
        }
        return 0;
    }

    public int calculatePartialColour(float partialTicks){
        int r = (int) (getPrevRed() + ((getRed()) - getPrevRed()) * partialTicks);
        int g = (int) (getPrevGreen() + ((getGreen()) - getPrevGreen()) * partialTicks);
        int b = (int) (getPrevBlue() + ((getBlue()) - getPrevBlue()) * partialTicks);
        return (r << 16) | (g << 8) | b;
    }

    public static int calculatePartialColour(int prevRed, int prevGreen, int prevBlue, int red, int green, int blue, float partialTicks){
        int r = (int) (prevRed + ((red) - prevRed) * partialTicks);
        int g = (int) (prevGreen + ((green) - prevGreen) * partialTicks);
        int b = (int) (prevBlue + ((blue) - prevBlue) * partialTicks);
        return (r << 16) | (g << 8) | b;
    }


    public static final Vec3 calculateViewVector(float xRot, float yRot) {
        float f = xRot * 0.017453292F;
        float g = -yRot * 0.017453292F;
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3((double)(i * j), (double)(-k), (double)(h * j));
    }

    public static boolean isHangingNonVertically(BlockState blockState){
        return isHangingNonVertically(blockState.getValue(BaseLightBlock.HANG_DIRECTION),
                blockState.getValue(BaseLightBlock.HANGING));
    }
    public static boolean isHangingNonVertically(Direction hangDirection, boolean isHanging){
        return (hangDirection != Direction.DOWN && hangDirection != Direction.UP) && isHanging;
    }

    public static Vec3 rayTraceDir(BaseLightBlockEntity be){
        BlockState blockState = be.getBlockState();
        Direction hangDirection = blockState.getValue(BaseLightBlock.HANG_DIRECTION);
        Direction direction = blockState.getValue(BaseLightBlock.FACING);
        boolean isHangingNonVertically = isHangingNonVertically(hangDirection, blockState.getValue(BaseLightBlock.HANGING));
        // TODO: Come back and try make this use the same code for both.
        if(!isHangingNonVertically) {
            float tilt = be.getTilt();
            if (be.isUpsideDown() || be instanceof FresnelBlockEntity) {
                tilt = -tilt;
            }
            float pan = (direction.toYRot() - be.getPan());
            if(direction.getAxis() == Direction.WEST.getAxis()){
                pan -= 180;
            }
            if(be instanceof FresnelBlockEntity){
                pan *= -1;
            }
            if (be.isUpsideDown()) {
                if (direction.getAxis() == Direction.Axis.X) {
                    pan = (direction.getOpposite().toYRot() + be.getPan());
                } else {
                    pan = (direction.toYRot() + be.getPan());
                }
            }
            return BaseLightBlockEntity.calculateViewVector(tilt, pan);
        } else {
            // Kindly put together with help from @Hekera & @Mikey
            Direction opposite = hangDirection.getOpposite();
            int step = opposite.getAxisDirection().getStep();
            float offset = 0;
            float toRad = 3.14159F / 180;
            float pan = be.getBasePan() + be.getPan();
            float tilt = be.getTilt();
            pan *= step * toRad;
            tilt *= -step * toRad;
            float sinPan = Mth.sin(pan + offset);
            float cosPan = Mth.cos(pan + offset);
            float cosTilt = Mth.cos(tilt);
            float x = sinPan * cosTilt;
            float y = Mth.sin(tilt);
            float z = cosPan * cosTilt;
            AxisCycle cycle = AxisCycle.VALUES[(opposite.getAxis().ordinal() + 2) % AxisCycle.VALUES.length];
            return new Vec3(cycle.cycle(x, y, z, Direction.Axis.X), cycle.cycle(x, y, z, Direction.Axis.Y), cycle.cycle(x, y, z, Direction.Axis.Z));
        }
    }

    public double doRayTrace(){
        return doRayTrace(getBlockPos().getCenter());
    }

    public double doRayTrace(Vec3 position) {
        Vec3 viewVector = BaseLightBlockEntity.rayTraceDir(this);
        double distance = getMaxLightDistance();
        Vec3 vec33 = position.add(viewVector.x * distance, viewVector.y * distance, viewVector.z * distance);
        ClipContext context = new ClipContext(position, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        ((ClipContextAccessor) context).setCollisionContext(new LightCollisionContext(getBlockPos()));
        BlockHitResult result = this.level.clip(context);
        BlockPos lightPos = result.getBlockPos();
        if (result.getType() != HitResult.Type.MISS && !result.isInside()) {
            distance = result.getLocation().distanceTo(position);
            if (!result.getBlockPos().equals(getBlockPos())) {
                lightPos = result.getBlockPos().relative(result.getDirection(), 1);
            }
        }
        distance = new Vec3(lightPos.getX(), lightPos.getY(), lightPos.getZ()).distanceTo(new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()));
        emissionBlock = lightPos;
        return distance;
    }

    @Override
    public void setRemoved() {
        if(emissionBlock != null){
            this.setDynamicLightEnabled(false);
            emissionBlock = null;
        }
        super.setRemoved();
    }

    public void setTilt(int tilt){
        this.prevTilt = this.tilt;
        this.tilt = tilt;
    }

    public void setPan(int pan){
        this.prevPan = this.pan;
        this.pan = pan;
    }

    @Override
    public double getDynamicLightX() {
        return emissionBlock.getX();
    }

    @Override
    public double getDynamicLightY() {
        return emissionBlock.getY();
    }

    @Override
    public double getDynamicLightZ() {
        return emissionBlock.getZ();
    }

    @Override
    public Level getDynamicLightWorld() {
        return this.getLevel();
    }

    @Override
    public void resetDynamicLight() {

    }
    @Override
    public int getLuminance() {
        float newVal = intensity / 255f;
        return (int) (newVal * 15f);
    }

    @Override
    public void dynamicLightTick() {

    }

    @Override
    public boolean shouldUpdateDynamicLight() {
        return LightManager.shouldUpdateDynamicLight() && emitsLight() && emissionBlock != null;
    }

    @Override
    public boolean lambdynlights$updateDynamicLight(LevelRenderer renderer) {
        if (!this.shouldUpdateDynamicLight())
            return false;
        int luminance = this.getLuminance();

        if(!emissionBlock.equals(prevEmissionBlock) || luminance != prevLuminance){
            this.prevEmissionBlock = emissionBlock;
            this.prevLuminance = luminance;
            var newPos = new LongOpenHashSet();

            if (luminance > 0) {
                var entityChunkPos = new ChunkPos(emissionBlock);
                var chunkPos = new BlockPos.MutableBlockPos(entityChunkPos.x, LambDynamicLightUtil.getSectionCoord(emissionBlock.getY()), entityChunkPos.z);

                LightManager.scheduleChunkRebuild(renderer, chunkPos);
                LightManager.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);

                var directionX = (emissionBlock.getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
                var directionY = (emissionBlock.getY() & 15) >= 8 ? Direction.UP : Direction.DOWN;
                var directionZ = (emissionBlock.getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

                for (int i = 0; i < 7; i++) {
                    if (i % 4 == 0) {
                        chunkPos.move(directionX); // X
                    } else if (i % 4 == 1) {
                        chunkPos.move(directionZ); // XZ
                    } else if (i % 4 == 2) {
                        chunkPos.move(directionX.getOpposite()); // Z
                    } else {
                        chunkPos.move(directionZ.getOpposite()); // origin
                        chunkPos.move(directionY); // Y
                    }
                    LightManager.scheduleChunkRebuild(renderer, chunkPos);
                    LightManager.updateTrackedChunks(chunkPos, this.trackedLitChunkPos, newPos);
                }
            }
            // Schedules the rebuild of removed chunks.
            this.lambdynlights$scheduleTrackedChunksRebuild(renderer);
            // Update tracked lit chunks.
            this.trackedLitChunkPos = newPos;
            return true;
        }
        return false;
    }

    @Override
    public void lambdynlights$scheduleTrackedChunksRebuild(LevelRenderer renderer) {
        if (Minecraft.getInstance().level == this.level)
            for (long pos : this.trackedLitChunkPos) {
                LightManager.scheduleChunkRebuild(renderer, pos);
            }
    }
}
