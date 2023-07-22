package dev.imabad.theatrical.forge.client.model;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.client.model.CableBakedModelBase;
import dev.imabad.theatrical.client.model.CableModelBase;
import dev.imabad.theatrical.forge.blockentity.CableBlockEntityForge;
import dev.imabad.theatrical.graphs.CableEdge;
import dev.imabad.theatrical.graphs.CableNetwork;
import dev.imabad.theatrical.graphs.CableNode;
import dev.imabad.theatrical.graphs.CableNodePos;
import dev.imabad.theatrical.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CableBakedModelForge extends CableBakedModelBase implements IDynamicBakedModel {
    public CableBakedModelForge(CableModelBase cable, TextureAtlasSprite p, CableModelBase.ModelCallback c) {
        super(cable, p, c);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState arg, @Nullable Direction arg2,
                                             @NotNull RandomSource arg3, @NotNull ModelData modelData,
                                             @Nullable RenderType arg4) {
        if(!modelData.has(CableBlockEntityForge.POS) || !modelData.has(CableBlockEntityForge.SIDES)
                || !modelData.has(CableBlockEntityForge.TYPE) || !modelData.has(CableBlockEntityForge.DIMENSION)){
            return Collections.emptyList();
        }
        boolean[] sides = modelData.get(CableBlockEntityForge.SIDES);
        BlockPos pos = modelData.get(CableBlockEntityForge.POS);
        CableType cableType = modelData.get(CableBlockEntityForge.TYPE);
        ResourceKey<Level> dimension = modelData.get(CableBlockEntityForge.DIMENSION);
        if(sides == null || pos == null || dimension == null){
            return Collections.emptyList();
        }
        Vec3 centerOfBlock = Vec3.atBottomCenterOf(pos);
        Vec3 blockPosAsVec = Vec3.atLowerCornerOf(pos);
        CableNetwork network = null;
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction side : Direction.values()) {
            if(sides[side.ordinal()]){
                Vec3 centerOfSide = CableBlock.modifyCenter(centerOfBlock, side);
                CableNodePos sideCenterNodePos = new CableNodePos(centerOfSide).dimension(dimension);
                CableNode sideCenterNode = null;
                if(network == null) {
                    network = TheatricalClient.CABLES.getIntersectingNetworks(sideCenterNodePos).stream()
                            .findFirst().orElse(null);
                }
                if(network != null){
                    sideCenterNode = network.locateNode(sideCenterNodePos);
                }
                if(sideCenterNode != null) {
                    Map<CableNode, CableEdge> edges = network.getEdges(sideCenterNode);
                    if (edges != null) {
                        int renderedEdges = 0;
                        for (CableEdge edge : edges.values()) {
                            Vec3 node1Position = edge.node1.getPosition().getLocation();
                            Vec3 node2Position = edge.node2.getPosition().getLocation();
                            node1Position = node1Position.subtract(blockPosAsVec);
                            node2Position = node2Position.subtract(blockPosAsVec);
                            boolean shouldRender = false;
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
                                        Vec3 nodesNormal = node2Position.subtract(node1Position).normalize();
                                        Direction nodeConnectionDirection = Direction.fromNormal((int) Math.round(nodesNormal.x), (int) Math.round(nodesNormal.y), (int) Math.round(nodesNormal.z));
                                        if(nodeConnectionDirection == Direction.UP){
                                            nodeConnectionDirection = Direction.SOUTH;
                                        } else if(nodeConnectionDirection == Direction.DOWN){
                                            nodeConnectionDirection = Direction.NORTH;
                                        }
                                        if(nodeConnectionDirection != null){
                                            Map<Direction, List<BakedQuad>> directionListMap = cornerPieces.get(side).get(cableType);
                                            if(directionListMap.containsKey(nodeConnectionDirection)){
                                                quads.addAll(directionListMap.get(nodeConnectionDirection));
                                            }
                                        }
                                    } else if(deciZ == 0 && deciX == 0.5){
                                        node2Position = new Vec3(node2Position.x, highestPoint.y, node1Position.z);
                                        Vec3 nodesNormal = node2Position.subtract(node1Position).normalize();
                                        Direction nodeConnectionDirection = Direction.fromNormal((int) Math.round(nodesNormal.x), (int) Math.round(nodesNormal.y), (int) Math.round(nodesNormal.z));
                                        if(nodeConnectionDirection == Direction.UP){
                                            nodeConnectionDirection = Direction.SOUTH;
                                        } else if(nodeConnectionDirection == Direction.DOWN){
                                            nodeConnectionDirection = Direction.NORTH;
                                        }
                                        if(nodeConnectionDirection != null){
                                            Map<Direction, List<BakedQuad>> directionListMap = cornerPieces.get(side).get(cableType);
                                            if(directionListMap.containsKey(nodeConnectionDirection)){
                                                quads.addAll(directionListMap.get(nodeConnectionDirection));
                                            }
                                        }
                                    } else {
                                        node2Position = new Vec3(node2Position.x, highestPoint.y, node2Position.z);
                                    }
                                }
                                shouldRender = true;
                            } else if(node1Position.y == node2Position.y && (node1Position.x != node2Position.x && node1Position.z != node2Position.z)) {
                                //Horizontal Diagonal.
                                double deciZ = node2Position.z - Math.floor(node2Position.z);
                                double yDiff = node1Position.y - node2Position.y;
                                double deciX = node2Position.x - Math.floor(node2Position.x);
                                if(deciZ == 0.5){
                                    node2Position = new Vec3(node2Position.x, node2Position.y, node1Position.z);
                                    Vec3 nodesNormal = node2Position.subtract(node1Position).normalize();
                                    Direction nodeConnectionDirection = Direction.fromNormal((int) Math.round(nodesNormal.x), (int) Math.round(nodesNormal.y), (int) Math.round(nodesNormal.z));
                                    if(side.getAxis() == Direction.Axis.Z){
                                        if(nodeConnectionDirection == Direction.EAST){
                                            nodeConnectionDirection = side == Direction.NORTH ? Direction.WEST : Direction.EAST;
                                        } else if(nodeConnectionDirection == Direction.WEST){
                                            nodeConnectionDirection = side == Direction.SOUTH ? Direction.WEST : Direction.EAST;
                                        }
                                    }
                                    if(nodeConnectionDirection == Direction.UP){
                                        nodeConnectionDirection = Direction.SOUTH;
                                    } else if(nodeConnectionDirection == Direction.DOWN){
                                        nodeConnectionDirection = Direction.NORTH;
                                    }
                                    if(nodeConnectionDirection != null){
                                        Map<Direction, List<BakedQuad>> directionListMap = cornerPieces.get(side).get(cableType);
                                        if(directionListMap.containsKey(nodeConnectionDirection)){
                                            quads.addAll(directionListMap.get(nodeConnectionDirection));
                                        }
                                    }
                                } else if(deciX == 0.5){
                                    node2Position = new Vec3(node1Position.x, node2Position.y, node2Position.z);
                                }
                                shouldRender = true;
                            } else if(node1Position.distanceTo(node2Position) >= 1){
                                shouldRender = true;
                            }
                            if(shouldRender) {
                                Vec3 nodesNormal = node2Position.subtract(node1Position).normalize();
                                Direction nodeConnectionDirection = Direction.fromNormal((int) Math.round(nodesNormal.x), (int) Math.round(nodesNormal.y), (int) Math.round(nodesNormal.z));
                                if(side.getAxis() == Direction.Axis.X){
                                    if(nodeConnectionDirection == Direction.NORTH){
                                        nodeConnectionDirection = side == Direction.EAST ? Direction.EAST : Direction.WEST;
                                    } else if(nodeConnectionDirection == Direction.SOUTH){
                                        nodeConnectionDirection = side == Direction.WEST ? Direction.EAST : Direction.WEST;
                                    }
                                } else if(side.getAxis() == Direction.Axis.Z){
                                    if(nodeConnectionDirection == Direction.EAST){
                                        nodeConnectionDirection = side == Direction.NORTH ? Direction.WEST : Direction.EAST;
                                    } else if(nodeConnectionDirection == Direction.WEST){
                                        nodeConnectionDirection = side == Direction.SOUTH ? Direction.WEST : Direction.EAST;
                                    }
                                }
                                if(nodeConnectionDirection == Direction.UP){
                                    nodeConnectionDirection = Direction.SOUTH;
                                } else if(nodeConnectionDirection == Direction.DOWN){
                                    nodeConnectionDirection = Direction.NORTH;
                                }
                                if(nodeConnectionDirection != null) {
                                    Map<Direction, List<BakedQuad>> directionListMap = directionToTypeToSide.get(side)
                                            .get(cableType);
                                    if(directionListMap.containsKey(nodeConnectionDirection)){
                                        quads.addAll(directionListMap.get(nodeConnectionDirection));
                                        renderedEdges++;
                                    }
                                }
                            }
                        }
                        if(renderedEdges == 0){
                            for (CableEdge edge : edges.values()) {
                                Vec3 node1Position = edge.node1.getPosition().getLocation();
                                Vec3 node2Position = edge.node2.getPosition().getLocation();
                                node1Position = node1Position.subtract(blockPosAsVec);
                                node2Position = node2Position.subtract(blockPosAsVec);
                                Vec3 nodesNormal = node1Position.subtract(node2Position).normalize();
                                Direction nodeConnectionDirection = Direction.fromNormal((int) Math.round(nodesNormal.x), (int) Math.round(nodesNormal.y), (int) Math.round(nodesNormal.z));
                                if(nodeConnectionDirection == Direction.UP){
                                    nodeConnectionDirection = Direction.SOUTH;
                                } else if(nodeConnectionDirection == Direction.DOWN){
                                    nodeConnectionDirection = Direction.NORTH;
                                }
                                if(nodeConnectionDirection != null) {
                                    Map<Direction, List<BakedQuad>> directionListMap = directionToTypeToSide.get(side)
                                            .get(cableType);
                                    if(directionListMap.containsKey(nodeConnectionDirection)){
                                        quads.addAll(directionListMap.get(nodeConnectionDirection));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ClientUtils.optimize(quads);
    }

}
