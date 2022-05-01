package net.mgcup.dkdmdoor.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

public class PacketTeleportationSound extends AbstractPacket {
    @Override
    public void encodeInfo(ChannelHandlerContext context, ByteBuf byteBuf) {

    }

    @Override
    public void decodeInfo(ChannelHandlerContext context, ByteBuf byteBuf) {

    }

    @Override
    public void handleClientSide(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().playSound(PositionedSoundRecord.getRecord(
                SoundEvents.BLOCK_PORTAL_TRAVEL, 0.8f + player.world.rand.nextFloat() * 0.4f, 1.0f));
    }

    @Override
    public void handleServerSide(EntityPlayer player) {

    }
}
