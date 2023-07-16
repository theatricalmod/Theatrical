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
import dev.imabad.theatrical.graphs.*;
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
import net.minecraft.world.phys.shapes.Shapes;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

public class TheatricalClient {

    private static ArtNetManager artNetManager;
    public static final GlobalCableManager CABLES = new GlobalCableManager();

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
        Minecraft mc = Minecraft.getInstance();
        if(mc.options.renderDebug){
            var MY_BLOCK = new BlockPos(-4, -59, 11);
            Vec3 cameraPos = camera.getPosition();
            //#region translateToCamera
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            //#region translateToBlock
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            for(CableNetwork network : CABLES.getCableNetworks().values()){
                Color color = getRandomColor(network.getId());
                for (Map.Entry<CableNodePos, CableNode> cableNodeEntry : network.getNodes().entrySet()) {
                    CableNodePos pos = cableNodeEntry.getKey();
                    CableNode node = cableNodeEntry.getValue();
                    if(pos == null){
                        continue;
                    }
                    if(!mc.level.dimension().equals(pos.dimension())){
                        continue;
                    }
                    poseStack.pushPose();
                    Vec3 location = pos.getLocation();
                    poseStack.translate(location.x, location.y, location.z);
                    poseStack.pushPose();
                    //poseStack.translate(0.5, 0.5, 0.5);
                    VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
//                Matrix4f matrix4f = poseStack.last().pose();
//                Matrix3f matrix3f = poseStack.last().normal();
//                buffer.vertex(matrix4f, 0, 0, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255).normal(matrix3f, 0,1, 0).endVertex();
//                buffer.vertex(matrix4f, 0, 1, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 255).normal(matrix3f, 0,1, 0).endVertex();
                    LevelRenderer.renderLineBox(poseStack, buffer, AABB.ofSize(new Vec3(0, 0, 0), 0.1d, 0.1d, 0.1d), color.getRed(), color.getGreen(), color.getBlue(), 1);
                    Map<CableNode, CableEdge> edges = network.getEdges(node);
                    poseStack.popPose();
                    poseStack.popPose();
                    if(edges != null){
                        poseStack.pushPose();
                        for (Map.Entry<CableNode, CableEdge> entry : edges.entrySet()) {
                            CableNode other = entry.getKey();
                            CableEdge edge = entry.getValue();
                            if(!edge.node1.getPosition().dimension().equals(edge.node2.getPosition().dimension())){
                                //don't care
                            } else {
                                Vec3 node1Position = edge.node1.getPosition().getLocation();
                                Vec3 node2Position = edge.node2.getPosition().getLocation();
                                Matrix4f matrix4f = poseStack.last().pose();
                                Matrix3f matrix3f = poseStack.last().normal();
                                Vec3 normal = node2Position.subtract(node1Position).normalize();
                                buffer.vertex(matrix4f, (float) node1Position.x, (float) node1Position.y , (float) node1Position.z).color(color.getRed(), color.getGreen(), color.getBlue(), 255).normal(matrix3f,0,1 ,0).endVertex();
                                buffer.vertex(matrix4f, (float) node2Position.x, (float) node2Position.y, (float) node2Position.z).color(color.getRed(), color.getGreen(), color.getBlue(), 255).normal(matrix3f, 0, 1, 0).endVertex();
                            }
                        }
                        poseStack.popPose();
                    }
                }
            }
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
