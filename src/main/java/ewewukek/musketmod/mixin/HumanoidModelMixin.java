package ewewukek.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ewewukek.musketmod.ClientUtilities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

@Mixin(HumanoidModel.class)
abstract class HumanoidModelMixin {
    private HumanoidRenderState state;
    private ArmPose leftArmPose;
    private ArmPose rightArmPose;

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void setupAnimHead(HumanoidRenderState state, CallbackInfo ci) {
        this.state = state;
        leftArmPose = state.leftArmPose;
        rightArmPose = state.rightArmPose;
    }

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    private void poseRightArm(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        if (ClientUtilities.poseArm(state, model, model.rightArm)) {
            state.rightArmPose = ArmPose.SPYGLASS; // to disable AnimationUtils.bobModelPart call
        }
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    private void poseLeftArm(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
        if (ClientUtilities.poseArm(state, model, model.leftArm)) {
            state.leftArmPose = ArmPose.SPYGLASS; // to disable AnimationUtils.bobModelPart call
        }
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void setupAnimTail(CallbackInfo ci) {
        if (state != null) {
            state.rightArmPose = rightArmPose;
            state.leftArmPose = leftArmPose;
        }
    }
}
