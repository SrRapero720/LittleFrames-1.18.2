package team.creative.creativecore.mixin;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    
    @Accessor
    public int getVertices();
    
    @Accessor
    public int getNextElementByte();
    
    @Accessor
    public ByteBuffer getBuffer();
    
}