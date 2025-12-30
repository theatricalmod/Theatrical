package dev.imabad.theatrical.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.imabad.theatrical.Theatrical;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class TheatricalRenderTypes {

    public static final RenderPipeline FADER_PIPELINE = RenderPipeline
            .builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Theatrical.location("pipeline/fader"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

    public static final RenderType FADER = RenderType.create(
            "theatrical_fader",
            RenderSetup.builder(FADER_PIPELINE).bufferSize(32565).createRenderSetup());

    public static final RenderPipeline BEAM_PIPELINE = RenderPipeline
            .builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Theatrical.location("pipeline/beam"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();


    public static final RenderType BEAM = RenderType.create(
            "theatrical_beam",
            RenderSetup.builder(BEAM_PIPELINE).bufferSize(32565).createRenderSetup());

}
