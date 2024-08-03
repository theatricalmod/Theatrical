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
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ArtNetUniverseConfigurationList extends ObjectSelectionList<ArtNetUniverseConfigurationList.Entry> implements LayoutElement {

    private ArtNetConfigurationScreen parent;
    public ArtNetUniverseConfigurationList(Minecraft minecraft, ArtNetConfigurationScreen screen, int width, int height, Component title) {
        super(minecraft, width, height, 32, height - 55 + 4, 30);
        this.parent = screen;
        this.setRenderBackground(true);
        this.setRenderHeader(false, 0);
        this.setRenderTopAndBottom(false);
    }

    public void setEntries(Map<Integer, UniverseConfig> configs){
        this.clearEntries();
        configs.forEach((key, value) -> addEntry(new Entry(parent, key, value)));
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.getRowWidth() + 6;
    }

    @Override
    public int getRowWidth() {
        return width - 10;
    }

    @Override
    public void setX(int x) {
        setLeftPos(x);
    }

    @Override
    public void setY(int y) {
        this.y0 = y;
        this.y1 = y + height;
    }

    @Override
    public int getX() {
        return x0;
    }

    @Override
    public int getY() {
        return y0;
    }

    @Override
    public int getWidth() {
        return x1 - x0;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
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
//            guiGraphics.drawString(font, Component.translatable("screen.artnetconfig.entry.subnet", config.getSubnet()),  left, top + 1, 16777215 );
//            guiGraphics.drawString(font, Component.translatable("screen.artnetconfig.entry.universe", config.getUniverse()),  left, top + 4 + font.lineHeight, 16777215 );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.parent.setSelected(this);
            return false;
        }
    }
}
