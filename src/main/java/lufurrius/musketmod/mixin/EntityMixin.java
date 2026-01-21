package lufurrius.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lufurrius.musketmod.GunItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void spawnAtLocation(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemEntity> ci) {
        Object object = this;
        if (stack.getItem() == Items.ARROW
        && (object instanceof AbstractSkeleton entity) && GunItem.isHoldingGun(entity)) {
            ItemStack cartridges = new ItemStack(lufurrius.musketmod.Items.CARTRIDGE, stack.getCount());
            ci.setReturnValue(entity.spawnAtLocation(level, cartridges, 0.0F));
            ci.cancel();
        }
    }
}
