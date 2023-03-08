package me.srrapero720.waterframes.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;

@Mixin(ViewArea.class)
public interface ViewAreaAccessor {
    
    @Invoker("getRenderChunkAt")
    public ChunkRenderDispatcher.RenderChunk getChunkAt(BlockPos pos);
    
}
