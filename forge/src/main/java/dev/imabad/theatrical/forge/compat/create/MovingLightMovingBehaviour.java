package dev.imabad.theatrical.forge.compat.create;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import dev.imabad.theatrical.Constants;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.client.LazyRenderers;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import dev.imabad.theatrical.client.blockentities.FixtureRenderer;
import dev.imabad.theatrical.client.blockentities.MovingLightRenderer;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.net.compat.create.SendBEDataToContraption;
import dev.imabad.theatrical.util.RndUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class MovingLightMovingBehaviour implements MovementBehaviour/*, DMXConsumer */{

    private final Double beamOpacity = TheatricalConfig.INSTANCE.CLIENT.beamOpacity;

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        MovementBehaviour.super.visitNewPosition(context, pos);
    }

    @Override
    public void startMoving(MovementContext context) {
        MovementBehaviour.super.startMoving(context);
//        addConsumer(context);
    }

    @Override
    public void stopMoving(MovementContext context) {
        MovementBehaviour.super.stopMoving(context);
//        removeConsumer(context);
    }

    @Override
    public boolean renderAsNormalBlockEntity() {
        return true;
    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        if(!context.world.isClientSide() && context.world instanceof ServerLevel serverLevel){
            UUID networkId = context.blockEntityData.getUUID("network");
            DMXNetwork network = DMXNetworkData.getInstance().getNetwork(networkId);
            if(network != null) {
                byte[] dmxUniverses = network.getDmxData(context.blockEntityData.getInt("dmxUniverse"));
                consume(context.blockEntityData, dmxUniverses);
                new SendBEDataToContraption(
                        context.contraption.entity.getId(), context.localPos, context.blockEntityData)
                        .sendToLevel(serverLevel);
//                context.contraption.getContraptionWorld().sendBlockUpdated(context.localPos, context.state, context.state, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public void writeExtraData(MovementContext context) {
        if(context.temporaryData instanceof byte[] bytes)
            context.data.putByteArray("dmxData", bytes);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        if(context.blockEntityData == null){
            return;
        }
        BlockEntity blockEntity = context.contraption.presentBlockEntities.get(context.localPos);
        if(blockEntity instanceof MovingLightBlockEntity movingLightBlock){
            float partialTicks = AnimationTickHolder.getPartialTicks();
            Vec3 v = context.position;
            if (v == null) {
                v = new Vec3(0, 0, 0);
            }
            MovingLightBlockEntity.tickInContraption(context.world, movingLightBlock, v);
            BlockPos pos = BlockPos.containing(v);
            boolean isFlipped = Fixtures.MOVING_LIGHT.get().isUpsideDown(context.state);
            boolean isHanging = ((HangableBlock) context.state.getBlock()).isHanging(context.world, pos);
            FixtureRenderer.FixtureRenderContext fixtureRenderContext = new FixtureRenderer.FixtureRenderContext(movingLightBlock.getFixture(), context.state.getValue(MovingLightBlock.FACING), isFlipped, isHanging, movingLightBlock.getPrevPan(),
                    movingLightBlock.getPan(), movingLightBlock.getPrevTilt(), movingLightBlock.getTilt(), BaseLightBlockEntity
                    .getSupportingStructure(movingLightBlock.getLevel(), movingLightBlock.getBlockPos(), context.state), movingLightBlock.getIntensity(),
                    movingLightBlock.getPrevIntensity(), movingLightBlock.getPrevRed(), movingLightBlock.getRed(), movingLightBlock.getPrevGreen(), movingLightBlock.getGreen(),
                    movingLightBlock.getPrevBlue(), movingLightBlock.getBlue(), movingLightBlock.getBlockPos(), movingLightBlock.getFocus(), movingLightBlock.getDistance());
            if(FixtureRenderer.shouldRenderBeam(fixtureRenderContext)) {
                PoseStack poseStack = matrices.getModelViewProjection();
                poseStack.pushPose();
                poseStack.translate(context.localPos.getX(), context.localPos.getY(), context.localPos.getZ());
                MovingLightRenderer.doPreparePoseStack(fixtureRenderContext, poseStack, partialTicks, context.state);
                VertexConsumer beamConsumer = buffer.getBuffer(TheatricalRenderTypes.BEAM);
                poseStack.translate(movingLightBlock.getFixture().getBeamStartPosition()[0], movingLightBlock.getFixture().getBeamStartPosition()[1], movingLightBlock.getFixture().getBeamStartPosition()[2]);
                float intensity = (movingLightBlock.getPrevIntensity() + ((movingLightBlock.getIntensity()) - movingLightBlock.getPrevIntensity()) * partialTicks);
                int color = movingLightBlock.calculatePartialColour(partialTicks);
                FixtureRenderer.renderLightBeam(beamConsumer, poseStack, fixtureRenderContext, partialTicks, (float) ((intensity * beamOpacity) / 255f), movingLightBlock.getFixture().getBeamWidth(), (float) movingLightBlock.getDistance(), color);
                poseStack.popPose();
                LazyRenderers.addLazyRender(new LazyRenderers.LazyRenderer() {
                    @Override
                    public void render(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, Camera camera, float partialTick) {
                        poseStack.pushPose();
                        Vec3 offset = Vec3.atLowerCornerOf(movingLightBlock.getBlockPos()).subtract(camera.getPosition());
                        poseStack.translate(offset.x, offset.y, offset.z);
                        MovingLightRenderer.doPreparePoseStack(fixtureRenderContext, poseStack, partialTick, context.state);
                        VertexConsumer beamConsumer = bufferSource.getBuffer(TheatricalRenderTypes.BEAM);
                        poseStack.translate(movingLightBlock.getFixture().getBeamStartPosition()[0], movingLightBlock.getFixture().getBeamStartPosition()[1], movingLightBlock.getFixture().getBeamStartPosition()[2]);
                        float intensity = (movingLightBlock.getPrevIntensity() + ((movingLightBlock.getIntensity()) - movingLightBlock.getPrevIntensity()) * partialTick);
                        int color = movingLightBlock.calculatePartialColour(partialTick);
//                        FixtureRenderer.renderLightBeam(beamConsumer, poseStack, fixtureRenderContext, partialTick, (float) ((intensity * beamOpacity) / 255f), movingLightBlock.getFixture().getBeamWidth(), (float) movingLightBlock.getDistance(), color);
                        poseStack.popPose();
                    }

                    @Override
                    public Vec3 getPos(float partialTick) {
                        return blockEntity.getBlockPos().getCenter();
                    }
                });
            }
        }
//        Vec3 v = context.position;
//        if (v == null) {
//            v = new Vec3(0, 0, 0);
//        }
//        BlockPos pos = BlockPos.containing(v);
//        boolean isFlipped = Fixtures.MOVING_LIGHT.get().isUpsideDown(context.state);
//        boolean isHanging = ((HangableBlock) context.state.getBlock()).isHanging(context.world, pos);
//        FixtureRenderer.FixtureRenderContext fixtureRenderContext =
//                new FixtureRenderer.FixtureRenderContext(
//                        Fixtures.MOVING_LIGHT.get(), context.state.getValue(MovingLightBlock.FACING), isFlipped,
//                        isHanging, context.blockEntityData.getInt("prevPan"),
//                        context.blockEntityData.getInt("pan"), context.blockEntityData.getInt("prevTilt"),
//                        context.blockEntityData.getInt("tilt"),
//                        BaseLightBlockEntity.getSupportingStructure(context.world, pos, context.state), context.blockEntityData.getFloat("intensity"),
//                        context.blockEntityData.getFloat("prevIntensity"), context.blockEntityData.getInt("prevRed"),
//                        context.blockEntityData.getInt("red"), context.blockEntityData.getInt("prevGreen"),
//                        context.blockEntityData.getInt("green"), context.blockEntityData.getInt("prevBlue"),
//                        context.blockEntityData.getInt("blue"), context.localPos, context.blockEntityData.getInt("focus"), context.blockEntityData.getDouble("distance"));
//        PoseStack modelViewProjection = matrices.getModelViewProjection();
//        Vec3 vec3 = Vec3.atCenterOf(context.localPos);
//        modelViewProjection.pushPose();
//        modelViewProjection.translate(context.localPos.getX(), context.localPos.getY(), context.localPos.getZ());
////        VertexConsumer linesB = buffer.getBuffer(RenderType.lines());
////        LevelRenderer.renderLineBox(modelViewProjection, linesB, AABB.ofSize(new Vec3(0, 0, 0), .1d, .1d, .1d), 1, 1, 1, 1);
//        MovingLightRenderer.doRender(fixtureRenderContext, modelViewProjection,
//                buffer.getBuffer(RenderType.cutout()),
//                1, context.state, LevelRenderer.getLightColor(context.world, pos), 0);
////        modelViewProjection.popPose();
////        modelViewProjection.pushPose();
////        modelViewProjection.translate(vec3.x, vec3.y, vec3.z);
////        beforeRenderBeam(fixtureRenderContext, poseStack, vertexConsumer, multiBufferSource,  partialTick, blockState, packedLight, packedOverlay);
////        linesB = buffer.getBuffer(RenderType.lines());
////        LevelRenderer.renderLineBox(modelViewProjection, linesB, AABB.ofSize(new Vec3(0, 0, 0), .1d, .1d, .1d), 1, 1, 1, 1);
//        if(FixtureRenderer.shouldRenderBeam(fixtureRenderContext)){
//            VertexConsumer beamConsumer = buffer.getBuffer(TheatricalRenderTypes.BEAM);
//            float intensity = (fixtureRenderContext.prevIntensity() + ((fixtureRenderContext.intensity()) - fixtureRenderContext.prevIntensity()) * 1);
//            int color = BaseLightBlockEntity.calculatePartialColour(
//                    fixtureRenderContext.prevRed(),
//                    fixtureRenderContext.prevGreen(),
//                    fixtureRenderContext.prevBlue(),
//                    fixtureRenderContext.red(),
//                    fixtureRenderContext.green(),
//                    fixtureRenderContext.blue(),
//                    1);
//            modelViewProjection.translate(fixtureRenderContext.fixtureType().getBeamStartPosition()[0], fixtureRenderContext.fixtureType().getBeamStartPosition()[1], fixtureRenderContext.fixtureType().getBeamStartPosition()[2]);
////            Vec3 vec3 = Vec3.atCenterOf(context.localPos);
////            modelViewProjection.translate(vec3.x, vec3.y, vec3.z);
////            MovingLightRenderer.doPreparePoseStack(fixtureRenderContext, modelViewProjection, 1, context.state);
////            modelViewProjection.translate(0.5, 0.5, 0.5);
//            MovingLightRenderer.renderLightBeam(beamConsumer, modelViewProjection,
//                    fixtureRenderContext, 1, (float) ((intensity * beamOpacity) / 255f),
//                    fixtureRenderContext.fixtureType().getBeamWidth(), (float) fixtureRenderContext.distance(), color);
////            LazyRenderers.addLazyRender(new LazyRenderers.LazyRenderer() {
////                @Override
////                public void render(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, Camera camera, float partialTick) {
////                    poseStack.pushPose();
////                    Vec3 offset = Vec3.atLowerCornerOf(fixtureRenderContext.pos()).subtract(camera.getPosition());
////                    poseStack.translate(offset.x, offset.y, offset.z);
////                    MovingLightRenderer.doPreparePoseStack(fixtureRenderContext, poseStack, partialTick, context.state);
////                    VertexConsumer beamConsumer = bufferSource.getBuffer(TheatricalRenderTypes.BEAM);
////                    poseStack.translate(fixtureRenderContext.fixtureType().getBeamStartPosition()[0], fixtureRenderContext.fixtureType().getBeamStartPosition()[1], fixtureRenderContext.fixtureType().getBeamStartPosition()[2]);
////
////                    MovingLightRenderer.renderLightBeam(beamConsumer, poseStack, fixtureRenderContext, partialTick, (float) ((intensity * beamOpacity) / 255f), fixtureRenderContext.fixtureType().getBeamWidth(), (float) fixtureRenderContext.distance(), color);
////                    poseStack.popPose();
////                }
////
////                @Override
////                public Vec3 getPos(float partialTick) {
////                    return context.localPos.getCenter();
////                }
////            });
//        }
//        modelViewProjection.popPose();
    }
//    private void generateDeviceId(MovementContext context) {
//        byte[] bytes = new byte[4];
//        if (context.world != null) {
//            RndUtils.nextBytes(context.world.getRandom(), bytes);
//        } else {
//            new Random().nextBytes(bytes);
//        }
//        data.putByteArray("deviceId", new RDMDeviceId(Constants.MANUFACTURER_ID, bytes).toBytes());
//    }
//    private void updateConsumer(MovementContext context){
//        var dmxData = DMXNetworkData.getInstance().getNetwork(data.getUUID("network"));
//        if (dmxData != null) {
//            dmxData.updateConsumer(this);
//        }
//    }
//
//    private void removeConsumer(MovementContext context){
//        var dmxData = DMXNetworkData.getInstance().getNetwork(data.getUUID("network"));
//        if (dmxData != null) {
//            dmxData.removeConsumer(this);
//        }
//    }
//
//    private void addConsumer(MovementContext context){
//        var dmxData = DMXNetworkData.getInstance().getNetwork(data.getUUID("network"));
//        if (dmxData != null) {
//            if(!data.contains("deviceId")){
//                generateDeviceId(context);
//            }
//            dmxData.addConsumer(this);
//        }
//    }
//
//    @Override
//    public void setNetworkId(UUID newNetworkId) {
//        data.putUUID("networkId", newNetworkId);
//    }
//
//    @Override
//    public int getChannelCount() {
//        return 7;
//    }
//
//    @Override
//    public int getChannelStart() {
//        return data.getInt("channelStartPoint");
//    }
//
//    @Override
//    public int getUniverse() {
//        return data.getInt("dmxUniverse");
//    }
//
    protected boolean storePrev(CompoundTag data){
        boolean hasChanged = false;
        if(data.getInt("tilt") != data.getInt("prevTilt")){
            data.putInt("prevTilt", data.getInt("tilt"));
            hasChanged = true;
        }
        if(data.getInt("pan") != data.getInt("prevPan")){
            data.putInt("prevPan", data.getInt("pan"));
            hasChanged = true;
        }
        if(data.getInt("focus") != data.getInt("prevFocus")){
            data.putInt("prevFocus", data.getInt("focus"));
            hasChanged = true;
        }
        if(data.getInt("intensity") != data.getInt("prevIntensity")){
            data.putInt("prevIntensity", data.getInt("intensity"));
            hasChanged = true;
        }
        if(data.getInt("red") != data.getInt("prevRed")){
            data.putInt("prevRed", data.getInt("red"));
            hasChanged = true;
        }
        if(data.getInt("green") != data.getInt("prevGreen")){
            data.putInt("prevGreen", data.getInt("green"));
            hasChanged = true;
        }
        if(data.getInt("blue") != data.getInt("prevBlue")){
            data.putInt("prevBlue", data.getInt("blue"));
            hasChanged = true;
        }
        return hasChanged;
    }

    public void consume(CompoundTag data, byte[] dmxValues) {
        int start = data.getInt("channelStartPoint") > 0 ? data.getInt("channelStartPoint") - 1 : 0;
        byte[] ourValues = Arrays.copyOfRange(dmxValues, start,
                start+ data.getInt("channelCount"));
        if(ourValues.length < 7){
            return;
        }
        this.storePrev(data);
        data.putInt("intensity", convertByteToInt(ourValues[0]));
        data.putInt("red", convertByteToInt(ourValues[1]));
        data.putInt("green", convertByteToInt(ourValues[2]));
        data.putInt("blue", convertByteToInt(ourValues[3]));
        data.putInt("focus", convertByteToInt(ourValues[4]));
        data.putInt("pan", (int) ((convertByteToInt(ourValues[5]) * 360) / 255f) - 180);
        data.putInt("tilt",(int) ((convertByteToInt(ourValues[6]) * 180) / 255F) - 180);
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }
//
//    @Override
//    public RDMDeviceId getDeviceId() {
//        return new RDMDeviceId(data.getByteArray("deviceId"));
//    }
//
//    @Override
//    public int getDeviceTypeId() {
//        return 0x01;
//    }
//
//    @Override
//    public String getModelName() {
//        return "Moving Head";
//    }
//
//    @Override
//    public ResourceLocation getFixtureId() {
//        return Fixtures.MOVING_LIGHT.getId();
//    }
//
//    @Override
//    public int getActivePersonality() {
//        return 0;
//    }
//
//    @Override
//    public UUID getNetworkId() {
//        return data.getUUID("network");
//    }
//
//    @Override
//    public void setStartAddress(int startAddress) {
//        data.putInt("channelStartPoint", startAddress);
//    }
}
