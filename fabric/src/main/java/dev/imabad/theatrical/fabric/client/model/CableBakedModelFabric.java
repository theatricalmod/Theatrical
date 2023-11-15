package dev.imabad.theatrical.fabric.client.model;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.client.model.CableBakedModelBase;
import dev.imabad.theatrical.client.model.CableModelBase;
import dev.imabad.theatrical.graphs.CableEdge;
import dev.imabad.theatrical.graphs.CableNetwork;
import dev.imabad.theatrical.graphs.CableNode;
import dev.imabad.theatrical.graphs.CableNodePos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class CableBakedModelFabric extends CableBakedModelBase implements BakedModel, FabricBakedModel {

    public CableBakedModelFabric(CableModelBase cable, TextureAtlasSprite p, CableModelBase.ModelCallback c) {
        super(cable, p, c);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> randomSupplier, RenderContext context) {
        if(!(blockView.getBlockEntity(pos) instanceof CableBlockEntity cbe)){
            return;
        }
        QuadEmitter emitter = context.getEmitter();
        CableType cableType = state.getValue(CableBlock.CABLE_TYPE);
        Vec3 centerOfBlock = Vec3.atBottomCenterOf(pos);
        Vec3 blockPosAsVec = Vec3.atLowerCornerOf(pos);
        CableNetwork network = null;
        RenderType layer = ItemBlockRenderTypes.getChunkRenderType(state);
        BlendMode blendMode = BlendMode.fromRenderLayer(layer);
        RenderMaterial renderMaterial = RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, blendMode).find();
        for (Direction side : Direction.values()) {
            if(cbe.hasSide(side)){
                Vec3 centerOfSide = CableBlock.modifyCenter(centerOfBlock, side);
                CableNodePos sideCenterNodePos = new CableNodePos(centerOfSide).dimension(Minecraft.getInstance().level);
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
                                                for (BakedQuad quad : directionListMap.get(nodeConnectionDirection)) {
                                                    emitter.fromVanilla(quad, renderMaterial, side.getOpposite());
                                                    emitter.emit();
                                                }
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
                                                directionListMap.get(nodeConnectionDirection).forEach(quad -> {
                                                    emitter.fromVanilla(quad, renderMaterial, side.getOpposite());
                                                    emitter.emit();
                                                });
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
                                            for (BakedQuad quad : directionListMap.get(nodeConnectionDirection)) {
                                                emitter.fromVanilla(quad, renderMaterial, null);
                                                emitter.emit();
                                            }
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
                                        for (BakedQuad quad : directionListMap.get(nodeConnectionDirection)) {
                                            emitter.fromVanilla(quad, renderMaterial, null);
                                            emitter.emit();
                                        }
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
                                        directionListMap.get(nodeConnectionDirection)
                                                .forEach((quad) -> {
                                                    emitter.fromVanilla(quad, renderMaterial, side.getOpposite());
                                                    emitter.emit();
                                                });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {

    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return Collections.emptyList();
    }

}
