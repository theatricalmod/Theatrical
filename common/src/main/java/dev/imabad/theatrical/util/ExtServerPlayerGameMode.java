package dev.imabad.theatrical.util;

public interface ExtServerPlayerGameMode {

    boolean hasCapturedBlockEntity();
    void setCapturedBlockEntity(boolean hasCapturedBlockEntity);

    boolean shouldCaptureSentBlockEntities();
    void setCaptureSentBlockEntities(boolean shouldCaptureSentBlockEntities);

}
