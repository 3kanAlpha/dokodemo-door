package net.mgcup.dkdmdoor;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class LocationFixer {
    /**
     * テレポート中のプレイヤーのUUIDとその行き先情報を保持する
     */
    private static HashMap<UUID, BlockPos> playersTeleporting = new HashMap<>();

    /**
     * チャンクが読み込まれたあとに、正しい位置にプレイヤーを戻す
     * @param event
     */
    @SubscribeEvent
    public void onEnteringChunk(EntityEvent.EnteringChunk event) {
        Entity entity = event.getEntity();

        if (entity instanceof EntityPlayer && playersTeleporting.containsKey(entity.getUniqueID())) {
            EntityPlayer player = (EntityPlayer) entity;
            BlockPos cor = playersTeleporting.get(player.getUniqueID());
            playersTeleporting.remove(player.getUniqueID()); // 消してから処理しないとループする
            moveToCorrectPosition(player, cor);
        }
    }

    private static void moveToCorrectPosition(EntityPlayer player, BlockPos dest) {
        double prevX = player.prevPosX;
        double prevY = player.prevPosY;
        double prevZ = player.prevPosZ;

        if (!isSafeLocation(player.world, dest)) {
            dest = findSafeLocation(player.world, dest, 3);
        }

        player.setPositionAndUpdate(dest.getX() + 0.5d, dest.getY(), dest.getZ() + 0.5d);
        player.fallDistance = 0.0f;
        player.motionY = 0.0d;

        // DokodemoDoorMod.logger.debug(String.format("Player Position Fixed: (%.1f, %.1f, %.1f) -> (%.1f, %.1f, %.1f)", prevX, prevY, prevZ, player.posX, player.posY, player.posZ));
    }

    private static boolean isSafeLocation(World world, BlockPos pos) {
        boolean flag = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
        boolean flag1 = world.getBlockState(pos.up()).getBlock() == Blocks.AIR;

        return (flag && flag1);
    }

    private static BlockPos findSafeLocation(World world, BlockPos pos, int range) {
        BlockPos vertex1 = pos.add(-range, -10, -range);
        BlockPos vertex2 = pos.add(range, 10, range);
        BlockPos safe = null;
        double minDistance = 0.0d;

        for (BlockPos b : BlockPos.getAllInBox(vertex1, vertex2)) {
            IBlockState state = world.getBlockState(b.down());
            if (state.isNormalCube() && isSafeLocation(world, b)) {
                double blockDistance = b.distanceSqToCenter(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);

                if (safe == null || blockDistance < minDistance) {
                    safe = b;
                    minDistance = blockDistance;
                }
            }
        }

        return safe != null ? safe : new BlockPos(pos.getX(), 255, pos.getZ());
    }

    public static void add(EntityPlayer player, BlockPos dest) {
        playersTeleporting.put(player.getUniqueID(), dest.toImmutable());
        // DokodemoDoorMod.logger.debug(String.format("%s's UUID: %s", player.getDisplayNameString(), player.getUniqueID()));
    }
}
