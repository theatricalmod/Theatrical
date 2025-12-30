package dev.imabad.theatrical.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LazyRenderers {

    public static abstract class LazyRenderer {
        public abstract void render(PoseStack poseStack, CameraRenderState camera);
        public abstract Vec3 getPos();
    }

    private static final List<LazyRenderer> renderers = new ArrayList<>();

    public static void addLazyRender(LazyRenderer renderer){
        renderers.add(renderer);
    }

    public static void doRender(CameraRenderState camera, PoseStack poseStack){
        if(!renderers.isEmpty()){
            if(renderers.size() == 1){
                LazyRenderer first = renderers.getFirst();
                first.render(poseStack, camera);
            } else {
                List<Tuple<LazyRenderer, Double>> distanced = new ArrayList<>();
                for (LazyRenderer lazyRenderer : renderers) {
                    distanced.add(new Tuple<>(lazyRenderer, camera.pos.distanceToSqr(lazyRenderer.getPos())));
                }
                distanced.sort(Comparator.comparingDouble(t -> -t.getB()));
                for (Tuple<LazyRenderer, Double> lazyRendererDoubleTuple : distanced) {
                    LazyRenderer a = lazyRendererDoubleTuple.getA();
                    a.render(poseStack, camera);
                }
            }
            renderers.clear();
        }
    }

}
