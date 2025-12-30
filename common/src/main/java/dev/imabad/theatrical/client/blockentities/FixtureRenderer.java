package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.client.LazyRenderers;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import dev.imabad.theatrical.client.blockentities.state.FixtureRendererState;
import dev.imabad.theatrical.config.TheatricalConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public abstract class FixtureRenderer<T extends BaseLightBlockEntity, S extends FixtureRendererState> implements BlockEntityRenderer<T, S> {
    private final Double beamOpacity = TheatricalConfig.INSTANCE.CLIENT.beamOpacity;

    public FixtureRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void submit(S renderState, PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        boolean isFlipped = renderState.isFlipped;
        boolean isHanging = renderState.isHanging;
        Direction facing = renderState.facing;
        renderModel(renderState, poseStack, submitNodeCollector, facing, isFlipped, isHanging);
        beforeRenderBeam(renderState, poseStack, submitNodeCollector, facing, isFlipped, isHanging);
        if(shouldRenderBeam(renderState)){
            LazyRenderers.addLazyRender(new LazyRenderers.LazyRenderer() {
                @Override
                public void render(PoseStack lazyRenderPoseStack, CameraRenderState camera) {
                    lazyRenderPoseStack.pushPose();
                    Vec3 offset = Vec3.atLowerCornerOf(renderState.blockPos).subtract(camera.pos);
                    lazyRenderPoseStack.translate(offset.x, offset.y, offset.z);
                    preparePoseStack(renderState, lazyRenderPoseStack, submitNodeCollector, facing, isFlipped, isHanging);
                    lazyRenderPoseStack.translate(renderState.fixtureRenderState.beamStartPosition[0], renderState.fixtureRenderState.beamStartPosition[1], renderState.fixtureRenderState.beamStartPosition[2]);
                    float intensity = renderState.intensity;
                    int color = renderState.colour;
                    if(color != 0) {
                        renderLightBeam(submitNodeCollector, lazyRenderPoseStack, renderState,
                                (float) ((intensity * beamOpacity) / 255f));
                    }
                    lazyRenderPoseStack.popPose();
                }

                @Override
                public Vec3 getPos() {
                    return renderState.blockPos.getCenter();
                }
            });
        }
        poseStack.popPose();
    }

    @Override
    public void extractRenderState(T blockEntity, S renderState, float partialTick, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, vec3, crumblingOverlay);
        renderState.isFlipped = blockEntity.isUpsideDown();
        BlockState blockState = blockEntity.getBlockState();
        renderState.isHanging = ((HangableBlock) blockState.getBlock()).isHanging(blockEntity.getLevel(), blockEntity.getBlockPos());
        renderState.facing = blockState.getValue(BaseLightBlock.FACING);
        renderState.intensity = blockEntity.getIntensity();
        renderState.interpolatedIntensity = (blockEntity.getPrevIntensity() + (blockEntity.getIntensity() - blockEntity.getPrevIntensity()) * partialTick);
        renderState.fixtureRenderState.extractFromFixture(blockEntity.getFixture());
        renderState.colour = blockEntity.getColour();
        renderState.focus = blockEntity.getFocus();
        renderState.distance = (float) blockEntity.getDistance();
        renderState.hangDirection = blockState.getValue(HangableBlock.HANG_DIRECTION);
        renderState.supportTransforms = blockEntity.getSupportingStructureTransforms();
        renderState.tilt = blockEntity.getTilt();
        renderState.prevTilt = blockEntity.getPrevTilt();
        renderState.pan = blockEntity.getPan();
        renderState.prevPan = blockEntity.getPrevPan();

        renderState.interpolatedPan = (renderState.prevPan + (renderState.pan - renderState.prevPan) * partialTick);
        renderState.interpolatedTilt = (renderState.prevTilt + (renderState.tilt - renderState.prevTilt) * partialTick);
    }

    public abstract void renderModel(S renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing, boolean isFlipped, boolean isHanging);

    public abstract void preparePoseStack(S renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing,  boolean isFlipped, boolean isHanging);

    public void beforeRenderBeam(S renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Direction facing,  boolean isFlipped, boolean isHanging) {}

    public boolean shouldRenderBeam(S renderState){
        return renderState.intensity > 0 && renderState.fixtureRenderState.hasBeam;
    }

    protected void minecraftRenderModel(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, BlockStateModel model, int lightCoords, int overlayCoords, int outlineColor){
        submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), model, 1f, 1f,1f, lightCoords, overlayCoords, outlineColor);
    }

    protected void renderLightBeam(VertexConsumer builder, PoseStack stack, T tileEntityFixture, float partialTicks, float alpha, float beamSize, float length, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        Matrix4f m = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        length += 0.5f;
        float endMultiplier = 1 + tileEntityFixture.getFocus()*length*0.03f;
        addVertex(builder, m, normal, r, g, b, 0, beamSize * endMultiplier, beamSize * endMultiplier, -length);
        addVertex(builder, m, normal, r, g, b, a,  beamSize, beamSize, 0);
        addVertex(builder, m, normal, r, g, b, a, beamSize, -beamSize, 0);
        addVertex(builder, m, normal, r, g, b, 0,beamSize * endMultiplier, -beamSize * endMultiplier, -length);

            addVertex(builder, m, normal, r, g, b, 0, -beamSize * endMultiplier, -beamSize * endMultiplier, -length);
            addVertex(builder, m, normal, r, g, b, a, -beamSize, -beamSize, 0);
            addVertex(builder, m, normal, r, g, b, a, -beamSize, beamSize, 0);
            addVertex(builder, m, normal, r, g, b, 0, -beamSize * endMultiplier, beamSize * endMultiplier, -length);

            addVertex(builder, m, normal, r, g, b, 0, -beamSize * endMultiplier, beamSize * endMultiplier, -length);
            addVertex(builder, m, normal, r, g, b, a, -beamSize, beamSize, 0);
            addVertex(builder, m, normal, r, g, b, a, beamSize, beamSize, 0);
            addVertex(builder, m, normal, r, g, b, 0, beamSize * endMultiplier, beamSize * endMultiplier, -length);

            addVertex(builder, m, normal, r, g, b, 0, beamSize * endMultiplier, -beamSize * endMultiplier, -length);
            addVertex(builder, m, normal, r, g, b, a, beamSize, -beamSize, 0);
            addVertex(builder, m, normal, r, g, b, a, -beamSize, -beamSize, 0);
            addVertex(builder, m, normal, r, g, b, 0, -beamSize * endMultiplier, -beamSize * endMultiplier, -length);
        });
    }

    protected void addVertex(VertexConsumer builder, Matrix4f matrix4f, Matrix3f matrix3f, int r, int g, int b, int a, float x, float y, float z) {
        builder.addVertex(matrix4f, x, y, z)
                        .setColor(r,g,b,a);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return TheatricalConfig.INSTANCE.CLIENT.renderDistance;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(cameraPos.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }

    public interface FixtureRenderCall<S extends FixtureRendererState> {
        void render(SubmitNodeCollector nodeCollector, PoseStack poseStack, S renderState);
    }
}
