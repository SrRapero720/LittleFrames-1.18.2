package team.creative.littletiles.common.level.little;

import java.util.UUID;

import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.level.IOrientatedLevel;

public interface LittleLevel extends IOrientatedLevel {
    
    default Level asLevel() {
        return (Level) this;
    }
    
    BlockUpdateLevelSystem getBlockUpdateLevelSystem();
    
    @Override
    Entity getHolder();
    
    @Override
    void setHolder(Entity entity);
    
    UUID key();
    
    void registerLevelBoundListener(LevelBoundsListener listener);
    
    void unload(LevelChunk chunk);

    void unload();
    
    Iterable<Entity> entities();
    
    int getFreeMapId();
    
    Iterable<? extends ChunkAccess> chunks();
    
    void tick();
    
    PacketListener getPacketListener(Player player);
    
    void stopTracking(ServerPlayer player);
    
}
