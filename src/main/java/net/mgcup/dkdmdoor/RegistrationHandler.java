package net.mgcup.dkdmdoor;

import net.mgcup.dkdmdoor.util.RegistryUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = DokodemoDoorMod.MODID)
public class RegistrationHandler {

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> register) {
        final Item[] items = {
                RegistryUtil.setItemName(new Item(), "wooden_teleporter").setCreativeTab(CreativeTabs.DECORATIONS),
                RegistryUtil.setItemName(new Item(), "iron_teleporter").setCreativeTab(CreativeTabs.DECORATIONS),
                RegistryUtil.setItemName(new Item(), "stone_teleporter").setCreativeTab(CreativeTabs.DECORATIONS)
        };

        register.getRegistry().registerAll(items);
    }

}
