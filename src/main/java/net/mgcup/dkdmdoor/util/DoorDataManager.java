package net.mgcup.dkdmdoor.util;

import jdk.nashorn.internal.ir.Block;
import net.mgcup.dkdmdoor.DokodemoDoorMod;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;

public class DoorDataManager {
    private HashMap<BlockPos, BlockPos> doorNetwork = new HashMap<>();

    public DoorDataManager(NBTTagCompound nbtTagCompound) {
        NBTTagList tagList = (NBTTagList) nbtTagCompound.getTag("EntryList");

        if (tagList == null) return;

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound entry = tagList.getCompoundTagAt(i);
            int fromX = entry.getInteger("fromX");
            int fromY = entry.getInteger("fromY");
            int fromZ = entry.getInteger("fromZ");
            int toX = entry.getInteger("toX");
            int toY = entry.getInteger("toY");
            int toZ = entry.getInteger("toZ");
            this.addEntry(new BlockPos(fromX, fromY, fromZ), new BlockPos(toX, toY, toZ));
        }
    }

    /**
     * Get DoorData as NBTTagCompound
     * @return DoorData
     */
    public NBTTagCompound getDoorData() {
        NBTTagCompound doorData = new NBTTagCompound();
        NBTTagList entryList = new NBTTagList();

        Iterator<BlockPos> itr = doorNetwork.keySet().iterator();
        while (itr.hasNext()) {
            BlockPos from = itr.next();
            BlockPos to = getDestination(from);

            // DokodemoDoorMod.logger.debug(String.format("Entry: %s -> %s", from.toString(), to.toString()));

            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger("fromX", from.getX());
            entry.setInteger("fromY", from.getY());
            entry.setInteger("fromZ", from.getZ());
            entry.setInteger("toX", to.getX());
            entry.setInteger("toY", to.getY());
            entry.setInteger("toZ", to.getZ());
            entryList.appendTag(entry);
        }

        doorData.setTag("EntryList", entryList);
        return doorData;
    }

    public void addEntry(BlockPos from, BlockPos to) {
        this.addEntry(from, to, false);
    }

    public void addEntry(BlockPos from, BlockPos to, boolean isUndirected) {
        doorNetwork.put(from, to);
        if (isUndirected) doorNetwork.put(to, from);
    }

    public void removeEntry(BlockPos from, boolean isUndirected) {
        if (!doorNetwork.containsKey(from)) return;
        BlockPos to = doorNetwork.get(from);
        doorNetwork.remove(from, to);
        if (isUndirected) doorNetwork.remove(to, from);
    }

    public boolean hasDestination(@Nonnull BlockPos from) {
        return doorNetwork.containsKey(from);
    }

    @Nullable
    public BlockPos getDestination(@Nonnull BlockPos pos) {
        return doorNetwork.get(pos);
    }
}
