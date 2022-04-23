package net.mgcup.dkdmdoor.util;

import net.mgcup.dkdmdoor.DokodemoDoorMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class RegistryUtil {

    public static Item setItemName(final Item item, String name) {
        item.setRegistryName(DokodemoDoorMod.MODID, name).setUnlocalizedName(DokodemoDoorMod.MODID + "." + name);
        return item;
    }

    public static Block setBlockName(final Block block, String name) {
        block.setRegistryName(DokodemoDoorMod.MODID, name).setUnlocalizedName(DokodemoDoorMod.MODID + "." + name);
        return block;
    }

}
