package dev.imabad.theatrical.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record ConfigurationCardData(UUID network, int dmxUniverse, int dmxAddress, boolean autoIncrement,
                                    boolean universeEnabled, boolean addressEnabled) {

    public static final Codec<ConfigurationCardData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("network").forGetter(ConfigurationCardData::network),
                    Codec.INT.fieldOf("dmxUniverse").forGetter(ConfigurationCardData::dmxUniverse),
                    Codec.INT.fieldOf("dmxAddress").forGetter(ConfigurationCardData::dmxAddress),
                    Codec.BOOL.fieldOf("autoIncrement").forGetter(ConfigurationCardData::autoIncrement),
                    Codec.BOOL.fieldOf("universeEnabled").forGetter(ConfigurationCardData::universeEnabled),
                    Codec.BOOL.fieldOf("addressEnabled").forGetter(ConfigurationCardData::addressEnabled)
            ).apply(instance, ConfigurationCardData::new)
    );


    public static final StreamCodec<FriendlyByteBuf, ConfigurationCardData> STREAM_CODEC =
            StreamCodec.ofMember(ConfigurationCardData::encode, ConfigurationCardData::new);

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(network);
        buf.writeInt(dmxUniverse);
        buf.writeInt(dmxAddress);
        buf.writeBoolean(autoIncrement);
        buf.writeBoolean(universeEnabled);
        buf.writeBoolean(addressEnabled);
    }

    private ConfigurationCardData(FriendlyByteBuf buf){
        this(buf.readUUID(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public ConfigurationCardData increment(int amountToIncrement){
        return new ConfigurationCardData(
                network, dmxUniverse, dmxAddress + amountToIncrement, autoIncrement, universeEnabled, addressEnabled
        );
    }

    // Codec
    // StreamCodec

}
