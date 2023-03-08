package me.srrapero720.waterframes.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    
    @Accessor
    Vec3 getAwaitingPositionFromClient();
    
    @Accessor
    @Mutable
    void setServer(MinecraftServer server);
    
    @Accessor
    @Mutable
    void setConnection(Connection con);
}
