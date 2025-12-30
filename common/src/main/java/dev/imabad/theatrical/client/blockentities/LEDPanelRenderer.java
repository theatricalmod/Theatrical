package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.blockentities.light.LEDPanelBlockEntity;
import dev.imabad.theatrical.client.LazyRenderers;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import dev.imabad.theatrical.client.blockentities.state.FixtureRendererState;
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

public class LEDPanelRenderer extends FixtureRenderer<LEDPanelBlockEntity, FixtureRendererState> {
    private BlockStateModel cachedStaticModel;
    public LEDPanelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderModel(FixtureRendererState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging) {
        if(cachedStaticModel == null){
            cachedStaticModel = TheatricalExpectPlatform.getBakedModel(renderState.fixtureRenderState.staticModel);
        }
        setupPoseStack(renderState, poseStack, submitNodeCollector, facing, isFlipped, isHanging,
                (nodeCollector, ps, state) ->
                        minecraftRenderModel(ps, nodeCollector, cachedStaticModel, state.lightCoords,
                                OverlayTexture.NO_OVERLAY, 0));
    }

    @Override
    public void preparePoseStack(FixtureRendererState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging) {
        setupPoseStack(renderState, poseStack, submitNodeCollector, facing, isFlipped, isHanging, null);
    }

    private void setupPoseStack(FixtureRendererState renderState, PoseStack poseStack,
                                SubmitNodeCollector submitNodeCollector, Direction facing,
                                boolean isFlipped, boolean isHanging,
                                @Nullable FixtureRenderCall<FixtureRendererState> staticRender) {
        //#region Fixture Hanging
        poseStack.translate(0.5F, 0, .5F);
        if(isHanging){
            Direction hangDirection = renderState.hangDirection;
            poseStack.translate(0, 0.5, 0F);
            if(hangDirection.getAxis() != Direction.Axis.Y){
                if(hangDirection.getAxis() == Direction.Axis.Z){
                    if(hangDirection == Direction.SOUTH) {
//                        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                        poseStack.mulPose(Axis.XN.rotationDegrees(180));
                    } else {
                        poseStack.mulPose(Axis.XN.rotationDegrees(180));
                    }
                }
            } else {
                if(hangDirection == Direction.UP){
                    switch (facing){
                        case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
                        case SOUTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
                        case WEST -> {
                            poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                            poseStack.mulPose(Axis.XP.rotationDegrees(90));
                        }
                        case EAST -> {
                            poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                            poseStack.mulPose(Axis.XP.rotationDegrees(90));
                        }
                    }
                } else if(hangDirection == Direction.DOWN){
                    switch (facing){
                        case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
                        case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
                        case WEST -> {
                            poseStack.mulPose(Axis.XP.rotationDegrees(90));
                            poseStack.mulPose(Axis.YN.rotationDegrees(90));
                        }
                        case EAST -> {
                            poseStack.mulPose(Axis.XP.rotationDegrees(90));
                            poseStack.mulPose(Axis.YP.rotationDegrees(90));
                        }
                    }
                }
            }
            poseStack.translate(0, -0.5, 0F);
        }
        //#endregion
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(-0.5F, 0, -.5F);
        if (isHanging) {
            float[] supportTransforms = renderState.supportTransforms;
            poseStack.translate(supportTransforms[0], supportTransforms[1], supportTransforms[2]);
        }
        // Static Model Render
        if(staticRender != null) {
            staticRender.render(submitNodeCollector, poseStack, renderState);
        }
    }
    @Override
    public void beforeRenderBeam(FixtureRendererState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging) {
        if(renderState.intensity > 0){
            poseStack.pushPose();
            float intensity = renderState.interpolatedIntensity;
            int color = renderState.colour;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (int) (((intensity * 1) / 255f) * 255);
            poseStack.translate(0, 0f, -0.01f);
            Matrix4f m = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            submitNodeCollector.submitCustomGeometry(poseStack, TheatricalRenderTypes.BEAM, (pose, beamConsumer) -> {
                addVertex(beamConsumer, m, normal, r, g, b, a, 0, 1 , 0);
                addVertex(beamConsumer, m, normal, r, g, b, a,  1, 1, 0);
                addVertex(beamConsumer, m, normal, r, g, b, a, 1, 0, 0);
                addVertex(beamConsumer, m, normal, r, g, b, a,0, 0, 0);
            });
            poseStack.popPose();
        }
    }

    @Override
    public FixtureRendererState createRenderState() {
        return new FixtureRendererState();
    }
}
