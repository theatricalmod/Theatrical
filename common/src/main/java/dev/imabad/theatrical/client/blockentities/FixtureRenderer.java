package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public abstract class FixtureRenderer<T extends BaseLightBlockEntity> implements BlockEntityRenderer<T> {
    private final Double beamOpacity = TheatricalConfig.INSTANCE.CLIENT.beamOpacity;

    public FixtureRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout());
        BlockState blockState = blockEntity.getBlockState();
        boolean isFlipped = blockEntity.isUpsideDown();
        boolean isHanging = ((HangableBlock) blockState.getBlock()).isHanging(blockEntity.getLevel(), blockEntity.getBlockPos());
        Direction facing = blockState.getValue(MovingLightBlock.FACING);
        renderModel(blockEntity, poseStack, vertexConsumer, facing, partialTick, isFlipped, blockState, isHanging, packedLight, packedOverlay);
        beforeRenderBeam(blockEntity, poseStack, vertexConsumer, multiBufferSource, facing, partialTick, isFlipped, blockState, isHanging, packedLight, packedOverlay);
        if(shouldRenderBeam(blockEntity)){
            VertexConsumer beamConsumer = multiBufferSource.getBuffer(RenderType.lightning());
            poseStack.translate(blockEntity.getFixture().getBeamStartPosition()[0], blockEntity.getFixture().getBeamStartPosition()[1], blockEntity.getFixture().getBeamStartPosition()[2]);
            float intensity = (blockEntity.getPrevIntensity() + ((blockEntity.getIntensity()) - blockEntity.getPrevIntensity()) * partialTick);
            int color = blockEntity.calculatePartialColour(partialTick);
            renderLightBeam(beamConsumer, poseStack, blockEntity, partialTick, (float) ((intensity * beamOpacity) / 255f), blockEntity.getFixture().getBeamWidth(), (float) blockEntity.getDistance(), color);
        }
        poseStack.popPose();
    }

    public abstract void renderModel(T blockEntity, PoseStack poseStack, VertexConsumer vertexConsumer, Direction facing, float partialTicks, boolean isFlipped, BlockState blockState, boolean isHanging, int packedLight, int packedOverlay);

    public void beforeRenderBeam(T blockEntity, PoseStack poseStack, VertexConsumer vertexConsumer,
                                 MultiBufferSource multiBufferSource, Direction facing, float partialTicks,
                                 boolean isFlipped, BlockState blockstate, boolean isHanging, int packedLight,
                                 int packedOverlay) {}

    public boolean shouldRenderBeam(T blockEntity){
        return blockEntity.getIntensity() > 0 && blockEntity.getFixture().hasBeam();
    }

    protected void minecraftRenderModel(PoseStack poseStack, VertexConsumer vertexConsumer, BlockState blockState, BakedModel model, int packedLight, int packedOverlay){
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), vertexConsumer, blockState, model, 1, 1, 1, packedLight, packedOverlay);
    }

    protected void renderLightBeam(VertexConsumer builder, PoseStack stack, T tileEntityFixture, float partialTicks, float alpha, float beamSize, float length, int color) {
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

    protected void addVertex(VertexConsumer builder, Matrix4f matrix4f, Matrix3f matrix3f, int r, int g, int b, int a, float x, float y, float z) {
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
