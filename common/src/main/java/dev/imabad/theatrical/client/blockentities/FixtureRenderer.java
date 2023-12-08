package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.config.TheatricalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FixtureRenderer<T extends BaseLightBlockEntity> implements BlockEntityRenderer<T> {
    private final Double beamOpacity = TheatricalConfig.INSTANCE.CLIENT.beamOpacity;
    private BakedModel cachedPanModel, cachedTiltModel, cachedStaticModel;

    public FixtureRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout());
        BlockState blockState = blockEntity.getBlockState();
        boolean isFlipped = blockEntity.isUpsideDown();
        boolean isHanging = ((HangableBlock) blockState.getBlock()).isHanging(blockEntity.getLevel(), blockEntity.getBlockPos());
        renderLight(blockEntity, poseStack, vertexConsumer, blockState.getValue(MovingLightBlock.FACING), partialTick, isFlipped, blockState, isHanging, packedLight, packedOverlay);
        if(blockEntity.getIntensity() > 0){
            VertexConsumer beamConsumer = multiBufferSource.getBuffer(RenderType.lightning());
//            poseStack.translate(0.5, 0.5, 0.5);
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(blockEntity.getFixture().getDefaultRotation()));
//            poseStack.translate(-0.5, -0.5, -0.5);
            poseStack.translate(blockEntity.getFixture().getBeamStartPosition()[0], blockEntity.getFixture().getBeamStartPosition()[1], blockEntity.getFixture().getBeamStartPosition()[2]);
            float intensity = (blockEntity.getPrevIntensity() + ((blockEntity.getIntensity()) - blockEntity.getPrevIntensity()) * partialTick);
            int color = blockEntity.calculatePartialColour(partialTick);
            renderLightBeam(beamConsumer, poseStack, blockEntity, partialTick, (float) ((intensity * beamOpacity) / 255f), blockEntity.getFixture().getBeamWidth(), (float) blockEntity.getDistance(), color);
        }
        poseStack.popPose();
    }

    public void renderLight(T blockEntity, PoseStack poseStack, VertexConsumer vertexConsumer, Direction facing, float partialTicks, boolean isFlipped, BlockState blockState, boolean isHanging, int packedLight, int packedOverlay) {
        if (cachedPanModel == null){
            cachedPanModel = TheatricalExpectPlatform.getBakedModel(blockEntity.getFixture().getPanModel());
        }
        if (cachedTiltModel == null){
            cachedTiltModel = TheatricalExpectPlatform.getBakedModel(blockEntity.getFixture().getTiltModel());
        }
        if(cachedStaticModel == null){
            cachedStaticModel = TheatricalExpectPlatform.getBakedModel(blockEntity.getFixture().getStaticModel());
        }
        if (blockEntity.getFixture().getHangType() == HangType.BRACE_BAR && isHanging) {
            poseStack.translate(0, 0.175, 0);
        }
        if (blockEntity.getFixture().getHangType() == HangType.HOOK_BAR && isHanging) {
            poseStack.translate(0, 0.05, 0);
        }
        poseStack.translate(0.5F, 0, .5F);
        if(facing.getAxis() == Direction.Axis.Z) {
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.getOpposite().toYRot()));
        } else  {
            poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        }
        poseStack.translate(-0.5F, 0, -.5F);
        if (blockEntity.getFixture().getHangType() == HangType.BRACE_BAR && isHanging) {
            if (blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().above()).getBlock() instanceof Support support) {
                float[] transforms = support.getHookTransforms(blockEntity.getLevel(), blockEntity.getBlockPos(), facing);
                poseStack.translate(transforms[0], transforms[1], transforms[2]);
            } else {
                poseStack.translate(0, 0.19, 0);
            }
        }
        if (isFlipped) {
            poseStack.translate(0.5F, 0.5, .5F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.5F, -0.5, -.5F);
        }
        if (blockEntity.getFixture().getHangType() == HangType.BRACE_BAR && isHanging) {
            poseStack.translate(0, 0.19, 0);
        }
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), vertexConsumer, blockState, cachedStaticModel, 1, 1, 1, packedLight, packedOverlay);
        if (blockEntity.getFixture().getHangType() == HangType.BRACE_BAR && isHanging) {
            poseStack.translate(0, 0.19, 0);
        }
        float[] pans = blockEntity.getFixture().getPanRotationPosition();
//        float[] pans = new float[]{0.5F, 0, 0.41F};
        poseStack.translate(pans[0], pans[1], pans[2]);
        poseStack.mulPose(Axis.YP.rotationDegrees((blockEntity.getPrevPan() + ((blockEntity.getPan()) - blockEntity.getPrevPan()) * partialTicks)));
        poseStack.translate(-pans[0], -pans[1], -pans[2]);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), vertexConsumer, blockState, cachedPanModel, 1, 1, 1, packedLight, packedOverlay);
        float[] tilts = blockEntity.getFixture().getTiltRotationPosition();
//        float[] tilts = new float[]{0.5F, 0.3F, 0.39F};
        poseStack.translate(tilts[0], tilts[1], tilts[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees((blockEntity.getPrevTilt() + ((blockEntity.getTilt()) - blockEntity.getPrevTilt()) * partialTicks)));
        poseStack.translate(-tilts[0], -tilts[1], -tilts[2]);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), vertexConsumer, blockState, cachedTiltModel, 1, 1, 1, packedLight, packedOverlay);
    }

    public void renderLightBeam(VertexConsumer builder, PoseStack stack, T tileEntityFixture, float partialTicks, float alpha, float beamSize, float length, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        Matrix4f m = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        float endMultiplier = beamSize * tileEntityFixture.getFocus();
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
    }

    private void addVertex(VertexConsumer builder, Matrix4f matrix4f, Matrix3f matrix3f, int r, int g, int b, int a, float x, float y, float z) {
        builder.vertex(matrix4f, x, y, z).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return TheatricalConfig.INSTANCE.CLIENT.renderDistance;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        return true;
    }
}
