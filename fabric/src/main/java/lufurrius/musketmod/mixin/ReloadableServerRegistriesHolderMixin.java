package lufurrius.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lufurrius.musketmod.ILootTableId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(ReloadableServerRegistries.Holder.class)
abstract class ReloadableServerRegistriesHolderMixin {
    private Identifier location;

    @Inject(method = "getLootTable", at = @At("HEAD"))
    private void getLootTableHead(ResourceKey<LootTable> key, CallbackInfoReturnable<LootTable> ci) {
        location = key.identifier();
    }

    @Inject(method = "getLootTable", at = @At("RETURN"))
    private void getLootTable(CallbackInfoReturnable<LootTable> ci) {
        ((ILootTableId)ci.getReturnValue()).setLocation(location);
    }
}
