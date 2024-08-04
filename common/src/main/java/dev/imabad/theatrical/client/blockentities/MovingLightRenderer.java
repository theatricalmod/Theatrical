package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class MovingLightRenderer extends FixtureRenderer<MovingLightBlockEntity> {
    private static BakedModel cachedPanModel, cachedTiltModel, cachedStaticModel;

    public MovingLightRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void doRender(FixtureRenderContext fixtureRenderContext, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks,BlockState blockState, int packedLight, int packedOverlay){
//        poseStack.pushPose();
        if(cachedStaticModel == null){
            cachedStaticModel = TheatricalExpectPlatform.getBakedModel(fixtureRenderContext.fixtureType().getStaticModel());
        }
        if (cachedPanModel == null){
            cachedPanModel = TheatricalExpectPlatform.getBakedModel(fixtureRenderContext.fixtureType().getPanModel());
        }
        if (cachedTiltModel == null){
            cachedTiltModel = TheatricalExpectPlatform.getBakedModel(fixtureRenderContext.fixtureType().getTiltModel());
        }
        //#region Fixture Hanging
        poseStack.translate(0.5F, 0, .5F);
        if(fixtureRenderContext.isHanging()){
            Direction hangDirection = blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0F);
            if(hangDirection.getAxis() != Direction.Axis.Y){
                if(hangDirection.getAxis() == Direction.Axis.Z){
                    if(hangDirection == Direction.SOUTH) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                    } else {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    }
                } else {
                    if(hangDirection == Direction.EAST) {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(-90));
                    } else {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                    }
                }
            } else {
                //TODO: Handle hanging up
            }
            poseStack.translate(0, -0.5, 0F);
        }
        //#endregion
        poseStack.mulPose(Axis.YP.rotationDegrees(fixtureRenderContext.facing().toYRot()));
        poseStack.translate(-0.5F, 0, -.5F);
        if (fixtureRenderContext.isHanging()) {
            Optional<BlockState> optionalSupport = fixtureRenderContext.supportingStructure();
            if (optionalSupport.isPresent()) {
                float[] transforms = fixtureRenderContext.fixtureType().getTransforms(blockState, optionalSupport.get());
                poseStack.translate(transforms[0], transforms[1], transforms[2]);
            } else {
                poseStack.translate(0, 0.19, 0);
            }
            poseStack.translate(0, -0.08, 0);
        }
        if (fixtureRenderContext.isFlipped()) {
            poseStack.translate(0.5F, 0.5, .5F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.5F, -0.5, -.5F);
        }
        // Static Model Render
        minecraftRenderModel(poseStack, vertexConsumer, blockState, cachedStaticModel, packedLight, packedOverlay);
        //#region Model Pan
        float[] pans = fixtureRenderContext.fixtureType().getPanRotationPosition();
        poseStack.translate(pans[0], pans[1], pans[2]);
        int prevPan = fixtureRenderContext.prevPan();
        int pan = fixtureRenderContext.pan();
        poseStack.mulPose(Axis.YP.rotationDegrees((prevPan + (pan - prevPan) * partialTicks)));
        poseStack.translate(-pans[0], -pans[1], -pans[2]);
        minecraftRenderModel(poseStack, vertexConsumer, blockState, cachedPanModel, packedLight, packedOverlay);
        //#endregion
        //#region Model Tilt
        float[] tilts = fixtureRenderContext.fixtureType().getTiltRotationPosition();
        poseStack.translate(tilts[0], tilts[1], tilts[2]);
        int prevTilt = fixtureRenderContext.prevTilt();
        int tilt = fixtureRenderContext.tilt();
        if (fixtureRenderContext.isFlipped()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees((prevTilt + (tilt - prevTilt) * partialTicks)));
        poseStack.translate(-tilts[0], -tilts[1], -tilts[2]);
        minecraftRenderModel(poseStack, vertexConsumer, blockState, cachedTiltModel, packedLight, packedOverlay);
        //#endregion
//        poseStack.popPose();
    }

    public static void doPreparePoseStack(FixtureRenderContext fixtureRenderContext, PoseStack poseStack, float partialTicks, BlockState blockState){
        poseStack.translate(0.5F, 0, .5F);
        if(fixtureRenderContext.isHanging()){
            Direction hangDirection = blockState.getValue(HangableBlock.HANG_DIRECTION);
            poseStack.translate(0, 0.5, 0F);
            if(hangDirection.getAxis() != Direction.Axis.Y){
                if(hangDirection.getAxis() == Direction.Axis.Z){
                    if(hangDirection == Direction.SOUTH) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                    } else {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    }
                } else {
                    if(hangDirection == Direction.EAST) {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(-90));
                    } else {
                        poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                    }
                }
            } else {
                //TODO: Handle hanging up
            }
            poseStack.translate(0, -0.5, 0F);
        }
        //#endregion
        poseStack.mulPose(Axis.YP.rotationDegrees(fixtureRenderContext.facing().toYRot()));
        poseStack.translate(-0.5F, 0, -.5F);
        if (fixtureRenderContext.isHanging()) {
            Optional<BlockState> optionalSupport = fixtureRenderContext.supportingStructure();
            if (optionalSupport.isPresent()) {
                float[] transforms = fixtureRenderContext.fixtureType().getTransforms(blockState, optionalSupport.get());
                poseStack.translate(transforms[0], transforms[1], transforms[2]);
            } else {
                poseStack.translate(0, 0.19, 0);
            }
            poseStack.translate(0, -0.08, 0);
        }
        if (fixtureRenderContext.isFlipped()) {
            poseStack.translate(0.5F, 0.5, .5F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.5F, -0.5, -.5F);
        }
        float[] pans = fixtureRenderContext.fixtureType().getPanRotationPosition();
        poseStack.translate(pans[0], pans[1], pans[2]);
        int prevPan = fixtureRenderContext.prevPan();
        int pan = fixtureRenderContext.pan();
        poseStack.mulPose(Axis.YP.rotationDegrees((prevPan + (pan - prevPan) * partialTicks)));
        poseStack.translate(-pans[0], -pans[1], -pans[2]);
        //#endregion
        //#region Model Tilt
        float[] tilts =  fixtureRenderContext.fixtureType().getTiltRotationPosition();
        poseStack.translate(tilts[0], tilts[1], tilts[2]);
        int prevTilt = fixtureRenderContext.prevTilt();
        int tilt = fixtureRenderContext.tilt();
        if (fixtureRenderContext.isFlipped()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees((prevTilt + (tilt - prevTilt) * partialTicks)));
        poseStack.translate(-tilts[0], -tilts[1], -tilts[2]);
        //#endregion
    }

    @Override
    public void renderModel(FixtureRenderContext fixtureRenderContext, PoseStack poseStack, VertexConsumer vertexConsumer, float partialTicks,BlockState blockState, int packedLight, int packedOverlay) {
        doRender(fixtureRenderContext, poseStack, vertexConsumer, partialTicks, blockState, packedLight, packedOverlay);
    }

    @Override
    public void preparePoseStack(FixtureRenderContext fixtureRenderContext, PoseStack poseStack, float partialTicks, BlockState blockState) {
        doPreparePoseStack(fixtureRenderContext, poseStack, partialTicks, blockState);
    }

}
