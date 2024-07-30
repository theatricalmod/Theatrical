package dev.imabad.theatrical.api.dmx;

import ch.bildspur.artnet.rdm.RDMSlotID;
import ch.bildspur.artnet.rdm.RDMSlotType;

public record DMXSlot(String label, RDMSlotType slotType, RDMSlotID slotID) {
}
