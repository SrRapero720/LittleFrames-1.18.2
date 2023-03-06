package team.creative.littletiles.client.level.little;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.level.LittleLevelPacket;

import javax.crypto.Cipher;
import java.net.SocketAddress;

public class LittleClientConnection extends Connection {

    public final LittleLevel level;

    public LittleClientConnection(LittleLevel level) {
        super(PacketFlow.CLIENTBOUND);
        this.level = level;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
    }

    @Override
    public void setProtocol(ConnectionProtocol protocol) {
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable thrown) {
    }

    @Override
    public void setListener(PacketListener listener) {
    }

    @Override
    public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener) {
        send(packet);
    }

    @Override
    public void send(Packet<?> packet) {
        LittleTiles.NETWORK.sendToServer(new LittleLevelPacket(level, packet));
    }

//    CANT BE USABLE IN 1.18.2 (overrides nothing)
//    @Override
//    public void send(Packet<?> packet, @Nullable PacketListener listener) {
//        send(packet);
//        if (listener != null)
//            listener.getConnection().send(packet);
//    }

    @Override
    public void tick() {
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public void disconnect(Component component) {
    }

    @Override
    public boolean isMemoryConnection() {
        return false;
    }

    @Override
    public void setEncryptionKey(Cipher cipher1, Cipher cipher2) {
    }

    @Override
    public boolean isEncrypted() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public Component getDisconnectedReason() {
        return null;
    }

    @Override
    public void setReadOnly() {
    }

    @Override
    public void setupCompression(int p_129485_, boolean p_182682_) {
    }

    @Override
    public void handleDisconnection() {
    }

    @Override
    public float getAverageReceivedPackets() {
        return 0;
    }

    @Override
    public float getAverageSentPackets() {
        return 0;
    }

    @Override
    public Channel channel() {
        return null;
    }
}
