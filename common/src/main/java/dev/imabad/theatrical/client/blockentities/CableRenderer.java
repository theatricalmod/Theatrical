package dev.imabad.theatrical.client.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.graphs.CableEdge;
import dev.imabad.theatrical.graphs.CableNetwork;
import dev.imabad.theatrical.graphs.CableNode;
import dev.imabad.theatrical.graphs.CableNodePos;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Map;

public class CableRenderer implements BlockEntityRenderer<CableBlockEntity> {


    public CableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
        CableBlock cableBlock = (CableBlock) blockEntity.getBlockState().getBlock();
//        Collection<CableNodePos.DiscoveredPosition> ends = cableBlock.getConnected(null, blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState());
//        CableNetwork network = null;
//        for (CableNodePos.DiscoveredPosition end : ends) {
//            if(network == null){
//                network = TheatricalClient.CABLES.getIntersectingNetworks(end).stream().findFirst().orElse(null);
//                if(network == null){
//                    continue;
//                }
//            }
//            CableNode node = network.locateNode(end);
//            if(node == null){
//                continue;
//            }
//            poseStack.pushPose();
//            Vec3 ourNodePos = node.getPosition().getLocation();
//            Map<CableNode, CableEdge> edges = network.getEdges(node);
//            if(edges != null) {
//                for (CableNode cableNode : edges.keySet()) {
//                    poseStack.pushPose();
//                    Vec3 oNodePos = cableNode.getPosition().getLocation();
//                    Vec3 dirNormal = oNodePos.subtract(ourNodePos).normalize();
//                    double length = ourNodePos.distanceTo(oNodePos);
//                    Direction dir = Direction.fromNormal(new BlockPos(dirNormal));
////                    poseStack.translate(0.5, 0.5, 0.5);
////                    poseStack.mulPose(dir.getRotation());
////                    poseStack.translate(-0.5, -0.5, -0.5);
////                    if (dir == Direction.UP) {
////                        poseStack.translate(0.5, 0.5, 0);
////                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
////                        poseStack.translate(-0.5, -0.5, 0);
////                    } else if (dir == Direction.EAST) {
////                        poseStack.translate(0.5, 0.5, 0);
////                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(90));
////                        poseStack.translate(-0.5, -0.5, 0);
////                    } else if (dir == Direction.WEST) {
////                        poseStack.translate(0.5, 0.5, 0);
////                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90));
////                        poseStack.translate(-0.5, -0.5, 0);
////                    } else if (dir == Direction.SOUTH) {
////                        poseStack.translate(0, 0.5, 0.5);
////                        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
////                        poseStack.translate(0, -0.5, -0.5);
////                    } else if (dir == Direction.NORTH) {
////                        poseStack.translate(0, 0.5, 0.5);
////                        poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
////                        poseStack.translate(0, -0.5, -0.5);
////                    }
//                    var cableWidth = 0.05f;
//                    var cableHeight = 0.05f;
//                    var cableLength = (float) length;
//                    poseStack.translate(0.1, 0, 0);
//                    renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
//                    poseStack.translate(0.5, 0.5, 0.5);
//                    poseStack.popPose();
//                }
//            }
//            poseStack.popPose();
//        }
        CableNetwork network = null;
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
                Vec3 centerOfSide = CableBlock.modifyCenter(Vec3.atBottomCenterOf(blockEntity.getBlockPos()), side);
                if(network == null) {
                    network = TheatricalClient.CABLES.getIntersectingNetworks(new CableNodePos(centerOfSide).dimension(blockEntity.getLevel())).stream()
                            .findFirst().orElse(null);
                }
                Vec3[] axes = CableBlock.DirectionAxes.fromDirection(side).getAxes();
                for (Vec3 axe : axes) {
                    for(int i : new int[]{1, -1}){
                        axe = axe.scale((i));
                        Direction axis = Direction.fromNormal(new BlockPos(axe));
                        var cableWidth = 0.05f;
                        var cableHeight = 0.05f;
                        var cableLength = 0.2f;
                        CableNodePos possibleNode = new CableNodePos(axe.scale(0.5).add(centerOfSide)).dimension(blockEntity.getLevel());
                        if(network == null) {
                            network = TheatricalClient.CABLES.getIntersectingNetworks(possibleNode).stream().findFirst().orElse(null);
                            if(network == null){
                                continue;
                            }
                        }
                        CableNode node = network.locateNode(possibleNode);
                        if(node == null){
                            // Kinda safe to assume that we're connected?
                            cableLength = (float) centerOfSide.distanceTo(possibleNode.getLocation());
                        }
                        if(cableLength <= 0){
                            continue;
                        }
                        poseStack.pushPose();
                        poseStack.translate(0.5, 0.5, 0.5);
                        if(axis == Direction.EAST){
                            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
                        } else if(axis == Direction.SOUTH){
                            poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                        } else if(axis == Direction.NORTH){
                            poseStack.mulPose(Vector3f.YP.rotationDegrees(270));
                        } else if(axis == Direction.UP){
                            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
                        }
//                        poseStack.mulPose(Vector3f.XP.rotationDegrees(axis.getRotation().toXYZDegrees().y()));
                        poseStack.translate(-0.5, -0.5, -0.5);
                        if(cableLength == 0.2f) {
                         poseStack.translate(0.35 - (cableLength / 2), 0, 0);
                        } else {
                            poseStack.translate(-0.05, 0, 0);
                        }
//                        renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
//                        poseStack.translate(-0.5, 0, -0.5);
                        poseStack.popPose();
                    }
//                    poseStack.translate(0.1, 0, -0.1);
//                    renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
                }
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
