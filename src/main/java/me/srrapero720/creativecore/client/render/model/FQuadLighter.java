package me.srrapero720.creativecore.client.render.model;

import net.minecraft.world.level.block.state.BlockState;

public interface FQuadLighter {
    
    public void setState(BlockState state);
    
    public void setCustomTint(int tint);
    
}
