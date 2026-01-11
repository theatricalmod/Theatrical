package dev.imabad.theatrical.forge.compat.create;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import dev.imabad.theatrical.api.Hangable;
import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.net.compat.create.SendBEDataToContraption;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;
import java.util.Map;

public class CreateCompat {

    public static void init(){
        MovementBehaviour.REGISTRY.registerProvider(blockType -> {
            if(blockType instanceof BaseLightBlock){
                return new DXMConsumerMovementBehaviour();
            }
            return null;
        });
        BlockMovementChecks.registerAttachedCheck((offsetState, world, offsetPos, oppositeDirection) -> {
            if(offsetState.getBlock() instanceof Hangable ){
                return BlockMovementChecks.CheckResult.SUCCESS;
            }
            BlockPos potentialSupportPos = offsetPos.relative(oppositeDirection);
            BlockState potentialSupport = world.getBlockState(potentialSupportPos);
            if(potentialSupport.getBlock() instanceof Support support){
                return BlockMovementChecks.CheckResult.of(support.isAttachedTo(world, potentialSupportPos,
                        potentialSupport, oppositeDirection.getOpposite()));
            }
            return BlockMovementChecks.CheckResult.PASS;
        });
        BlockMovementChecks.registerBrittleCheck(state -> {
            if(state.getBlock() instanceof BaseLightBlock){
                return BlockMovementChecks.CheckResult.FAIL;
            }
            return BlockMovementChecks.CheckResult.PASS;
        });
    }

    public static void handleBEDataForContraption(SendBEDataToContraption packet) {
        Map<Integer, WeakReference<AbstractContraptionEntity>> integerWeakReferenceMap = ContraptionHandler.loadedContraptions.get(Minecraft.getInstance().level);
        if(integerWeakReferenceMap.containsKey(packet.getEntityId())){
            AbstractContraptionEntity abstractContraptionEntity = integerWeakReferenceMap.get(packet.getEntityId()).get();
            if(abstractContraptionEntity != null) {
                BlockEntity blockEntityClientSide = abstractContraptionEntity
                        .getContraption().getBlockEntityClientSide(packet.getPosInContraption());
                if(blockEntityClientSide instanceof BaseDMXConsumerLightBlockEntity be){
                    be.consume(packet.getDmxData(), true);
                }
            }
        }
    }

}
