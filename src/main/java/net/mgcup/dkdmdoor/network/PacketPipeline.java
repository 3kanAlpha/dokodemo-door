package net.mgcup.dkdmdoor.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@ChannelHandler.Sharable
public class PacketPipeline extends MessageToMessageCodec<FMLProxyPacket, AbstractPacket> {
    private EnumMap<Side, FMLEmbeddedChannel> channels;
    private LinkedList<Class<? extends AbstractPacket>> packets = new LinkedList<>();
    private boolean isPostInitialized = false;

    /**
     * PipelineにPacketを登録する。識別子は自動でセットされる。1byte制約により256個までしか登録できない。
     * @param clazz 登録したいクラス
     * @return 登録が成功したかどうか
     */
    public boolean registerPacket(Class<? extends AbstractPacket> clazz) {
        if (this.packets.size() > 256) {
            return false;
        }

        if (this.packets.contains(clazz)) {
            return false;
        }

        if (this.isPostInitialized) {
            return false;
        }

        return packets.add(clazz);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket msg, List<Object> out) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        Class<? extends AbstractPacket> clazz = msg.getClass();
        if (!this.packets.contains(clazz)) {
            throw new NullPointerException("No Packet Registered for: " + clazz.getCanonicalName());
        }

        byte discriminator = (byte) this.packets.indexOf(clazz);
        buffer.writeByte(discriminator);
        msg.encodeInfo(ctx, buffer);
        FMLProxyPacket proxyPacket = new FMLProxyPacket(new PacketBuffer(buffer.copy()), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
        out.add(proxyPacket);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
        PacketBuffer payload = new PacketBuffer(msg.payload());
        byte discriminator = payload.readByte();
        Class<? extends AbstractPacket> clazz = this.packets.get(discriminator);
        if (clazz == null) {
            throw new NullPointerException("No packet registered for discriminator: " + discriminator);
        }

        AbstractPacket packet = clazz.newInstance();
        packet.decodeInfo(ctx, payload.slice());

        EntityPlayer player;
        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT:
                player = this.getClientPlayer();
                packet.handleClientSide(player);
                break;
            case SERVER:
                INetHandler handler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                player = ((NetHandlerPlayServer) handler).player;
                packet.handleServerSide(player);
                break;
            default:
                break;
        }

        out.add(packet);
    }

    public void registerChannel(String channelName, Class<? extends AbstractPacket> packetHandlerClass) {
        this.channels = NetworkRegistry.INSTANCE.newChannel(channelName, this);
        this.registerPacket(packetHandlerClass);
    }

    public void postInit() {
        if (this.isPostInitialized) {
            return;
        }

        this.isPostInitialized = true;
        Collections.sort(this.packets, (o1, o2) -> {
            int com = String.CASE_INSENSITIVE_ORDER.compare(o1.getCanonicalName(), o2.getCanonicalName());

            if (com == 0) {
                com = o1.getCanonicalName().compareTo(o2.getCanonicalName());
            }

            return com;
        });
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    /**
     * 特定のプレイヤーにパケットを送る
     * @param message 送りたいメッセージ
     * @param player メッセージを送るプレイヤー
     */
    public void sendTo(AbstractPacket message, EntityPlayerMP player) {
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        this.channels.get(Side.SERVER).writeAndFlush(message);
    }
}
