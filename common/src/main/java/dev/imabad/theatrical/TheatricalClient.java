package dev.imabad.theatrical;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.client.LazyRenderers;
import dev.imabad.theatrical.client.blockentities.BasicLightingConsoleRenderer;
import dev.imabad.theatrical.client.blockentities.FresnelRenderer;
import dev.imabad.theatrical.client.blockentities.LEDPanelRenderer;
import dev.imabad.theatrical.client.blockentities.MovingLightRenderer;
import dev.imabad.theatrical.client.dmx.ArtNetToNetworkClientData;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.config.UniverseConfig;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.client.dmx.TheatricalArtNetClient;
import dev.imabad.theatrical.lighting.LightManager;
import dev.imabad.theatrical.net.artnet.ListConsumers;
import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import dev.imabad.theatrical.client.dmx.ArtNetManager;
import dev.imabad.theatrical.net.artnet.RequestNetworks;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TheatricalClient {

    public static Set<BlockPos> DEBUG_BLOCKS = new HashSet<>();
    private static ArtNetManager artNetManager;
    public static void init() {
        BlockEntityRendererRegistry.register(BlockEntities.MOVING_LIGHT.get(), MovingLightRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.LED_FRESNEL.get(), FresnelRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.LED_PANEL.get(), LEDPanelRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.BASIC_LIGHTING_DESK.get(), BasicLightingConsoleRenderer::new);
//        BlockEntityRendererRegistry.register(BlockEntities.CABLE.get(), CableRenderer::new);
        artNetManager = new ArtNetManager();
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register((event) -> {
            new RequestNetworks().sendToServer();
            if(TheatricalConfig.INSTANCE.CLIENT.artnetEnabled){
                artNetManager.getClient();
            }
        });
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register((event) -> {
            onWorldClose();
        });
/*      We send straight from the ArtNetClient now instead of looping on a tick.
        ClientTickEvent.CLIENT_LEVEL_POST.register(instance -> {
            if(instance.dimension().equals(Level.OVERWORLD)) {
                if (TheatricalConfig.INSTANCE.CLIENT.artnetEnabled) {
                    for (int univers : artNetManager.getClient().getUniverses()) {
                        if(univers != -1) {
                            byte[] data = artNetManager.getClient().readDmxData(0, univers);
                            new SendArtNetData(artNetManager.getNetworkId(), univers, data).sendToServer();
                        }
                    }
                }
            }
        });
*/
    }

    public static ArtNetManager getArtNetManager(){
        return artNetManager;
    }

    public static float[] renderThings(BlockPos MY_BLOCK, VertexConsumer consumer, PoseStack poseStack, BaseLightBlockEntity be, MultiBufferSource multiBuffer){
        Vec3 viewVector = BaseLightBlockEntity.rayTraceDir(be);
        double distance = 25;
        Vec3 origin = new Vec3(0.5, 0.5, 0.5);
        Vec3 destination = origin.add(viewVector.x * distance, viewVector.y * distance, viewVector.z * distance);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        consumer.vertex(matrix4f, (float) origin.x, (float) origin.y, (float) origin.z).color(255, 255, 255, 255).normal(matrix3f, 0.0f, 0.0f, 0.0f).endVertex();
        consumer.vertex(matrix4f, (float) destination.x, (float) destination.y, (float) destination.z).color(255, 255, 255, 255).normal(matrix3f, 0.0f, 0.0f, 0.0f).endVertex();
        return new float[]{be.getTilt(), be.getPan()};
    }

    public static void onWorldClose(){
        artNetManager.shutdownAll();
        ArtNetToNetworkClientData.unload();
    }

    public static void renderWorldLastAfterTripwire(LevelRenderer levelRenderer){
        LightManager.updateAll(levelRenderer);
    }

    public static void renderWorldLast(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta){
        Minecraft mc = Minecraft.getInstance();
        LazyRenderers.doRender(camera,poseStack, mc.renderBuffers().bufferSource(), tickDelta);
        if(Platform.isDevelopmentEnvironment()) {
            if (mc.options.renderDebug) {
                Vec3 cameraPos = camera.getPosition();
                //#region translateToCamera
                poseStack.pushPose();
                poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                for (BlockPos MY_BLOCK : DEBUG_BLOCKS) {
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
                    float[] values = null;
                    if (Minecraft.getInstance().level.getBlockEntity(MY_BLOCK) != null) {
                        values = renderThings(MY_BLOCK, buffer, poseStack, (BaseLightBlockEntity) Minecraft.getInstance().level.getBlockEntity(MY_BLOCK), bufferSource);
                    }
                    bufferSource.endBatch(RenderType.lines());
                    if (values != null) {
                        poseStack.pushPose();
                        poseStack.translate(-0.5, 1.25, 0.5);
                        poseStack.scale(0.025f, 0.025f, 0.025f);
                        BlockState blockState = Minecraft.getInstance().level.getBlockState(MY_BLOCK);
                        Direction opposite = blockState.getValue(MovingLightBlock.HANG_DIRECTION).getOpposite();
                        poseStack.mulPose(Axis.XP.rotationDegrees(180));
                        poseStack.mulPose(Axis.YP.rotationDegrees(opposite.toYRot()));
                        Minecraft.getInstance().font.drawInBatch(String.format("OG Tilt: %s OG Pan: %s", values[0], values[1]), 0, -10, 0xffffff, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0, false);
                        Minecraft.getInstance().font.drawInBatch(String.format("DIR: %s", blockState.getValue(MovingLightBlock.FACING)), 0, -30, 0xffffff, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0, false);
                        poseStack.popPose();
                    }
                    //#endregion
                    poseStack.popPose();
                    //#endregion
                    poseStack.popPose();
                }
                //#endregion
                poseStack.popPose();
            }
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

    public static void handleConsumerChange(NotifyConsumerChange notifyConsumerChange){
        if(TheatricalConfig.INSTANCE.CLIENT.artnetEnabled){
            TheatricalArtNetClient artNetClient = getArtNetManager().getClient();
            if(TheatricalConfig.INSTANCE.CLIENT.universes.containsKey(notifyConsumerChange.getUniverse())){
                UniverseConfig universeConfig = TheatricalConfig.INSTANCE.CLIENT.universes.get(notifyConsumerChange.getUniverse());
                DMXDevice dmxDevice = notifyConsumerChange.getDmxDevice();
                if(notifyConsumerChange.getChangeType() == NotifyConsumerChange.ChangeType.ADD){
                    artNetClient.addDevice((short) universeConfig.subnet,(short)  universeConfig.universe, dmxDevice.getDeviceId(), dmxDevice);
                } else if(notifyConsumerChange.getChangeType() == NotifyConsumerChange.ChangeType.UPDATE) {
                    artNetClient.updateDevice((short) universeConfig.subnet,(short)  universeConfig.universe, dmxDevice.getDeviceId(), dmxDevice);
                } else {
                    artNetClient.removeDevice((short) universeConfig.subnet,(short)  universeConfig.universe, dmxDevice.getDeviceId());
                }
            }
        }
    }

    public static void handleListConsumers(ListConsumers listConsumers){
        if(TheatricalConfig.INSTANCE.CLIENT.artnetEnabled) {
            TheatricalArtNetClient artNetClient = getArtNetManager().getClient();
            if(TheatricalConfig.INSTANCE.CLIENT.universes.containsKey(listConsumers.getUniverse())) {
                UniverseConfig universeConfig = TheatricalConfig.INSTANCE.CLIENT.universes.get(listConsumers.getUniverse());
                for (DMXDevice dmxDevice : listConsumers.getDmxDevices()) {
                    artNetClient.addDevice((short) universeConfig.subnet, (short) universeConfig.universe, dmxDevice.getDeviceId(), dmxDevice);
                }
            }
        }
    }
}
