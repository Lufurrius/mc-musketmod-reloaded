package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(SkeletonModel.class)
abstract class SkeletonModelMixin {
    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void setupAnimHead(SkeletonRenderState state, CallbackInfo ci) {
        if (ClientUtilities.isHoldingGun(state) && state.isUsingItem) {
            if (state.mainArm == HumanoidArm.RIGHT) {
                state.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            } else {
                state.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
        }
    }
}
