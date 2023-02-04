package dev.imabad.theatrical.api.dmx;

public interface DMXConsumer {

    int getChannelCount();

    int getChannelStart();

    void consume(byte[] dmxValues);

}
