package dev.imabad.theatrical.blockentities.control;

import dev.imabad.theatrical.api.dmx.BelongsToNetwork;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class BasicLightingDeskBlockEntity extends ClientSyncBlockEntity implements BelongsToNetwork {
    public static class StoredCue {
        private byte[] faders;
        private int fadeInTicks;
        private int fadeOutTicks;

        public StoredCue(){}

        public StoredCue(byte[] faders, int fadeInTicks, int fadeOutTicks){
            this.faders = faders;
            this.fadeInTicks = fadeInTicks;
            this.fadeOutTicks = fadeOutTicks;
        }

        public CompoundTag toNBT(){
            CompoundTag compoundNBT = new CompoundTag();
            compoundNBT.putByteArray("faders", faders);
            compoundNBT.putInt("fadeIn", fadeInTicks);
            compoundNBT.putInt("fadeOut", fadeOutTicks);
            return compoundNBT;
        }

        public StoredCue fromNBT(CompoundTag nbt){
            this.faders = nbt.getByteArray("faders");
            this.fadeInTicks = nbt.getInt("fadeIn");
            this.fadeOutTicks = nbt.getInt("fadeOut");
            return this;
        }

        public byte[] getFaders() {
            return faders;
        }

        public int getFadeInTicks() {
            return fadeInTicks;
        }

        public int getFadeOutTicks() {
            return fadeOutTicks;
        }
    }

    private int ticks = 0;
    private byte[] faders = new byte[12];
    private final byte[] actualDMX = new byte[12];
    private int currentStep = 0;
    private HashMap<Integer, StoredCue> storedSteps = new HashMap<>();
    private StoredCue activeCue;

    private boolean isRunMode = false;
    private int fadeInTicks = 0;
    private int fadeOutTicks = 0;

    private int fadeInTicksRemaining = 0;
    private int fadeOutTicksRemaining = 0;
    private byte[] perTickOut, perTickIn;
    private boolean isFadingOut = false;
    private byte grandMaster = -1;

    private UUID networkId;
    private int universe = 0;

    public BasicLightingDeskBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.BASIC_LIGHTING_DESK.get(), blockPos, blockState);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if(level.isClientSide){
            return;
        }
        if(be instanceof BasicLightingDeskBlockEntity beL){
            beL.tick();
        }
    }

    public void tick(){
        ticks++;
        if(ticks >= 1){
            if(isFadingOut){
                if(fadeOutTicksRemaining > 0) {
                    fadeOutTicksRemaining--;
                    this.doFadeTickOut();
                } else {
                    isFadingOut = false;
                }
            } else {
                if(fadeInTicksRemaining > 0){
                    fadeInTicksRemaining--;
                    this.doFadeTickIn();
                }
            }
            ticks = 0;
            byte[] dmx = new byte[512];
            for(int i = 0; i < faders.length; i++){
                dmx[i] = (byte) (convertByteToInt(faders[i]) * (convertByteToInt(grandMaster) / 255F));
            }
            update(dmx);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void update(byte[] data) {
        if(level != null && level.getServer() != null) {
            var dmxData = DMXNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
            if(dmxData != null) {
                dmxData.getConsumers(universe).forEach(consumer -> consumer.consume(data));
            }
        }
    }


    public float convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

    public byte[] getFaders() {
        return this.faders;
    }

    @Override
    public void write(CompoundTag compoundTag) {
        if(compoundTag == null){
            compoundTag = new CompoundTag();
        }
        compoundTag.putByteArray("faders", faders);
        CompoundTag compoundNBT = new CompoundTag();
        for(int key : storedSteps.keySet()){
            compoundNBT.put(Integer.toString(key), storedSteps.get(key).toNBT());
        }
        compoundTag.put("storedSteps", compoundNBT);
        compoundTag.putInt("currentStep", currentStep);
        compoundTag.putByte("grandMaster", grandMaster);
        compoundTag.putBoolean("isRunMode", isRunMode);
        compoundTag.putInt("fadeInTicks", fadeInTicks);
        compoundTag.putInt("fadeOutTicks", fadeOutTicks);
        if(networkId != null){
            compoundTag.putUUID("network", networkId);
        }
        compoundTag.putInt("universe", universe);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        if(compoundTag.contains("faders")){
            faders = compoundTag.getByteArray("faders");
        }
        if(compoundTag.contains("storedSteps")){
            storedSteps = new HashMap<>();
            CompoundTag compoundNBT = compoundTag.getCompound("storedSteps");
            for(String key : compoundNBT.getAllKeys()){
                int stepNumber = Integer.parseInt(key);
                storedSteps.put(stepNumber, new StoredCue().fromNBT(compoundNBT.getCompound(key)));
            }
        }
        if(compoundTag.contains("currentStep")){
            currentStep = compoundTag.getInt("currentStep");
        }
        if(compoundTag.contains("grandMaster")){
            grandMaster = compoundTag.getByte("grandMaster");
        }
        if(compoundTag.contains("isRunMode")){
            isRunMode = compoundTag.getBoolean("isRunMode");
        }
        if(compoundTag.contains("fadeInTicks")){
            fadeInTicks = compoundTag.getInt("fadeInTicks");
        }
        if(compoundTag.contains("fadeOutTicks")){
            fadeOutTicks = compoundTag.getInt("fadeOutTicks");
        }
        if(compoundTag.contains("network")){
            networkId = compoundTag.getUUID("network");
        }
        if(compoundTag.contains("universe")){
            universe = compoundTag.getInt("universe");
        }
    }

    public void setFaders(byte[] faders){
        this.faders = Arrays.copyOf(faders, faders.length);
        setChanged();
    }

    public void setFader(int fader, int value){
        if(fader != -1) {
            this.faders[fader] = (byte) value;
        } else {
            this.grandMaster = (byte) value;
        }
        setChanged();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public byte getGrandMaster() {
        return grandMaster;
    }

    public boolean isRunMode() {
        return isRunMode;
    }

    public void toggleMode(){
        this.isRunMode = !this.isRunMode;
        setChanged();
    }

    public void clickButton(){
        if(isRunMode()){
            this.recallNextStep();
        } else {
            this.storeCurrentFaders();
        }
    }

    public void moveForward(){
        if(isRunMode){
            this.currentStep = this.getNextStep();
        } else {
            this.currentStep++;
            if(this.storedSteps.containsKey(this.currentStep)){
                byte[] faders = storedSteps.get(this.currentStep).getFaders();
                setFaders(faders);
            }
        }
    }

    public void moveBack(){
        if(this.isRunMode){
            this.currentStep = this.getPreviousStep();
        } else {
            if(this.currentStep - 1 < 0){
                return;
            }
            this.currentStep--;
            if(this.storedSteps.containsKey(this.currentStep)){
                byte[] faders = storedSteps.get(this.currentStep).getFaders();
                setFaders(faders);
            }
        }
    }

    public HashMap<Integer, StoredCue> getStoredSteps() {
        return storedSteps;
    }

    private void doFadeTickIn(){
        for(int i = 0; i < faders.length; i++) {
            this.faders[i] = (byte)(faders[i] - perTickIn[i]);
        }
    }
    private void doFadeTickOut(){
        for(int i = 0; i < faders.length; i++) {
            this.faders[i] = (byte)(faders[i] - perTickOut[i]);
        }
    }

    private void recallNextStep(){
        if(this.storedSteps.size() < this.currentStep){
            return;
        }
        StoredCue previousCue = activeCue;
        if(!this.storedSteps.containsKey(this.currentStep)){
            return;
        }
        StoredCue storedCue = storedSteps.get(this.currentStep);
        if(previousCue != null) {
            if (previousCue.fadeOutTicks > 0) {
                this.isFadingOut = true;
                this.fadeOutTicksRemaining = previousCue.getFadeOutTicks();
                this.perTickOut = new byte[12];
                for (int i = 0; i < faders.length; i++) {
                    this.perTickOut[i] = (byte)((convertByteToInt(faders[i])) / fadeOutTicksRemaining);
                }
            }
        }
        if(storedCue.fadeInTicks > 0){
            this.fadeInTicksRemaining = storedCue.getFadeInTicks();
            this.perTickIn = new byte[12];
            for(int i = 0; i < faders.length; i++) {
                if(isFadingOut){
                    this.perTickIn[i] = (byte)(-convertByteToInt(storedCue.getFaders()[i]) / fadeInTicksRemaining);
                } else {
                    this.perTickIn[i] = (byte)((convertByteToInt(faders[i]) - convertByteToInt(storedCue.getFaders()[i])) / fadeInTicksRemaining);
                }
            }
        } else {
            byte[] faders = storedCue.getFaders();
            setFaders(faders);
        }
        activeCue = storedCue;
        this.currentStep = getNextStep();
        setChanged();
    }

    private Integer getFirst(){
        return this.storedSteps.keySet().stream().min(Integer::compareTo).get();
    }

    private Integer getNextStep(){
        if(this.storedSteps.size() > 0) {
            Optional<Integer> nextSteps = this.storedSteps.keySet().stream().filter(integer -> integer > this.currentStep).min(Integer::compareTo);
            return nextSteps.orElseGet(this::getFirst);
        } else {
            return currentStep;
        }
    }

    private Integer getPreviousStep(){
        if(this.storedSteps.size() > 0) {
            Optional<Integer> nextSteps = this.storedSteps.keySet().stream().filter(integer -> integer < this.currentStep).sorted(Integer::compareTo).max(Comparator.naturalOrder());
            return nextSteps.orElseGet(() -> this.storedSteps.keySet().stream().sorted(Integer::compareTo).max(Comparator.naturalOrder()).get());
        } else {
            return currentStep;
        }
    }

    private void storeCurrentFaders(){
        StoredCue storedCue = new StoredCue(Arrays.copyOf(faders, faders.length), this.fadeInTicks, this.fadeOutTicks);
        storedSteps.put(this.currentStep, storedCue);
        this.currentStep++;
        setChanged();
    }

    public int getFadeInTicks() {
        return fadeInTicks;
    }

    public void setFadeInTicks(int fadeInTicks) {
        this.fadeInTicks = fadeInTicks;
    }

    public int getFadeOutTicks() {
        return fadeOutTicks;
    }

    public void setFadeOutTicks(int fadeOutTicks) {
        this.fadeOutTicks = fadeOutTicks;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public void setNetworkId(UUID newNetworkId) {
        if(newNetworkId == this.networkId){
            return;
        }
        this.networkId = newNetworkId;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
