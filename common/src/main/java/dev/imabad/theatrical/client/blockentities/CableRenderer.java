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
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collection;
import java.util.Map;

public class CableRenderer implements BlockEntityRenderer<CableBlockEntity> {


    public CableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
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
        Vec3 blockPosAsVec = Vec3.atLowerCornerOf(blockEntity.getBlockPos());
        CableNetwork network = null;
        for(Direction side : Direction.values()){
            poseStack.pushPose();
            if(blockEntity.hasSide(side)){
                switch (side) {
                    case DOWN -> poseStack.translate(0, 0.05, 0);
                    case WEST -> poseStack.translate(0.05, 0, 0);
                    case EAST -> poseStack.translate(-0.05, 0, 0);
                    case NORTH -> poseStack.translate(0, 0, 0.05);
                    case SOUTH -> poseStack.translate(0, 0, -0.05);
                }
//                if(side == Direction.UP) {
//                    poseStack.translate(0.5, 0.5, 0);
//                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
//                    poseStack.translate(-0.5, -0.5, 0);
//                } else if(side == Direction.EAST){
//                    poseStack.translate(0.5, 0.5, 0);
//                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(90));
//                    poseStack.translate(-0.5, -0.5, 0);
//                } else if(side == Direction.WEST){
//                    poseStack.translate(0.5, 0.5, 0);
//                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(-90));
//                    poseStack.translate(-0.5, -0.5, 0);
//                } else if(side == Direction.SOUTH){
//                    poseStack.translate(0, 0.5, 0.5);
//                    poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
//                    poseStack.translate(0, -0.5, -0.5);
//                }else if(side == Direction.NORTH){
//                    poseStack.translate(0, 0.5, 0.5);
//                    poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
//                    poseStack.translate(0, -0.5, -0.5);
//                }
//                poseStack.mulPose(Vector3f.YP.rotationDegrees(side.toYRot()));
                Vec3 centerOfSide = CableBlock.modifyCenter(Vec3.atBottomCenterOf(blockEntity.getBlockPos()), side);
                CableNodePos sideCenterNodePos = new CableNodePos(centerOfSide).dimension(blockEntity.getLevel());
                CableNode sideCenterNode = null;
                if(network == null) {
                    network = TheatricalClient.CABLES.getIntersectingNetworks(sideCenterNodePos).stream()
                            .findFirst().orElse(null);
                }
                if(network != null){
                    sideCenterNode = network.locateNode(sideCenterNodePos);
                }
                var cableWidth = 0.05f;
                var cableHeight = 0.05f;
                if(sideCenterNode != null) {
                    Map<CableNode, CableEdge> edges = network.getEdges(sideCenterNode);
                    if (edges != null) {
                        int renderedEdges = 0;
                        for (CableEdge edge : edges.values()) {
                            Vec3 node1Position = edge.node1.getPosition().getLocation();
                            Vec3 node2Position = edge.node2.getPosition().getLocation();
                            node1Position = node1Position.subtract(blockPosAsVec);
                            node2Position = node2Position.subtract(blockPosAsVec);
                            if(node1Position.y != node2Position.y && (node1Position.x != node2Position.x || node1Position.z != node2Position.z)){
                                // Vertical Diagonal
                                Vec3 highestPoint = new Vec3(Math.max(node1Position.x, node2Position.x), Math.max(node1Position.y, node2Position.y), Math.max(node1Position.z, node2Position.z));
                                Vec3 lowestPoint = new Vec3(Math.min(node1Position.x, node2Position.x), Math.min(node1Position.y, node2Position.y), Math.min(node1Position.z, node2Position.z));
                                double diff = lowestPoint.y - Math.floor(lowestPoint.y);
                                double yDiff = node1Position.y - node2Position.y;
                                double deciZ = node1Position.z - Math.floor(node1Position.z);
                                double deciX = node1Position.x - Math.floor(node1Position.x);
                                if(diff == 0){
                                    // L shape - ◺
                                    if(deciZ == 0.5 && deciX == 0) {
                                        node2Position = new Vec3(node1Position.x, lowestPoint.y, node2Position.z);
                                    } else if(deciZ == 0 && deciX == 0.5){
                                        node2Position = new Vec3(node2Position.x, lowestPoint.y, node1Position.z);
                                    } else {
                                        node2Position = new Vec3(node2Position.x, lowestPoint.y, node2Position.z);
                                    }
                                } else {
                                    // backwards r shape - ◹
//                                    node2Position = new Vec3(lowestPoint.x, highestPoint.y, lowestPoint.z);
                                    if(deciZ == 0.5 && deciX == 0){
                                        node2Position = new Vec3(node1Position.x, highestPoint.y, node2Position.z);
                                    } else if(deciZ == 0 && deciX == 0.5){
                                        node2Position = new Vec3(node2Position.x, highestPoint.y, node1Position.z);
                                    } else {
                                        node2Position = new Vec3(node2Position.x, highestPoint.y, node2Position.z);
                                    }
                                }
                                Vec3 relat = node1Position.subtract(node2Position);
                                if(relat.y != 0){
                                    if(relat.y > 0){
                                        node2Position = node2Position.add(0, -0.05, 0);
                                    } else {
                                        node2Position = node2Position.add(0, 0.05, 0);
                                    }
                                } else if(relat.z != 0){
                                    if(relat.z > 0){
                                        node2Position = node2Position.add(0, 0, -0.05);
                                    } else {
                                        node2Position = node2Position.add(0, 0, 0.05);
                                    }
                                } else if(relat.x != 0){
                                    if(relat.x > 0){
                                        node2Position = node2Position.add(-0.05, 0, 0);
                                    } else {
                                        node2Position = node2Position.add(0.05, 0, 0);
                                    }
                                }
                                renderCableBetweenNodes(vertexConsumer, poseStack,cableWidth, cableHeight, node1Position, node2Position,
                                        0, 0, 0, side);
                                renderedEdges++;
                            } else if(node1Position.y == node2Position.y && (node1Position.x != node2Position.x && node1Position.z != node2Position.z)) {
                                //Horizontal Diagonal.
                                double deciZ = node2Position.z - Math.floor(node2Position.z);
                                double yDiff = node1Position.y - node2Position.y;
                                double deciX = node2Position.x - Math.floor(node2Position.x);
                                if(deciZ == 0.5){
                                    node2Position = new Vec3(node2Position.x, node2Position.y, node1Position.z);
                                } else if(deciX == 0.5){
                                    node2Position = new Vec3(node1Position.x, node2Position.y, node2Position.z);
                                }
                                if(yDiff == 0){
                                    Vec3 relat = node1Position.subtract(node2Position);
                                    if(relat.x != 0){
                                        if(relat.x > 0){
                                            node2Position = node2Position.add(-0.05, 0, 0);
                                        } else {
                                            node2Position = node2Position.add(0.05, 0, 0);
                                        }
                                    } else if(relat.z != 0){
                                        if(relat.z > 0){
                                            node2Position = node2Position.add(0, 0, -0.05);
                                        } else {
                                            node2Position = node2Position.add(0, 0, 0.05);
                                        }
                                    }
                                }
                                renderCableBetweenNodes(vertexConsumer, poseStack,cableWidth, cableHeight, node1Position, node2Position,
                                        0, 0, 0, side);
                                renderedEdges++;
                            } else if(node1Position.distanceTo(node2Position) >= 1){
                                renderCableBetweenNodes(vertexConsumer, poseStack,cableWidth, cableHeight, node1Position,node2Position, 0, 0, 0, side);
                                renderedEdges++;
                            }
                        }
                        if(renderedEdges == 0){
                            for (CableEdge edge : edges.values()) {
                                Vec3 node1Position = edge.node1.getPosition().getLocation();
                                Vec3 node2Position = edge.node2.getPosition().getLocation();
                                node1Position = node1Position.subtract(blockPosAsVec);
                                node2Position = node2Position.subtract(blockPosAsVec);
                                renderCableBetweenNodes(vertexConsumer, poseStack,cableWidth, cableHeight, node1Position,node2Position, 0, 0, 0, side);
                            }
                        }
                    }
                }
//                Vec3[] axes = CableBlock.DirectionAxes.fromDirection(side).getAxes();
//                for (Vec3 axe : axes) {
//                    for(int i : new int[]{1, -1}){
//                        axe = axe.scale((i));
//                        Direction axis = Direction.fromNormal(new BlockPos(axe));
//                        var cableLength = 0.2f;
//                        CableNodePos possibleNode = new CableNodePos(axe.scale(0.5).add(centerOfSide)).dimension(blockEntity.getLevel());
//                        if(network == null) {
//                            network = TheatricalClient.CABLES.getIntersectingNetworks(possibleNode).stream().findFirst().orElse(null);
//                            if(network == null){
//                                continue;
//                            }
//                        }
//                        CableNode node = network.locateNode(possibleNode);
//                        if(node == null){
//                            // Kinda safe to assume that we're connected?
//                            cableLength = (float) centerOfSide.distanceTo(possibleNode.getLocation());
//                        }
//                        if(cableLength <= 0){
//                            continue;
//                        }
//                        poseStack.pushPose();
//                        poseStack.translate(0.5, 0.5, 0.5);
//                        if(axis == Direction.EAST){
//                            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
//                        } else if(axis == Direction.SOUTH){
//                            poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
//                        } else if(axis == Direction.NORTH){
//                            poseStack.mulPose(Vector3f.YP.rotationDegrees(270));
//                        } else if(axis == Direction.UP){
//                            poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
//                        }
////                        poseStack.mulPose(Vector3f.XP.rotationDegrees(axis.getRotation().toXYZDegrees().y()));
//                        poseStack.translate(-0.5, -0.5, -0.5);
//                        if(cableLength == 0.2f) {
//                         poseStack.translate(0.35 - (cableLength / 2), 0, 0);
//                        } else {
//                            poseStack.translate(-0.05, 0, 0);
//                        }
//                        renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
////                        poseStack.translate(-0.5, 0, -0.5);
//                        poseStack.popPose();
//                    }
////                    poseStack.translate(0.1, 0, -0.1);
////                    renderCableLength(vertexConsumer, poseStack, cableLength, cableWidth, cableHeight);
//                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    private void addVertex(VertexConsumer builder, Matrix4f matrix4f, Matrix3f matrix3f, int r, int g, int b, int a, float x, float y, float z) {
        builder.vertex(matrix4f, x, y, z).color(r, g, b, a).overlayCoords(OverlayTexture.NO_OVERLAY).endVertex();
    }
    private void renderCableBetweenNodes(VertexConsumer vertexConsumer, PoseStack poseStack, float cableWidth, float cableHeight, Vec3 pos1, Vec3 pos2, float r, float g, float b, Direction side) {

        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float minX = (float) pos1.x; // + (cableWidth / 2);
        float minY = (float) pos1.y; // - (cableHeight / 2);
        float minZ = (float) pos1.z; // - (cableWidth / 2);
        float maxX = (float) pos2.x; // + (cableWidth);
        float maxY = (float) pos2.y; // + (cableHeight);
        float maxZ = (float) pos2.z; // + (cableWidth);
//        if(side.getOpposite().getAxis().getOpposite().getAxisDirection() == Direction.AxisDirection.NEGATIVE){
//            cableWidth = -cableWidth;
//            cableHeight = -cableHeight;
//        }

        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ).expandTowards(cableWidth / 2, cableHeight / 2, cableWidth / 2);
        box = box.expandTowards(-(cableWidth / 2), -(cableHeight / 2), -(cableWidth / 2));
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, box, r,g ,b ,1);
//        LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, Shapes.create(, pos1.y - (cableHeight / 2), pos1.z - (cableWidth / 2), pos2.x + (cableWidth / 2), pos2.y + (cableHeight / 2), pos2.z +  (cableWidth / 2)), 0, 0,0, 1,1 ,1, 1);
//        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//
//        vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//
//        vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(normal, -1.0F, 0.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
//
//        vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(normal, 0.0F, -1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
//
//        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(normal, 1.0F, 0.0F, 0.0F).endVertex();
//
//        vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
//        vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
//        addVertex(vertexConsumer, m, normal, r,g,b,a, pos1.x() - (cableWidth / 2), pos1.y() - (cableHeight / 2), pos1.z()- (cableWidth / 2));
//        addVertex(vertexConsumer, m, normal, r,g,b,a, pos1.x() - (cableWidth / 2), pos1.y() + (cableHeight / 2), pos1.z()- (cableWidth / 2));
//        addVertex(vertexConsumer, m, normal, r,g,b,a, pos2.x() - (cableWidth / 2), pos2.y() + (cableHeight / 2), pos2.z()- (cableWidth / 2));
//        addVertex(vertexConsumer, m, normal, r,g,b,a,  pos2.x() - (cableWidth / 2), pos2.y() - (cableHeight / 2), pos2.z()- (cableWidth / 2));
//
//        addVertex(vertexConsumer, m, normal, r,g,b,a, pos2.x() + (cableWidth / 2), pos2.y() - (cableHeight / 2), pos2.z() + (cableWidth / 2));
//        addVertex(vertexConsumer, m, normal, r,g,b,a, pos2.x() + (cableWidth / 2), pos2.y() + (cableHeight / 2), pos2.z() + (cableWidth / 2));
//        addVertex(vertexConsumer, m, normal, r,g,b,a, pos1.x() - (cableWidth / 2), pos1.y() + (cableHeight / 2), pos1.z() - (cableWidth / 2));
//        addVertex(vertexConsumer, m, normal, r,g,b,a,  pos1.x() - (cableWidth / 2), pos1.y() - (cableHeight / 2), pos1.z()- (cableWidth / 2));
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
