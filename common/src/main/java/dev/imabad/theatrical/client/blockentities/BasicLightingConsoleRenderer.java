package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BasicLightingConsoleRenderer implements BlockEntityRenderer<BasicLightingDeskBlockEntity> {
    public BasicLightingConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }


    public float convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }
    @Override
    public void render(BasicLightingDeskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        BlockState blockState = blockEntity.getBlockState();
        Direction blockDirection = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        poseStack.translate(0.5, 0.5, 0.5);
        if(blockDirection.getAxis() == Direction.Axis.X){
            blockDirection = blockDirection.getOpposite();
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(blockDirection.toYRot())); //idk what this is
        poseStack.translate(-0.5, -0.5, -0.5);
        double startX = 1.5;
        byte[] faders = blockEntity.getFaders();
        VertexConsumer linesVertexBuilder = buffer.getBuffer(RenderType.lines());
        for(int i = 0; i < faders.length; i++){
            double baseY = 5.4;
            if(i >= 6){
                baseY += (i / 6) * 7;
            }
            int faderNumber = i - ((i / 6) * 6);
            renderLine(poseStack, startX + (faderNumber * 1.2), baseY, linesVertexBuilder);
        }
        renderLine(poseStack, 14.5, 5.4, linesVertexBuilder);
        VertexConsumer iVertexBuilder = buffer.getBuffer(TheatricalRenderTypes.FADER);
        for(int i = 0; i < faders.length; i++){
            double baseY = 5.4;
            if(i >= 6){
                baseY += (i / 6) * 7;
            }
            int faderNumber = i - ((i / 6) * 6);
            renderFader(poseStack, startX + (faderNumber * 1.2), baseY, -((convertByteToInt(faders[i]) / 255) * 3), iVertexBuilder);
        }
        renderFader(poseStack, 14.5, 5.4, -((convertByteToInt(blockEntity.getGrandMaster()) / 255) * 3), iVertexBuilder);
//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        renderStep(poseStack, blockEntity, buffer, packedLight);
//        renderCurrentMode(poseStack, blockEntity, buffer, packedLight);
        poseStack.popPose();
    }
    public void renderStep(PoseStack stack, BasicLightingDeskBlockEntity tileEntityBasicLightingControl, MultiBufferSource buffer,  int combinedLightIn){
        stack.pushPose();
//        FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
        Font font = Minecraft.getInstance().font;
        stack.translate(10.7 /16D, 3 /16D, 9.3 / 16D);
        stack.scale(0.005F, -0.005F, 0.005F);
        stack.mulPose(Axis.XP.rotationDegrees(90F));
        font.drawInBatch("STEP", 0 , 0, -1, false, stack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, combinedLightIn);
//        fontrenderer.drawString(stack,"Step: " + tileEntityBasicLightingControl.getCurrentStep(), 0, 0, 0x000000);
        stack.popPose();
    }

    public void renderCurrentMode(PoseStack stack, BasicLightingDeskBlockEntity tileEntityBasicLightingControl, MultiBufferSource buffer, int combinedLightIn){
        stack.pushPose();
//        FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
        Font font = Minecraft.getInstance().font;
        stack.translate(10.4 /16D, 3 /16D, 8.3 / 16D);
        stack.scale(0.003F, -0.003F, 0.003F);
        stack.mulPose(Axis.XP.rotationDegrees(90F));
        font.drawInBatch(tileEntityBasicLightingControl.isRunMode() ? "Run mode" : "Program mode", 0 , 0, 0x000000, false, stack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0x000000, combinedLightIn);
//        fontrenderer.drawString(stack,"Step: " + tileEntityBasicLightingControl.getCurrentStep(), 0, 0, 0x000000);
        stack.popPose();
    }


    public void renderLine(PoseStack stack, double x, double y, VertexConsumer vertexBuilder){
        stack.pushPose();
        Matrix4f m = stack.last().pose();
        stack.translate(x / 16D, 3 / 16D,  y / 16D);
        vertexBuilder.vertex(m, 0, 0, 0).color(0, 0, 0, 255).normal(0, 0, 0).endVertex();
        vertexBuilder.vertex(m, 0, 0, -(3 / 16F)).color(0, 0, 0, 255).normal(0, 0, 0).endVertex();
        stack.popPose();
    }

    public void renderFader(PoseStack stack, double x, double baseY, double faderY, VertexConsumer builder){
        stack.pushPose();
        Matrix4f m = stack.last().pose();
        float height = 0.4F / 16F;
        float width = 0.6F / 16F;

        stack.translate((x / 16D) - width / 2, 3 / 16D, (baseY + faderY) / 16D);

        //right
        builder.vertex(m, width, height, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, height, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, 0, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, 0, 0).color(0, 0, 0,255).endVertex();

        //front
        builder.vertex(m, 0, 0, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, 0, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, height, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, height, width).color(0, 0, 0,255).endVertex();

        //left
        builder.vertex(m, 0, 0, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, 0, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, height, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, height, 0).color(0, 0, 0,255).endVertex();

        //back
        builder.vertex(m, 0, height, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, height, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, 0, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, 0, 0).color(0, 0, 0,255).endVertex();

        //bottom
        builder.vertex(m, width, 0, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, 0, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, 0, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, 0, 0).color(0, 0, 0,255).endVertex();

        //Top
        builder.vertex(m, 0, height, 0).color(0, 0, 0,255).endVertex();
        builder.vertex(m, 0, height, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, height, width).color(0, 0, 0,255).endVertex();
        builder.vertex(m, width, height, 0).color(0, 0, 0,255).endVertex();

        stack.popPose();
    }
}
