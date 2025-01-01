package dev.imabad.theatrical.client.gui.widgets;

import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import dev.imabad.theatrical.config.UniverseConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.Comparator;

public class ArtNetUniverseConfigurationList extends ConfigurationListWidget<ArtNetConfigurationScreen, Integer, UniverseConfig, ArtNetUniverseConfigurationList.Entry> {


    public ArtNetUniverseConfigurationList(Minecraft minecraft, ArtNetConfigurationScreen parentScreen, int width, int height, Component title) {
        super(minecraft, parentScreen, width, height, title);
    }

    @Override
    public Entry createEntry(ArtNetConfigurationScreen parent, Integer key, UniverseConfig value) {
        return new Entry(parent, key, value);
    }

    @Override
    public Comparator<Integer> getComparator() {
        return Comparator.naturalOrder();
    }

    @Environment(EnvType.CLIENT)
    public static class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {

        private final ArtNetConfigurationScreen parent;
        private UniverseConfig config;
        private int networkUniverse;
        public Entry(ArtNetConfigurationScreen parent, int networkUniverse, UniverseConfig config) {
            this.parent = parent;
            this.config = config;
            this.networkUniverse = networkUniverse;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        public void close() {
        }

        public UniverseConfig getConfig() {
            return config;
        }

        public int getNetworkUniverse() {
            return networkUniverse;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, Component.translatable("screen.artnetconfig.entry.universe", networkUniverse),  left, top + 1, 16777215 );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.parent.setSelected(this);
            return false;
        }
    }
}
