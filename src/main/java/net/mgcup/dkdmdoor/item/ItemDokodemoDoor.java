package net.mgcup.dkdmdoor.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemDoor;

public class ItemDokodemoDoor extends ItemDoor {

    public ItemDokodemoDoor(Block block) {
        super(block);
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
    }
}
