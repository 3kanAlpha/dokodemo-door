package net.mgcup.dkdmdoor;

import net.mgcup.dkdmdoor.network.PacketPipeline;
import net.mgcup.dkdmdoor.network.PacketTeleportationSound;
import net.mgcup.dkdmdoor.util.DokodemoDoorSaveHandler;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.MinecraftForge;
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
    public static final String VERSION = "1.0";

    public static Logger logger;

    public static DokodemoDoorSaveHandler saveHandler;
    public static PacketPipeline packetPipeline = new PacketPipeline();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new LocationFixer());
        packetPipeline.registerChannel(DokodemoDoorMod.MODID, PacketTeleportationSound.class);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        packetPipeline.postInit();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        WorldServer server = event.getServer().getWorld(0);
        saveHandler = new DokodemoDoorSaveHandler(server.getSaveHandler().getWorldDirectory());
    }
}
