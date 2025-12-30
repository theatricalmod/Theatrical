package dev.imabad.theatrical.client.gui.widgets;

import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import dev.imabad.theatrical.config.UniverseConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ArtNetUniverseConfigurationList extends ObjectSelectionList<ArtNetUniverseConfigurationList.Entry> implements LayoutElement {

    private final ArtNetConfigurationScreen parent;
    public ArtNetUniverseConfigurationList(Minecraft minecraft, ArtNetConfigurationScreen screen, int width, int height, Component title) {
        super(minecraft, width, height, height - 55 + 4, 30);
        this.parent = screen;
    }

    public void setEntries(Map<Integer, UniverseConfig> configs){
        replaceEntries(configs.entrySet().stream().map(integerUniverseConfigEntry
                -> new Entry(parent, integerUniverseConfigEntry.getKey(),
                integerUniverseConfigEntry.getValue())).toList());
        repositionEntries();
    }


    @Override
    protected int scrollBarX() {return this.getX() + this.getRowWidth() + 6;
    }

    @Override
    public int getRowWidth() {
        return width - 10;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public static class Entry extends ObjectSelectionList.Entry<Entry> implements AutoCloseable {

        private final ArtNetConfigurationScreen parent;
        private final UniverseConfig config;
        private final int networkUniverse;
        private final StringWidget nameWidget;
        public Entry(ArtNetConfigurationScreen parent, int networkUniverse, UniverseConfig config) {
            this.parent = parent;
            this.config = config;
            this.networkUniverse = networkUniverse;
            this.nameWidget = new StringWidget(Component.translatable("screen.artnetconfig.entry.universe", networkUniverse), parent.getFont());
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
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
            this.parent.setSelected(this);
            return super.mouseClicked(mouseButtonEvent, bl);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
            nameWidget.setPosition(this.getContentX() + 2, this.getContentY() + 1);
            nameWidget.render(guiGraphics, i, j, f);
        }
    }
}
