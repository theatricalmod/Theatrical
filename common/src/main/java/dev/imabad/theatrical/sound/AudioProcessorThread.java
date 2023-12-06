package dev.imabad.theatrical.sound;

import net.minecraft.core.BlockPos;

public class AudioProcessorThread extends Thread {

    private final BlockPos speakerPos;
    private boolean running;

    public AudioProcessorThread(BlockPos speakerPos){
        this.speakerPos = speakerPos;
        this.running = true;
    }

    @Override
    public void run() {
        while(running){

        }
    }
}
