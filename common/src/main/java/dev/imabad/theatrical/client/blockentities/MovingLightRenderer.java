package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.client.LazyRenderers;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import dev.imabad.theatrical.client.blockentities.state.MovingLightRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MovingLightRenderer extends FixtureRenderer<MovingLightBlockEntity, MovingLightRenderState> {
    private BlockStateModel cachedPanModel, cachedTiltModel, cachedStaticModel;

    public MovingLightRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderModel(MovingLightRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging) {
        if(cachedStaticModel == null){
            cachedStaticModel = TheatricalExpectPlatform.getBakedModel(renderState.fixtureRenderState.staticModel);
        }
        if (cachedPanModel == null){
            cachedPanModel = TheatricalExpectPlatform.getBakedModel(renderState.fixtureRenderState.panModel);
        }
        if (cachedTiltModel == null){
            cachedTiltModel = TheatricalExpectPlatform.getBakedModel(renderState.fixtureRenderState.tiltModel);
        }
        setupPoseStack(renderState, poseStack, submitNodeCollector, facing, isFlipped, isHanging,
                (nodeCollector, ps, state) ->
                        minecraftRenderModel(ps, nodeCollector, cachedStaticModel, state.lightCoords,
                                OverlayTexture.NO_OVERLAY, 0),
                (nodeCollector, ps, state) ->
                        minecraftRenderModel(poseStack, submitNodeCollector,  cachedPanModel, renderState.lightCoords,
                                OverlayTexture.NO_OVERLAY, 0),
                (nodeCollector, ps, state) ->
                        minecraftRenderModel(poseStack, submitNodeCollector, cachedTiltModel, renderState.lightCoords,
                                OverlayTexture.NO_OVERLAY, 0));
    }

    private void setupPoseStack(MovingLightRenderState renderState, PoseStack poseStack,
                                SubmitNodeCollector submitNodeCollector, Direction facing,
                                boolean isFlipped, boolean isHanging,
                                @Nullable FixtureRenderCall<MovingLightRenderState> staticRender,
                                @Nullable FixtureRenderCall<MovingLightRenderState> panRender,
                                @Nullable FixtureRenderCall<MovingLightRenderState> tiltRender) {
        //#region Fixture Hanging
        poseStack.translate(0.5F, 0, .5F);
        if(isHanging){
            Direction hangDirection = renderState.hangDirection;
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
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(-0.5F, 0, -.5F);
        if (isHanging) {
            float[] supportTransforms = renderState.supportTransforms;
            poseStack.translate(supportTransforms[0], supportTransforms[1], supportTransforms[2]);
            poseStack.translate(0, -0.08, 0);
        }
        if (isFlipped) {
            poseStack.translate(0.5F, 0.5, .5F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.5F, -0.5, -.5F);
        }
        // Static Model Render
        if(staticRender != null) {
            staticRender.render(submitNodeCollector, poseStack, renderState);
        }
        //#region Model Pan
        float[] pans = renderState.fixtureRenderState.panRotationPosition;
        poseStack.translate(pans[0], pans[1], pans[2]);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.interpolatedPan));
        poseStack.translate(-pans[0], -pans[1], -pans[2]);
        if(panRender != null) {
            panRender.render(submitNodeCollector, poseStack, renderState);
        }
        //#endregion
        //#region Model Tilt
        float[] tilts = renderState.fixtureRenderState.tiltRotationPosition;
        poseStack.translate(tilts[0], tilts[1], tilts[2]);
        if (isFlipped) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(renderState.interpolatedTilt));
        poseStack.translate(-tilts[0], -tilts[1], -tilts[2]);
        if(tiltRender != null) {
            tiltRender.render(submitNodeCollector, poseStack, renderState);
        }
        //#endregion
    }

    @Override
    public void preparePoseStack(MovingLightRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging) {
        setupPoseStack(renderState, poseStack, submitNodeCollector, facing, isFlipped, isHanging, null, null, null);
    }


    @Override
    public void beforeRenderBeam(MovingLightRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging) {
        if(renderState.intensity > 0){
            poseStack.pushPose();
            float intensity = renderState.interpolatedIntensity;
            int color = renderState.colour;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (int) (((intensity * 1) / 255f) * 255);
            poseStack.translate(0, 0f, 0.123f);
            Matrix4f m = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            submitNodeCollector.order(1).submitCustomGeometry(poseStack, TheatricalRenderTypes.BEAM, (pose, beamConsumer) -> {
                addVertex(beamConsumer, m, normal, r, g, b, a, 0.375f, 0.625f , 0f);
                addVertex(beamConsumer, m, normal, r, g, b, a,  0.625f, 0.625f, 0f);
                addVertex(beamConsumer, m, normal, r, g, b, a, 0.625f, 0.375f,0f);
                addVertex(beamConsumer, m, normal, r, g, b, a,0.375f, 0.375f, 0f);
            });
            poseStack.popPose();
        }
    }

    @Override
    public MovingLightRenderState createRenderState() {
        return new MovingLightRenderState();
    }
}
