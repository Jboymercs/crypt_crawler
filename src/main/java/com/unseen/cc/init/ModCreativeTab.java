package com.unseen.cc.init;

import com.unseen.cc.util.tab.CryptCrawlerCreativeTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;

public class ModCreativeTab {
    public static CreativeTabs ITEMS = new CryptCrawlerCreativeTab(CreativeTabs.getNextID(), "crypt_crawler", () -> Items.STONE_SWORD);
}
