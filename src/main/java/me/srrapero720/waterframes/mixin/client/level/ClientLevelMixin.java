package me.srrapero720.waterframes.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import me.srrapero720.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.client.level.little.LittleClientChunkCache;
import team.creative.littletiles.client.level.little.LittleClientLevel;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientChunkCache"), method = "<init>", require = 1)
    public ClientChunkCache newClientChunkCache(ClientLevel level, int distance) {
        if (level instanceof LittleClientLevel little) {
            LittleClientChunkCache cache = CreativeHackery.allocateInstance(LittleClientChunkCache.class);
            cache.init(little);
            return cache;
        }
        return new ClientChunkCache(level, distance);
    }
    
}
