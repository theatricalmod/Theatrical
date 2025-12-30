package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.client.TheatricalRenderTypes;
import dev.imabad.theatrical.client.blockentities.state.BasicLightingConsoleRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class BasicLightingConsoleRenderer implements BlockEntityRenderer<BasicLightingDeskBlockEntity, BasicLightingConsoleRenderState> {
    public BasicLightingConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    public float convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
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


    public void renderLine(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, double x, double y){
        poseStack.pushPose();
        poseStack.translate(x / 16D, 3 / 16D, y / 16D);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, vertexConsumer) -> {
            Matrix4f m = pose.pose();
            vertexConsumer.addVertex(m, 0, 0, 0).setColor(0, 0, 0, 255).setNormal(0, 0, 0)
                    .setLineWidth(1f);
            vertexConsumer.addVertex(m, 0, 0, -(3 / 16F)).setColor(0, 0, 0, 255).setNormal(0, 0, 0)
                    .setLineWidth(1f);
        });
        poseStack.popPose();
    }

    public void renderFader(SubmitNodeCollector submitNodeCollector, PoseStack stack, double x, double baseY, double faderY){
        stack.pushPose();
        float height = 0.4F / 16F;
        float width = 0.6F / 16F;

        stack.translate((x / 16D) - width / 2, 3 / 16D, (baseY + faderY) / 16D);
        submitNodeCollector.submitCustomGeometry(stack, TheatricalRenderTypes.FADER, (pose, builder) -> {
            Matrix4f m = pose.pose();
            //right
            builder.addVertex(m, width, height, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, width, height, width).setColor(0, 0, 0,255);
            builder.addVertex(m, width, 0, width).setColor(0, 0, 0,255);
            builder.addVertex(m, width, 0, 0).setColor(0, 0, 0,255);

            //front
            builder.addVertex(m, 0, 0, width).setColor(0, 0, 0,255);
            builder.addVertex(m, width, 0, width).setColor(0, 0, 0,255);
            builder.addVertex(m, width, height, width).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, height, width).setColor(0, 0, 0,255);

            //left
            builder.addVertex(m, 0, 0, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, 0, width).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, height, width).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, height, 0).setColor(0, 0, 0,255);

            //back
            builder.addVertex(m, 0, height, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, width, height, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, width, 0, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, 0, 0).setColor(0, 0, 0,255);

            //bottom
            builder.addVertex(m, width, 0, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, width, 0, width).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, 0, width).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, 0, 0).setColor(0, 0, 0,255);

            //Top
            builder.addVertex(m, 0, height, 0).setColor(0, 0, 0,255);
            builder.addVertex(m, 0, height, width).setColor(0, 0, 0,255);
            builder.addVertex(m, width, height, width).setColor(0, 0, 0,255);
            builder.addVertex(m, width, height, 0).setColor(0, 0, 0,255);
        });

        stack.popPose();
    }

    @Override
    public BasicLightingConsoleRenderState createRenderState() {
        return new BasicLightingConsoleRenderState();
    }

    @Override
    public void submit(BasicLightingConsoleRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        BlockState blockState = renderState.blockState;
        Direction blockDirection = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        poseStack.translate(0.5, 0.5, 0.5);
        if(blockDirection.getAxis() == Direction.Axis.X){
            blockDirection = blockDirection.getOpposite();
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(blockDirection.toYRot())); //idk what this is
        poseStack.translate(-0.5, -0.5, -0.5);
        double startX = 1.5;
        byte[] faders = renderState.faders;
        for(int i = 0; i < faders.length; i++){
            double baseY = 5.4;
            if(i >= 6){
                baseY += (i / 6) * 7;
            }
            int faderNumber = i - ((i / 6) * 6);
            renderLine(submitNodeCollector, poseStack, startX + (faderNumber * 1.2), baseY);
        }
        renderLine(submitNodeCollector, poseStack, 14.5, 5.4);
        for(int i = 0; i < faders.length; i++){
            double baseY = 5.4;
            if(i >= 6){
                baseY += (i / 6) * 7;
            }
            int faderNumber = i - ((i / 6) * 6);
            renderFader(submitNodeCollector, poseStack, startX + (faderNumber * 1.2), baseY, -((convertByteToInt(faders[i]) / 255) * 3));
        }
        renderFader(submitNodeCollector, poseStack, 14.5, 5.4, -((convertByteToInt(renderState.grandMaster) / 255) * 3));
        poseStack.popPose();
    }

    @Override
    public void extractRenderState(BasicLightingDeskBlockEntity blockEntity, BasicLightingConsoleRenderState renderState, float f, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, f, vec3, crumblingOverlay);
        renderState.faders = blockEntity.getFaders();
        renderState.grandMaster = blockEntity.getGrandMaster();
    }
}
