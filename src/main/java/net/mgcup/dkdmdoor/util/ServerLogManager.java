package net.mgcup.dkdmdoor.util;

import net.mgcup.dkdmdoor.DokodemoDoorMod;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class ServerLogManager {

    public static void printServerLog(MinecraftServer server, String msg) {
        if (server != null && !server.isSinglePlayer()) {
            server.logInfo(msg);
        }
        DokodemoDoorMod.logger.info(msg);
    }
}
