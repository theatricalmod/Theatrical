package dev.imabad.theatrical.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ConfigurationListWidget<P extends Screen, K, O, E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> implements LayoutElement {

    private final P parent;
    public ConfigurationListWidget(Minecraft minecraft, P parentScreen, int width, int height, Component title) {
     this(minecraft, parentScreen, width, height,title, 30);
    }

    public ConfigurationListWidget(Minecraft minecraft, P parentScreen, int width, int height, Component title, int itemHeight) {
        super(minecraft, width, height, 32, height - 55 + 4, itemHeight);
        this.parent = parentScreen;
        this.setRenderBackground(true);
        this.setRenderHeader(false, 0);
    }

    public void setSelectedEntry(E entry) {
        setFocused(entry);
        if(entry == null){
            setSelected(null);
        }
    }

    public abstract E createEntry(P parent, K key, O value);
    public abstract Comparator<K> getComparator();


    public void setEntries(Map<K, O> entries){
        this.clearEntries();
        entries.keySet().stream().sorted(getComparator()).forEach((key) -> addEntry(createEntry(parent, key, entries.get(key))));
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

}
