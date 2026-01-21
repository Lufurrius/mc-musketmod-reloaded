package lufurrius.musketmod;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ClientUtilities {
    public static void registerItemProperties() {
        // Item model property registration moved to data-driven model properties in 1.21.11.
    }

    public static void handleSmokeEffectPacket(SmokeEffectPacket packet) {
        Minecraft instance = Minecraft.getInstance();
        ClientLevel level = instance.level;
        Vec3 origin = new Vec3(packet.origin());
        Vec3 direction = new Vec3(packet.direction());
        GunItem.fireParticles(level, origin, direction);
    }

    // for mixins
    public static boolean canUseScope;
    public static boolean attackKeyDown;
    public static boolean preventFiring;

    public static void setScoping(Player player, boolean scoping) {
        if (scoping != ScopedMusketItem.isScoping) {
            player.playSound(
                scoping ? SoundEvents.SPYGLASS_USE : SoundEvents.SPYGLASS_STOP_USING,
                1.0f, 1.0f);
            ScopedMusketItem.isScoping = scoping;
        }
        if (!scoping) ScopedMusketItem.recoilTicks = 0;
    }

    public static boolean poseArm(HumanoidRenderState state, HumanoidModel<? extends HumanoidRenderState> model, ModelPart arm) {
        if (state.isUsingItem) {
            return false;
        }

        boolean isRight = arm == model.rightArm;
        boolean isMainHand = (state.mainArm == HumanoidArm.RIGHT) == isRight;
        ItemStack stack = isRight ? state.rightHandItemStack : state.leftHandItemStack;
        if (stack.getItem() instanceof GunItem && shouldAim(state, stack, isMainHand)) {
            arm.xRot = model.head.xRot + 0.1f - Mth.HALF_PI;
            if (state.isCrouching) arm.xRot -= 0.4f;
            arm.yRot = model.head.yRot + (isRight ? -0.3f : 0.3f);
            return true;
        }

        ItemStack stack2 = isRight ? state.leftHandItemStack : state.rightHandItemStack;
        boolean isMainHand2 = !isMainHand;
        if (stack2.getItem() instanceof GunItem gun2 && shouldAim(state, stack2, isMainHand2)
        && (gun2.twoHanded() || stack.isEmpty())) {
            arm.xRot = model.head.xRot - 1.5f;
            if (state.isCrouching) arm.xRot -= 0.4f;
            arm.yRot = model.head.yRot + (isRight ? -0.6f : 0.6f);
            return true;
        }

        return false;
    }

    public static boolean shouldAim(HumanoidRenderState state, ItemStack stack, boolean isMainHand) {
        if (state.isUsingItem) return false;
        if (!(stack.getItem() instanceof GunItem gun)) return false;
        if (!isMainHand && gun.twoHanded()) return false;

        if (!isMainHand) {
            ItemStack mainStack = state.mainArm == HumanoidArm.RIGHT ? state.rightHandItemStack : state.leftHandItemStack;
            if (mainStack.getItem() instanceof GunItem mainGun && mainGun.twoHanded()) return false;
        }

        return GunItem.isLoaded(stack) || Config.alwaysAim;
    }

    public static boolean isHoldingGun(HumanoidRenderState state) {
        return isGun(state.leftHandItemStack) || isGun(state.rightHandItemStack);
    }

    private static boolean isGun(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    public static boolean shouldAim(LivingEntity entity, ItemStack stack, InteractionHand hand) {
        if (entity.isUsingItem()) return false;
        if (entity instanceof Mob mob) return mob.isAggressive() || (mob instanceof Pillager);

        return ((GunItem)stack.getItem()).canUseFrom(entity, hand)
            && (GunItem.isLoaded(stack) || Config.alwaysAim);
    }

    public static boolean shouldAim(LivingEntity entity, ItemStack stack) {
        InteractionHand hand = stack == entity.getMainHandItem()
            ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return shouldAim(entity, stack, hand);
    }

    public static void renderGunInHand(ItemInHandRenderer renderer, AbstractClientPlayer player, InteractionHand hand, float dt, float pitch, float swingProgress, float equipProgress, ItemStack stack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        if (player.isScoping()) {
            return;
        }

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isRightHand = arm == HumanoidArm.RIGHT;
        float sign = isRightHand ? 1 : -1;

        GunItem gun = (GunItem)stack.getItem();
        if (!gun.canUseFrom(player, hand)) {
            poseStack.pushPose();
            poseStack.translate(sign * 0.5, -0.5 - 0.6 * equipProgress, -0.7);
            poseStack.mulPose(Axis.XP.rotationDegrees(70));
            renderer.renderItem(player, stack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
            poseStack.popPose();
            return;
        }

        ItemStack activeStack = GunItem.getActiveStack(hand);
        if (stack == activeStack) {
            setEquipAnimationDisabled(hand, true);

        } else if (activeStack != null && activeStack.getItem() != gun) {
            setEquipAnimationDisabled(hand, false);
        }

        poseStack.pushPose();
        poseStack.translate(sign * 0.15, -0.25, -0.35);

        if (swingProgress > 0) {
            float swingSharp = Mth.sin(Mth.sqrt(swingProgress) * Mth.PI);
            float swingNormal = Mth.sin(swingProgress * Mth.PI);

            if (gun == Items.MUSKET_WITH_BAYONET) {
                poseStack.translate(sign * -0.05 * swingNormal, 0, 0.05 - 0.3 * swingSharp);
                poseStack.mulPose(Axis.YP.rotationDegrees(5 * swingSharp));
            } else {
                poseStack.translate(sign * 0.05 * (1 - swingNormal), 0.05 * (1 - swingNormal), 0.05 - 0.4 * swingSharp);
                poseStack.mulPose(Axis.XP.rotationDegrees(180 + sign * 20 * (1 - swingSharp)));
            }

        } else if (player.isUsingItem() && player.getUsedItemHand() == hand) {
            Pair<Integer, Integer> loadingDuration = GunItem.getLoadingDuration(stack);
            int loadingStages = loadingDuration.getLeft();
            int ticksPerLoadingStage = loadingDuration.getRight();

            float usingTicks = player.getTicksUsingItem() + dt - 1;
            int loadingStage = GunItem.getLoadingStage(stack) + (int)(usingTicks / ticksPerLoadingStage);
            int reloadDuration = GunItem.reloadDuration(stack);

            if (reloadDuration > 0 && usingTicks < reloadDuration + 5) {
                poseStack.translate(0, -0.3, 0.05);
                poseStack.mulPose(Axis.XP.rotationDegrees(60));
                poseStack.mulPose(Axis.ZP.rotationDegrees(sign * 10));

                float t = 0;
                // return
                if (usingTicks >= ticksPerLoadingStage && loadingStage <= loadingStages) {
                    usingTicks = usingTicks % ticksPerLoadingStage;
                    if (usingTicks < 4) {
                        t = (4 - usingTicks) / 4;
                    }
                }
                // hit down by ramrod
                if (usingTicks >= ticksPerLoadingStage - 2 && loadingStage < loadingStages) {
                    t = (usingTicks - ticksPerLoadingStage + 2) / 2;
                    t = Mth.sin(Mth.HALF_PI * Mth.sqrt(t));
                }
                poseStack.translate(0, 0, 0.025 * t);

                if (gun == Items.BLUNDERBUSS) {
                    poseStack.translate(0, 0, -0.06);
                } else if (gun == Items.PISTOL) {
                    poseStack.translate(0, 0, -0.12);
                }
            }
        } else {
            if (isEquipAnimationDisabled(hand)) {
                if (equipProgress == 0) {
                    setEquipAnimationDisabled(hand, false);
                    GunItem.setActiveStack(hand, null);
                }
            } else {
                poseStack.translate(0, -0.6 * equipProgress, 0);
            }
        }

        renderer.renderItem(player, stack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, submitNodeCollector, light);
        poseStack.popPose();
    }

    public static boolean disableMainHandEquipAnimation;
    public static boolean disableOffhandEquipAnimation;

    public static boolean isEquipAnimationDisabled(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return disableMainHandEquipAnimation;
        } else {
            return disableOffhandEquipAnimation;
        }
    }

    public static void setEquipAnimationDisabled(InteractionHand hand, boolean disabled) {
        if (hand == InteractionHand.MAIN_HAND) {
            disableMainHandEquipAnimation = disabled;
        } else {
            disableOffhandEquipAnimation = disabled;
        }
    }
}
