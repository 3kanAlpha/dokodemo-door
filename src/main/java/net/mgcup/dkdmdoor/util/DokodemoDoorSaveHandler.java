package net.mgcup.dkdmdoor.util;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;

import java.io.*;
import java.nio.file.Files;

/**
 * ドアのネットワーク情報をNBTデータとしてファイルから読み書きする
 */
public class DokodemoDoorSaveHandler {
    private final File saveDirectory;
    private static final String DAT_NAME = "doors.dat";
    private DoorDataManager manager;

    public DokodemoDoorSaveHandler(File saveDir) {
        saveDirectory = saveDir;
        saveDirectory.mkdirs();

        this.init();
    }

    private void init() {
        NBTTagCompound nbtTagCompound = loadDoorInfo();
        if (nbtTagCompound == null) nbtTagCompound = new NBTTagCompound();
        manager = new DoorDataManager(nbtTagCompound);
    }

    public DoorDataManager getManager() {
        return this.manager;
    }

    public NBTTagCompound loadDoorInfo() {
        File file = new File(this.saveDirectory, DAT_NAME);

        if (file.exists()) {
            try {
                NBTTagCompound compressed = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
                NBTTagCompound doorData = compressed.getCompoundTag("DoorData");
                return doorData;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void saveDoorInfo(NBTTagCompound doorData) {
        NBTTagCompound toWrite = new NBTTagCompound();
        toWrite.setTag("DoorData", doorData);

        try {
            File file = new File(this.saveDirectory, DAT_NAME);
            File file_new = new File(this.saveDirectory, DAT_NAME + "_new");

            CompressedStreamTools.writeCompressed(toWrite, Files.newOutputStream(file_new.toPath()));

            if (file.exists()) {
                file.delete();
            }

            file_new.renameTo(file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
