package lufurrius.musketmod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import lufurrius.musketmod.GunItem;
import lufurrius.musketmod.VanillaHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.extensions.IItemExtension;

@Mixin(GunItem.class)
abstract class GunItemMixin implements IItemExtension {
    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return VanillaHelper.canEnchant(enchantment, stack)
            || enchantment.value().isSupportedItem(stack);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return VanillaHelper.canEnchant(enchantment, stack)
            || enchantment.value().isSupportedItem(stack);
    }
}
