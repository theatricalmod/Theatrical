package dev.imabad.theatrical.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LazyRenderers {

    public static abstract class LazyRenderer {
        public abstract void render(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, Camera camera, float partialTick);
        public abstract Vec3 getPos(float partialTick);
    }

    private static final List<LazyRenderer> renderers = new ArrayList<>();

    public static void addLazyRender(LazyRenderer renderer){
        renderers.add(renderer);
    }

    public static void doRender(Camera camera, PoseStack poseStack, MultiBufferSource.BufferSource renderer,float partialTick){
        if(!renderers.isEmpty()){
            if(renderers.size() == 1){
                LazyRenderer first = renderers.get(0);
                first.render(renderer, poseStack, camera, partialTick);
            } else {
                List<Tuple<LazyRenderer, Double>> distanced = new ArrayList<>();
                for (LazyRenderer lazyRenderer : renderers) {
                    distanced.add(new Tuple<>(lazyRenderer, camera.getPosition().distanceToSqr(lazyRenderer.getPos(partialTick))));
                }
                distanced.sort(Comparator.comparingDouble(t -> -t.getB()));
                for (Tuple<LazyRenderer, Double> lazyRendererDoubleTuple : distanced) {
                    LazyRenderer a = lazyRendererDoubleTuple.getA();
                    a.render(renderer, poseStack, camera, partialTick);
                }
            }
            renderer.endBatch();
            renderers.clear();
        }
    }

}
