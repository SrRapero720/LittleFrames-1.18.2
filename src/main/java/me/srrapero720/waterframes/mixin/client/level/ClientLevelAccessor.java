package me.srrapero720.waterframes.mixin.client.level;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
    
    @Accessor
    public TransientEntitySectionManager getEntityStorage();
    
    @Accessor
    public ClientPacketListener getConnection();
    
//    @Invoker
//    public BlockStatePredictionHandler callGetBlockStatePredictionHandler();
    
}
