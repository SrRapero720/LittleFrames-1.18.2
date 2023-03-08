package me.srrapero720.waterframes.mixin.common.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {
    
    @Accessor
    public boolean getHasCollision();
}
