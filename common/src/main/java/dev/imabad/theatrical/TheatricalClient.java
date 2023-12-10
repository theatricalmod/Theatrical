package dev.imabad.theatrical;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.client.blockentities.FixtureRenderer;
import dev.imabad.theatrical.protocols.artnet.ArtNetManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TheatricalClient {

    public static Set<BlockPos> DEBUG_BLOCKS = new HashSet<>();
    private static ArtNetManager artNetManager;
    public static void init() {
        BlockEntityRendererRegistry.register(BlockEntities.MOVING_LIGHT.get(), FixtureRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.LED_FRESNEL.get(), FixtureRenderer::new);
//        BlockEntityRendererRegistry.register(BlockEntities.CABLE.get(), CableRenderer::new);
        artNetManager = new ArtNetManager();
    }

    public static ArtNetManager getArtNetManager(){
        return artNetManager;
    }

    public static float[] renderThings(BlockPos MY_BLOCK, VertexConsumer consumer, PoseStack poseStack, BaseLightBlockEntity be, MultiBufferSource multiBuffer){
        Vec3 viewVector = BaseLightBlockEntity.rayTraceDir(be);
        double distance = 25;
        Vec3 origin = new Vec3(0.5, 0.5, 0.5);
        Vec3 destination = origin.add(viewVector.x * distance, viewVector.y * distance, viewVector.z * distance);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        consumer.vertex(matrix4f, (float) origin.x, (float) origin.y, (float) origin.z).color(255, 255, 255, 255).normal(matrix3f, 0.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(matrix4f, (float) destination.x, (float) destination.y, (float) destination.z).color(255, 255, 255, 255).normal(matrix3f, 0.0f, 0.0f, 0.0f).endVertex();
        return new float[]{be.getTilt(), be.getPan()};
    }

    public static void renderWorldLast(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta){
        Minecraft mc = Minecraft.getInstance();
        if(mc.getDebugOverlay().showDebugScreen()){
            Vec3 cameraPos = camera.getPosition();
            //#region translateToCamera
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            for(BlockPos MY_BLOCK : DEBUG_BLOCKS) {
                //#region translateToBlock
                poseStack.pushPose();
                poseStack.translate(MY_BLOCK.getX(), MY_BLOCK.getY(), MY_BLOCK.getZ());
                //#region MainRender
                poseStack.pushPose();
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);
                LevelRenderer.renderLineBox(poseStack, buffer, AABB.ofSize(new Vec3(0, 0, 0), 1d, 1d, 1d), 1, 1, 1, 1);
                poseStack.popPose();
                float[] values = null;
                if (Minecraft.getInstance().level.getBlockEntity(MY_BLOCK) != null) {
                    values = renderThings(MY_BLOCK, buffer, poseStack, (BaseLightBlockEntity) Minecraft.getInstance().level.getBlockEntity(MY_BLOCK), bufferSource);
                }
                bufferSource.endBatch(RenderType.lines());
                if(values != null) {
                    poseStack.pushPose();
                    poseStack.translate(-0.5, 1.25, 0.5);
                    poseStack.scale(0.025f, 0.025f, 0.025f);
                    BlockState blockState = Minecraft.getInstance().level.getBlockState(MY_BLOCK);
                    Direction opposite = blockState.getValue(MovingLightBlock.HANG_DIRECTION).getOpposite();
                    poseStack.mulPose(Axis.XP.rotationDegrees(180));
                    poseStack.mulPose(Axis.YP.rotationDegrees(opposite.toYRot()));
                    Minecraft.getInstance().font.drawInBatch(String.format("OG Tilt: %s OG Pan: %s", values[0], values[1]), 0, -10, 0xffffff, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0, false);
                    Minecraft.getInstance().font.drawInBatch(String.format("DIR: %s", blockState.getValue(MovingLightBlock.FACING)), 0, -30, 0xffffff, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0, false);
                    poseStack.popPose();
                }
                //#endregion
                poseStack.popPose();
                //#endregion
                poseStack.popPose();
            }
            //#endregion
            poseStack.popPose();
        }
    }

    public static Color getRandomColor(UUID id) {

        byte[] bytes = UUID2Bytes(id);

        int r= Math.abs(bytes[0]);
        int g = Math.abs(bytes[1]);
        int b = Math.abs(bytes[2]);

        return new Color(r, g, b);
    }

    public static byte[] UUID2Bytes(UUID uuid) {

        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        return ByteBuffer.allocate(16).putLong(hi).putLong(lo).array();
    }
}
