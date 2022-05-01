package net.mgcup.dkdmdoor;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class LocationFixer {
    private static HashMap<UUID, BlockPos> playersTeleporting = new HashMap<>();

    @SubscribeEvent
    public void onEnteringChunk(EntityEvent.EnteringChunk event) {
        Entity entity = event.getEntity();

        if (entity instanceof EntityPlayer && playersTeleporting.containsKey(entity.getPersistentID())) {
            EntityPlayer player = (EntityPlayer) entity;
            moveToCorrectPosition(player, playersTeleporting.get(entity.getPersistentID()));
            playersTeleporting.remove(entity.getPersistentID());
        }
    }

    private static void moveToCorrectPosition(EntityPlayer player, BlockPos dest) {
        double prevX = player.prevPosX;
        double prevY = player.prevPosY;
        double prevZ = player.prevPosZ;

        player.setPositionAndUpdate(dest.getX() + 0.5d, dest.getY() + 0.5d, dest.getZ() + 0.5d);
        player.fallDistance = 0.0f;
        player.motionY = 0.0d;
    }

    public static void add(EntityPlayer player, BlockPos dest) {
        playersTeleporting.put(player.getPersistentID(), dest.toImmutable());
    }
}
