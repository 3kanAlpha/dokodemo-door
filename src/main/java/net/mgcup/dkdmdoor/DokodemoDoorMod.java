package net.mgcup.dkdmdoor;

import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = DokodemoDoorMod.MODID, name = DokodemoDoorMod.NAME, version = DokodemoDoorMod.VERSION)
public class DokodemoDoorMod
{
    public static final String MODID = "dkdmdoor";
    public static final String NAME = "Anywhere Travel Door";
    public static final String VERSION = "0.1";

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // postinit
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        WorldServer server = event.getServer().getWorld(0);
    }
}
