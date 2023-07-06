package dev.imabad.theatrical;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.client.blockentities.CableRenderer;
import dev.imabad.theatrical.client.blockentities.MovingLightRenderer;
import dev.imabad.theatrical.protocols.artnet.ArtNetManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TheatricalClient {

    private static ArtNetManager artNetManager;

    public static void init() {
        BlockEntityRendererRegistry.register(BlockEntities.MOVING_LIGHT.get(), MovingLightRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.CABLE.get(), CableRenderer::new);
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

    public static boolean renderHitBox(PoseStack poseStack, Level level, BlockPos blockPos, Entity entity, Camera camera){
        if(level.getBlockState(blockPos).getBlock() instanceof CableBlock && level.getBlockEntity(blockPos) instanceof CableBlockEntity cable){
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
            int shapeIndex  = CableBlock.getSubShapeHit(cable, entity, blockPos, CableBlock.BOXES);

            if(shapeIndex >= 0 && shapeIndex < CableBlock.BOXES.length){
                Direction direction = Direction.values()[shapeIndex];

                var shape = CableBlock.BOXES[shapeIndex];
                Vec3 cameraPos = camera.getPosition();
                //#region translateToCamera
                poseStack.pushPose();
                poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                //#region translateToBlock
                poseStack.pushPose();
                poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                //#region MainRender
                poseStack.pushPose();
                poseStack.translate(0, 0,0);
                LevelRenderer.renderLineBox(poseStack, buffer, shape.bounds(), 0, 0, 0, 0.4f);
                bufferSource.endBatch(RenderType.lines());
                //#endregion
                poseStack.popPose();
                //#endregion
                poseStack.popPose();
                //#endregion
                poseStack.popPose();
            }
            return false;
        }
        return true;
    }

    public static void renderWorldLast(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta){
        var MY_BLOCK = new BlockPos(-4, -59, 11);
        Vec3 cameraPos = camera.getPosition();
        //#region translateToCamera
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
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
        if(Minecraft.getInstance().level.getBlockEntity(MY_BLOCK) != null){
//            renderThings(MY_BLOCK, buffer, poseStack, (MovingLightBlockEntity) Minecraft.getInstance().level.getBlockEntity(MY_BLOCK));
        }
        bufferSource.endBatch(RenderType.lines());
        //#endregion
        poseStack.popPose();
        //#endregion
        poseStack.popPose();
        //#endregion
        poseStack.popPose();
    }
}
