package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.client.LazyRenderers;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Map;
import java.util.WeakHashMap;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FresnelRenderer extends FixtureRenderer<FresnelBlockEntity> {
    private BakedModel cachedPanModel, cachedTiltModel, cachedStaticModel;

    private static class SmoothingState {
        float smoothPan = 0f;
        float smoothTilt = 0f;
        long lastUpdateTime = -1;
    }

    private final Map<FresnelBlockEntity, SmoothingState> smoothingStates = new WeakHashMap<>();
    private static final float SMOOTH_SPEED = 7f;

    public FresnelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderModel(FresnelBlockEntity blockEntity, PoseStack poseStack, VertexConsumer vertexConsumer, Direction facing, float partialTicks, boolean isFlipped, BlockState blockState, boolean isHanging, int packedLight, int packedOverlay) {
        if(cachedStaticModel == null){
            cachedStaticModel = TheatricalExpectPlatform.getBakedModel(blockEntity.getFixture().getStaticModel());
        }
        if (cachedPanModel == null){
            cachedPanModel = TheatricalExpectPlatform.getBakedModel(blockEntity.getFixture().getPanModel());
        }
        if (cachedTiltModel == null){
            cachedTiltModel = TheatricalExpectPlatform.getBakedModel(blockEntity.getFixture().getTiltModel());
        }
        //#region Fixture Hanging
        poseStack.translate(0.5F, 0, .5F);
        if(isHanging){
            Direction hangDirection = blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0F);
            if(hangDirection.getAxis() != Direction.Axis.Y){
                if(hangDirection.getAxis() == Direction.Axis.Z){
                    if(hangDirection == Direction.SOUTH) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    } else {
                        poseStack.mulPose(Axis.XN.rotationDegrees(90));
                    }
                } else {
                    if(hangDirection == Direction.EAST) {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                    } else {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(-90));
                    }
                }
            } else {
                //TODO: Handle hanging up
            }
            poseStack.translate(0, -0.5, 0F);
        }
        //#endregion
        if(facing.getAxis() == Direction.Axis.X){
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getOpposite().toYRot()));
        }
        poseStack.translate(-0.5F, 0, -.5F);
        if (isHanging) {
            Optional<BlockState> optionalSupport = blockEntity.getSupportingStructure();
            if (optionalSupport.isPresent()) {
                float[] transforms = blockEntity.getFixture().getTransforms(blockState, optionalSupport.get());
                poseStack.translate(transforms[0], transforms[1], transforms[2]);
            } else {
                poseStack.translate(0, 0.19, 0);
            }
        }

        minecraftRenderModel(poseStack, vertexConsumer, blockState, cachedStaticModel, packedLight, packedOverlay);

        SmoothingState state = smoothingStates.computeIfAbsent(blockEntity, k -> new SmoothingState());

        long now = System.nanoTime();
        if (state.lastUpdateTime < 0) state.lastUpdateTime = now;
        float deltaTime = (now - state.lastUpdateTime) / 1_000_000_000f;
        state.lastUpdateTime = now;
        deltaTime = Math.min(deltaTime, 0.1f);

        float targetPan = blockEntity.getPrevPan() + (blockEntity.getPan() - blockEntity.getPrevPan()) * partialTicks;
        float targetTilt = blockEntity.getPrevTilt() + (blockEntity.getTilt() - blockEntity.getPrevTilt()) * partialTicks;

        float alpha = 1f - (float) Math.exp(-SMOOTH_SPEED * deltaTime);
        state.smoothPan = state.smoothPan + (targetPan - state.smoothPan) * alpha;
        state.smoothTilt = state.smoothTilt + (targetTilt - state.smoothTilt) * alpha;

        //#region Model Pan
        float[] pans = blockEntity.getFixture().getPanRotationPosition();
        poseStack.translate(pans[0], pans[1], pans[2]);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.smoothPan));
        poseStack.translate(-pans[0], -pans[1], -pans[2]);
        minecraftRenderModel(poseStack, vertexConsumer, blockState, cachedPanModel, packedLight, packedOverlay);
        //#endregion
        //#region Model Tilt
        float[] tilts = blockEntity.getFixture().getTiltRotationPosition();
        poseStack.translate(tilts[0], tilts[1], tilts[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.smoothTilt));
        poseStack.translate(-tilts[0], -tilts[1], -tilts[2]);
        minecraftRenderModel(poseStack, vertexConsumer, blockState, cachedTiltModel, packedLight, packedOverlay);
        //#endregion
    }

    @Override
    public void beforeRenderBeam(FresnelBlockEntity blockEntity, PoseStack poseStack, VertexConsumer vertexConsumer, MultiBufferSource multiBufferSource, Direction facing, float partialTicks, boolean isFlipped, BlockState blockstate, boolean isHanging, int packedLight, int packedOverlay) {
        if(blockEntity.getIntensity() > 0){
            LazyRenderers.addLazyRender(new LazyRenderers.LazyRenderer() {
                @Override
                public void render(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, Camera camera, float partialTick) {
                    poseStack.pushPose();
                    Vec3 offset = Vec3.atLowerCornerOf(blockEntity.getBlockPos()).subtract(camera.getPosition());
                    poseStack.translate(offset.x, offset.y, offset.z);
                    preparePoseStack(blockEntity, poseStack, facing, partialTick, isFlipped, blockstate, isHanging);
                    VertexConsumer beamConsumer = multiBufferSource.getBuffer(TheatricalRenderTypes.BEAM);
                    float intensity = (blockEntity.getPrevIntensity() + ((blockEntity.getIntensity()) - blockEntity.getPrevIntensity()) * partialTicks);
                    int color = blockEntity.getColour();
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;
                    int a = (int) (((intensity * 1) / 255f) * 255);
                    poseStack.translate(0, -0.25f, 0.05f);
                    Matrix4f m = poseStack.last().pose();
                    Matrix3f normal = poseStack.last().normal();
                    addVertex(beamConsumer, m, normal, r, g, b, a, 0.34375f, 0.65625f , 0f);
                    addVertex(beamConsumer, m, normal, r, g, b, a,  0.65625f, 0.65625f, 0f);
                    addVertex(beamConsumer, m, normal, r, g, b, a, 0.65625f, 0.34375f,0f);
                    addVertex(beamConsumer, m, normal, r, g, b, a,0.34375f, 0.34375f, 0f);
                    poseStack.popPose();
                }

                @Override
                public Vec3 getPos(float partialTick) {
                    return blockEntity.getBlockPos().getCenter();
                }
            });
        }
    }

    @Override
    public void preparePoseStack(FresnelBlockEntity blockEntity, PoseStack poseStack, Direction facing, float partialTicks, boolean isFlipped, BlockState blockState, boolean isHanging) {
        //#region Fixture Hanging
        poseStack.translate(0.5F, 0, .5F);
        if(isHanging){
            Direction hangDirection = blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0F);
            if(hangDirection.getAxis() != Direction.Axis.Y){
                if(hangDirection.getAxis() == Direction.Axis.Z){
                    if(hangDirection == Direction.SOUTH) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    } else {
                        poseStack.mulPose(Axis.XN.rotationDegrees(90));
                    }
                } else {
                    if(hangDirection == Direction.EAST) {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                    } else {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(-90));
                    }
                }
            } else {
                //TODO: Handle hanging up
            }
            poseStack.translate(0, -0.5, 0F);
        }
        //#endregion
        if(facing.getAxis() == Direction.Axis.X){
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getOpposite().toYRot()));
        }
        poseStack.translate(-0.5F, 0, -.5F);
        if (isHanging) {
            Optional<BlockState> optionalSupport = blockEntity.getSupportingStructure();
            if (optionalSupport.isPresent()) {
                float[] transforms = blockEntity.getFixture().getTransforms(blockState, optionalSupport.get());
                poseStack.translate(transforms[0], transforms[1], transforms[2]);
            } else {
                poseStack.translate(0, 0.19, 0);
            }
        }

        SmoothingState state = smoothingStates.computeIfAbsent(blockEntity, k -> new SmoothingState());

        //#region Model Pan
        float[] pans = blockEntity.getFixture().getPanRotationPosition();
        poseStack.translate(pans[0], pans[1], pans[2]);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.smoothPan));
        poseStack.translate(-pans[0], -pans[1], -pans[2]);
        //#endregion
        //#region Model Tilt
        float[] tilts = blockEntity.getFixture().getTiltRotationPosition();
        poseStack.translate(tilts[0], tilts[1], tilts[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.smoothTilt));
        poseStack.translate(-tilts[0], -tilts[1], -tilts[2]);
        //#endregion
    }
}
