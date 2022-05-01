package net.mgcup.dkdmdoor.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

public abstract class AbstractPacket {
    public abstract void encodeInfo(ChannelHandlerContext context, ByteBuf byteBuf);

    public abstract void decodeInfo(ChannelHandlerContext context, ByteBuf byteBuf);

    public abstract void handleClientSide(EntityPlayer player);

    public abstract void handleServerSide(EntityPlayer player);
}
