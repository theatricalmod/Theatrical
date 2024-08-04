package dev.imabad.theatrical.forge.compat.create;

import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.net.compat.create.SendBEDataToContraption;
import net.minecraft.client.Minecraft;
import java.lang.ref.WeakReference;
import java.util.Map;

public class CreateCompat {

    public static void init(){
        AllMovementBehaviours.registerBehaviourProvider(state -> {
            if(state.getBlock() instanceof MovingLightBlock){
                return new MovingLightMovingBehaviour();
            }
            return null;
        });
    }

    public static void handleBEDataForContraption(SendBEDataToContraption packet) {
        Map<Integer, WeakReference<AbstractContraptionEntity>> integerWeakReferenceMap = ContraptionHandler.loadedContraptions.get(Minecraft.getInstance().level);
        if(integerWeakReferenceMap.containsKey(packet.getEntityId())){
            AbstractContraptionEntity abstractContraptionEntity = integerWeakReferenceMap.get(packet.getEntityId()).get();
            if(abstractContraptionEntity != null) {
                abstractContraptionEntity
                        .getContraption().presentBlockEntities.get(packet.getPosInContraption()).load(packet.getBlockEntityData());
            }
        }
    }

}
