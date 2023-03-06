package team.creative.creativecore.mixin;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    
    @Accessor
    int getVertices();
    
    @Accessor
    int getNextElementByte();
    
    @Accessor
    ByteBuffer getBuffer();
    
}