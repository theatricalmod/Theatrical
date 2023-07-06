package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

public class CableRenderer implements BlockEntityRenderer<CableBlockEntity> {


    public CableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lightning());

        for(Direction side : Direction.values()){
            poseStack.pushPose();
            if(blockEntity.hasSide(side)){
                if(side == Direction.UP) {
                    poseStack.translate(0.5, 0.5, 0);
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
                    poseStack.translate(-0.5, -0.5, 0);
                } else if(side == Direction.EAST){
                    poseStack.translate(0.5, 0.5, 0);
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(90));
                    poseStack.translate(-0.5, -0.5, 0);
                } else if(side == Direction.WEST){
                    poseStack.translate(0.5, 0.5, 0);
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90));
                    poseStack.translate(-0.5, -0.5, 0);
                } else if(side == Direction.SOUTH){
                    poseStack.translate(0, 0.5, 0.5);
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
                    poseStack.translate(0, -0.5, -0.5);
                }else if(side == Direction.NORTH){
                    poseStack.translate(0, 0.5, 0.5);
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
                    poseStack.translate(0, -0.5, -0.5);
                }
//                poseStack.mulPose(Vector3f.YP.rotationDegrees(side.toYRot()));
                var cableWidth = 0.05f;
                var cableHeight = 0.05f;
                var cableLength = 0.8f;
                poseStack.translate(0.1, 0, 0);
                renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                poseStack.translate(-0.5, -0.5, -0.5);
                poseStack.translate(0.1, 0, -0.1);
                renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    private void addVertex(VertexConsumer builder, Matrix4f matrix4f, Matrix3f matrix3f, int r, int g, int b, int a, float x, float y, float z) {
        builder.vertex(matrix4f, x, y, z).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).endVertex();
    }

    private void renderCableLength(VertexConsumer vertexConsumer, PoseStack poseStack, float cableLength, float cableWidth, float cableHeight){
        Matrix4f m = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int r = 255, g = 255, b = 255, a = 255;
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, 0, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, cableHeight, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, cableHeight, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, 0, 0.5f - cableWidth);

        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, 0, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, cableHeight, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, cableHeight, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, 0, 0.5f + cableWidth);

        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, cableHeight, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, cableHeight, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, cableHeight, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, cableHeight, 0.5f + cableWidth);

        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, 0, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, cableHeight, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, cableHeight, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, cableLength, 0, 0.5f + cableWidth);

        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, 0, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, cableHeight, 0.5f + cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, cableHeight, 0.5f - cableWidth);
        addVertex(vertexConsumer, m, normal, r,g,b,a, 0, 0, 0.5f - cableWidth);
    }
}
