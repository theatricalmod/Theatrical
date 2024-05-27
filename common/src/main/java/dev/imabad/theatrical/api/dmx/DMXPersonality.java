package dev.imabad.theatrical.api.dmx;

import java.util.ArrayList;
import java.util.List;

public class DMXPersonality {
    private final int channelCount;
    private final String description;
    private final List<DMXSlot> slots;

    public DMXPersonality(int channelCount, String description){
        this.channelCount = channelCount;
        this.description = description;
        this.slots = new ArrayList<>();
    }

    public DMXPersonality addSlot(DMXSlot slot){
        slots.add(slot);
        return this;
    }

    public DMXPersonality addSlot(int position, DMXSlot slot){
        slots.add(position, slot);
        return this;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public String getDescription() {
        return description;
    }

    public List<DMXSlot> getSlots() {
        return slots;
    }
}
