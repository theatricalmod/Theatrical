package dev.imabad.theatrical.client.gui.widgets.audio;

import dev.imabad.theatrical.api.network.audio.AudioChannelDefinition;
import dev.imabad.theatrical.api.network.audio.AudioDeviceDefinition;
import dev.imabad.theatrical.api.network.audio.AudioNetworkDevice;
import dev.imabad.theatrical.client.gui.screen.audio.ConfigureMixerRoutingScreen;
import dev.imabad.theatrical.client.gui.widgets.ConfigurationListWidget;
import dev.imabad.theatrical.util.DimensionBlockPos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.Comparator;

public class MixerDeviceListWidget extends ConfigurationListWidget<ConfigureMixerRoutingScreen, DimensionBlockPos, AudioDeviceDefinition, MixerDeviceListWidget.Entry>
{

    public MixerDeviceListWidget(Minecraft minecraft, ConfigureMixerRoutingScreen parentScreen, int width, int height, Component title) {
        super(minecraft, parentScreen, width, height, title, 40);
    }

    @Override
    public MixerDeviceListWidget.Entry createEntry(ConfigureMixerRoutingScreen parent, DimensionBlockPos key, AudioDeviceDefinition value) {
        return new MixerDeviceListWidget.Entry(parent, key, value);
    }

    @Override
    public Comparator<DimensionBlockPos> getComparator() {
        return Comparator.comparing(DimensionBlockPos::pos);
    }

    @Environment(EnvType.CLIENT)
    public static class Entry extends ObjectSelectionList.Entry<MixerDeviceListWidget.Entry> implements AutoCloseable {
        private final ConfigureMixerRoutingScreen parent;

        private final DimensionBlockPos pos;
        private final AudioDeviceDefinition device;

        public Entry(ConfigureMixerRoutingScreen parent, DimensionBlockPos pos, AudioDeviceDefinition device) {
            this.parent = parent;
            this.pos = pos;
            this.device = device;
        }

        public DimensionBlockPos getPos() {
            return pos;
        }

        public AudioDeviceDefinition getDevice() {
            return device;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        public void close() {
        }
        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, Component.literal(device.name()), left, top + 1, 16777215 );
            int dimensionNameHeight = top + 1 + font.lineHeight + 2;
            guiGraphics.drawString(font, Component.literal(pos.dimension().location().toString()), left, dimensionNameHeight, 16777215 );
            guiGraphics.drawString(font, Component.literal(pos.pos().toShortString()), left, dimensionNameHeight + font.lineHeight + 2, 16777215);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.parent.setSelectedDevice(this);
            return false;
        }
    }
}
