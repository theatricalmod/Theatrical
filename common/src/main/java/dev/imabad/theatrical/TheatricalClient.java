package dev.imabad.theatrical;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.client.blockentities.MovingLightRenderer;
import dev.imabad.theatrical.protocols.artnet.ArtNetManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

public class TheatricalClient {

    private static ArtNetManager artNetManager;
    public static void init() {
        BlockEntityRendererRegistry.register(BlockEntities.MOVING_LIGHT.get(), MovingLightRenderer::new);
//        BlockEntityRendererRegistry.register(BlockEntities.CABLE.get(), CableRenderer::new);
        artNetManager = new ArtNetManager();
    }

    public static ArtNetManager getArtNetManager(){
        return artNetManager;
    }

    public static void renderThings(BlockPos MY_BLOCK, VertexConsumer consumer, PoseStack poseStack, MovingLightBlockEntity be){
        Direction direction = be.getBlockState().getValue(MovingLightBlock.FACING);
        float lookingAngle = direction.toYRot();
        boolean isUpsideDown = be.isUpsideDown();
        int pan = be.getPan();
        lookingAngle = (isUpsideDown ? lookingAngle + pan : lookingAngle - pan);

        float tilt = be.getTilt();
        if (!isUpsideDown) {
            tilt = -tilt;
        }

        Vec3 vec32 = BaseLightBlockEntity.calculateViewVector(tilt, lookingAngle);
        double distance = 25;
        Vec3 vec3 = new Vec3(0.5, 0.5, 0.5);
        Vec3 vec33 = vec3.add(vec32.x * distance, vec32.y * distance, vec32.z * distance);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        consumer.vertex(matrix4f, (float) vec3.x, (float) vec3.y, (float) vec3.z).color(255, 255, 255, 255).normal(matrix3f, 0.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(matrix4f, (float) vec33.x, (float) vec33.y, (float) vec33.z).color(255, 255, 255, 255).normal(matrix3f, 0.0f, 0.0f, 0.0f).endVertex();
    }


    public static void renderWorldLast(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta){
        Minecraft mc = Minecraft.getInstance();
        if(mc.options.renderDebug){
            var MY_BLOCK = new BlockPos(-4, -59, 11);
            Vec3 cameraPos = camera.getPosition();
            //#region translateToCamera
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            //#region translateToBlock
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

            bufferSource.endBatch(RenderType.lines());
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
