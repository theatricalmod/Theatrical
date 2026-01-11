package dev.imabad.theatrical.forge.compat.create;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DXMConsumerMovementBehaviour implements MovementBehaviour {


    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        MovementBehaviour.super.visitNewPosition(context, pos);
    }

    private void setupConsumer(MovementContext context){
        if(context.world.isClientSide()) return;
        if(context.temporaryData instanceof DMXConsumerInContraption) return;
        DMXConsumerInContraption dmxConsumerInContraption = new DMXConsumerInContraption(
                context.blockEntityData,
                context.localPos,
                context.contraption
        );
        TheatricalNetworkData
                .getInstance()
                .getNetwork(dmxConsumerInContraption.getNetworkId())
                .dmx().addConsumer(dmxConsumerInContraption);
        context.temporaryData = dmxConsumerInContraption;
    }

    @Override
    public void stopMoving(MovementContext context) {
        MovementBehaviour.super.stopMoving(context);
        if(context.world.isClientSide()) return;
        if(context.temporaryData instanceof DMXConsumerInContraption dmxConsumerInContraption){
            if(dmxConsumerInContraption.getLastData() != null) {
                context.blockEntityData.putByteArray("last_data", dmxConsumerInContraption.getLastData());
            }
            TheatricalNetworkData
                    .getInstance()
                    .getNetwork(dmxConsumerInContraption.getNetworkId())
                    .dmx().removeConsumer(dmxConsumerInContraption);
            context.temporaryData = null;
        }
    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        if(!context.world.isClientSide()){
            setupConsumer(context);
            return;
        }
        BlockEntity blockEntityClientSide = context.contraption.getBlockEntityClientSide(context.localPos);
        if(blockEntityClientSide instanceof BaseLightBlockEntity lightBlockEntity) {
            BaseLightBlockEntity.tickInContraption(context.contraption.getContraptionWorld(), lightBlockEntity,
                    context.position, (from, to, level, be) -> {
                        BlockHitResult blockHitResult = null;
                        Vec3 globalVector = Vec3.ZERO;
                        BlockPos globalBlockPos = null;
                        if(context.contraption.entity instanceof CarriageContraptionEntity carriageContraption) {
                            for (Carriage carriage : carriageContraption.getCarriage().train.carriages) {
                                CarriageContraptionEntity contraptionEntity = carriage.anyAvailableEntity();
                                blockHitResult = ContraptionHandlerClient.rayTraceContraption(from, to, contraptionEntity);
                                if(blockHitResult != null) {
                                    globalVector = contraptionEntity.toGlobalVector(blockHitResult.getLocation(), 1);
                                    globalBlockPos = BlockPos.containing(contraptionEntity.toGlobalVector(VecHelper.getCenterOf(blockHitResult.getBlockPos()), 1));
                                    break;
                                };
                            }
                        } else {
                            blockHitResult = ContraptionHandlerClient.rayTraceContraption(from, to, context.contraption.entity);
                            if(blockHitResult != null) {
                                globalVector = context.contraption.entity.toGlobalVector(blockHitResult.getLocation(), 1);
                                globalBlockPos = BlockPos.containing(context.contraption.entity.toGlobalVector(VecHelper.getCenterOf(blockHitResult.getBlockPos()), 1));
                            }
                        }
                        if(blockHitResult != null){
                            return new BlockHitResult(globalVector, blockHitResult.getDirection(), globalBlockPos, blockHitResult.isInside());
                        }
                        return BaseLightBlockEntity.DEFAULT_RAY_TRACE_SUPPLIER.doRayTrace(from, to, level, be);
                    });
        }
    }
}
