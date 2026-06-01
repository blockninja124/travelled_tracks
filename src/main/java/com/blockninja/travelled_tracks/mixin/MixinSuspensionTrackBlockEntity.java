package com.blockninja.travelled_tracks.mixin;

import com.blockninja.travelled_tracks.TRMTCompat;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import edn.stratodonut.trackwork.TrackworkUtil;
import edn.stratodonut.trackwork.tracks.blocks.SuspensionTrackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import static edn.stratodonut.trackwork.tracks.forces.SimpleWheelController.UP;
import static org.valkyrienskies.mod.api.ValkyrienSkies.toMinecraft;

@Mixin(SuspensionTrackBlockEntity.class)
public abstract class MixinSuspensionTrackBlockEntity extends BlockEntity {

    @Shadow
    private float wheelTravel;

    @Unique
    private BlockPos travelled_tracks$lastGroundPos = null;

    @Shadow
    public abstract float getSpeed();

    public MixinSuspensionTrackBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void injectTick(CallbackInfo ci) {
        if (Math.abs(this.getSpeed()) < 8) return;

        Vector3d pos = VectorConversionsMCKt.toJOML(Vec3.atBottomCenterOf(this.getBlockPos()));
        Vector3dc ground = VSGameUtilsKt.getWorldCoordinates(this.level, this.getBlockPos(), pos.sub(UP.mul(this.wheelTravel * 1.2, new Vector3d())));
        BlockPos blockpos = BlockPos.containing(toMinecraft(ground));
        if (blockpos.equals(travelled_tracks$lastGroundPos)) {
            return;
        }
        travelled_tracks$lastGroundPos = blockpos;

        Vector3d dirEstimate = TrackworkUtil.getForwardVec3d(this.getBlockState().getValue(RotatedPillarKineticBlock.AXIS), this.getSpeed());
        Direction blockDir = Direction.getNearest(dirEstimate.x, dirEstimate.y, dirEstimate.z);
        Ship ship = ValkyrienSkies.getShipManagingBlock(this.level, blockpos);
        if (ship != null) {
            Vector3d vecDir = VectorConversionsMCKt.toJOMLD(blockDir.getNormal());
            ship.getTransform().getShipToWorldRotation().transform(vecDir);
            blockDir = Direction.getNearest(vecDir.x, vecDir.y, vecDir.z);
        }

        TRMTCompat.weatherGround(this.level, blockpos, true, blockDir);
    }
}
