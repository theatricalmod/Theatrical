package dev.imabad.theatrical.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class BasicSlider extends AbstractSliderButton {

    private Component initialMessage;
    private double minValue, maxValue;
    private final Consumer<Double> applyValue;
    public BasicSlider(int x, int y, int width, int height, Component message, double value, double minValue, double maxValue, Consumer<Double> applyValue) {
        super(x, y, width, height, message, value);
        this.initialMessage = message;
        this.applyValue = applyValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
        updateMessage();
    }
    public double getValue() {
        return Math.round(this.value * (maxValue - minValue) + minValue);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.empty().append(getValue() + ""));
    }

    @Override
    protected void applyValue() {
        applyValue.accept(getValue());
    }
}
