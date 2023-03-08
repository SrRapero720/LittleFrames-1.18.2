package me.srrapero720.waterframes.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.VertexBuffer;

import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferExtender {
    
    @Unique
    public ChunkLayerUploadManager manager;
    
    @Override
    public ChunkLayerUploadManager getManager() {
        return manager;
    }
    
    @Override
    public void setManager(ChunkLayerUploadManager manager) {
        this.manager = manager;
    }
    
    @Inject(at = @At("TAIL"), method = "upload(Lcom/mojang/blaze3d/vertex/BufferBuilder;)V")
    public void upload(CallbackInfo info) {
        if (manager != null) manager.uploaded();
    }
    
    @Override
    @Accessor
    public abstract int getIndexCount();
    
    @Override
    @Accessor
    public abstract int getVertextBufferId();
    
}
