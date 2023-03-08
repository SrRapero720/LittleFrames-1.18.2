package me.srrapero720.waterframes.mixin.client.render;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.VisibilitySet;

@Mixin(CompiledChunk.class)
public interface CompiledChunkAccessor {
    
    @Accessor
    BufferBuilder.SortState getTransparencyState();
    
    @Accessor
    void setTransparencyState(BufferBuilder.SortState state);
    
    @Accessor
    Set<RenderType> getHasBlocks();
    
    @Accessor
    VisibilitySet getVisibilitySet();
    
    @Accessor
    void setVisibilitySet(VisibilitySet visibilitySet);
    
}
