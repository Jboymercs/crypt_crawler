package com.unseen.cc.common.items;

import com.unseen.cc.util.ModUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * This base class is for any crafting material items that all we want to have in the desc is Crafting Material
 */
public class ItemCraftingMaterial extends ItemBase{
    public ItemCraftingMaterial(String name, CreativeTabs tab) {
        super(name, tab);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + ModUtils.translateDesc("material_desc"));
    }
}
