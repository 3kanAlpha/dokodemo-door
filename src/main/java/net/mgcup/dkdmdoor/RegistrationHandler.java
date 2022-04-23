package net.mgcup.dkdmdoor;

import net.mgcup.dkdmdoor.block.BlockDokodemoDoor;
import net.mgcup.dkdmdoor.init.ModBlocks;
import net.mgcup.dkdmdoor.item.ItemDokodemoDoor;
import net.mgcup.dkdmdoor.util.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = DokodemoDoorMod.MODID)
public class RegistrationHandler {

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> register) {
        final Item[] items = {
                RegistryUtil.setItemName(new ItemDokodemoDoor(ModBlocks.WOODEN_TELEPORTER_BLOCK), "wooden_teleporter"),
                RegistryUtil.setItemName(new ItemDokodemoDoor(ModBlocks.IRON_TELEPORTER_BLOCK), "iron_teleporter"),
                RegistryUtil.setItemName(new ItemDokodemoDoor(ModBlocks.STONE_TELEPORTER_BLOCK), "stone_teleporter")
        };

        final Item[] itemBlocks = {
                new ItemBlock(ModBlocks.WOODEN_TELEPORTER_BLOCK).setRegistryName(ModBlocks.WOODEN_TELEPORTER_BLOCK.getRegistryName()),
                new ItemBlock(ModBlocks.IRON_TELEPORTER_BLOCK).setRegistryName(ModBlocks.IRON_TELEPORTER_BLOCK.getRegistryName()),
                new ItemBlock(ModBlocks.STONE_TELEPORTER_BLOCK).setRegistryName(ModBlocks.STONE_TELEPORTER_BLOCK.getRegistryName())
        };

        register.getRegistry().registerAll(items);
        register.getRegistry().registerAll(itemBlocks);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> register) {
        final Block[] blocks = {
                RegistryUtil.setBlockName(new BlockDokodemoDoor(Material.WOOD), "wooden_teleporter_block").setHardness(3.0f),
                RegistryUtil.setBlockName(new BlockDokodemoDoor(Material.IRON), "iron_teleporter_block").setHardness(6.0f).setResistance(50.0f),
                RegistryUtil.setBlockName(new BlockDokodemoDoor(Material.ROCK), "stone_teleporter_block").setHardness(6.0f).setResistance(50.0f)
        };

        register.getRegistry().registerAll(blocks);
    }

}
