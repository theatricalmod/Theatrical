package dev.imabad.theatrical.client.gui.widgets.audio;

import dev.imabad.theatrical.api.network.audio.AudioChannelDefinition;
import dev.imabad.theatrical.client.gui.screen.audio.ConfigureMixerRoutingScreen;
import dev.imabad.theatrical.client.gui.widgets.ConfigurationListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.Comparator;

public class MixerChannelListWidget extends ConfigurationListWidget<ConfigureMixerRoutingScreen, Integer, AudioChannelDefinition, MixerChannelListWidget.Entry>
{

    public MixerChannelListWidget(Minecraft minecraft, ConfigureMixerRoutingScreen parentScreen, int width, int height, Component title) {
        super(minecraft, parentScreen, width, height, title);
    }

    @Override
    public Entry createEntry(ConfigureMixerRoutingScreen parent, Integer key, AudioChannelDefinition value) {
        return new Entry(parent, key, value);
    }

    @Override
    public Comparator<Integer> getComparator() {
        return Comparator.naturalOrder();
    }

    @Environment(EnvType.CLIENT)
    public static class Entry extends ObjectSelectionList.Entry<MixerChannelListWidget.Entry> implements AutoCloseable {
        private final ConfigureMixerRoutingScreen parent;
        private final int channel;
        private final AudioChannelDefinition audioDeviceChannel;

        public Entry(ConfigureMixerRoutingScreen parent, int channel, AudioChannelDefinition audioDeviceChannel) {
            this.parent = parent;
            this.channel = channel;
            this.audioDeviceChannel = audioDeviceChannel;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        public void close() {
        }

        public int getChannel() {
            return channel;
        }

        public AudioChannelDefinition getAudioDeviceChannel() {
            return audioDeviceChannel;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, Component.literal(audioDeviceChannel.name()), left, top + 1, 16777215 );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.parent.setSelected(this);
            return false;
        }
    }
}
