package dev.imabad.theatrical.fixtures;

import ch.bildspur.artnet.rdm.RDMSlotID;
import ch.bildspur.artnet.rdm.RDMSlotType;
import dev.imabad.theatrical.api.dmx.DMXSlot;

public class SharedSlots {

    public static final DMXSlot INTENSITY = new DMXSlot("Intensity",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_INTENSITY);
    public static final DMXSlot RED = new DMXSlot("Red",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_COLOR_SUB_CYAN);
    public static final DMXSlot GREEN = new DMXSlot("Green",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_COLOR_SUB_MAGENTA);
    public static final DMXSlot BLUE = new DMXSlot("Blue",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_COLOR_SUB_YELLOW);
    public static final DMXSlot PAN = new DMXSlot("Pan",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_PAN);
    public static final DMXSlot TILT = new DMXSlot("Tilt",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_TILT);
    public static final DMXSlot FOCUS = new DMXSlot("Focus",RDMSlotType.ST_PRIMARY, RDMSlotID.SD_BEAM_SIZE_IRIS);

}
