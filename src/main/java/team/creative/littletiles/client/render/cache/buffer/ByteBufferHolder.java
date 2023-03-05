package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
//import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;

public class ByteBufferHolder implements BufferHolder {
    
    public final ByteBuffer buffer;
    public final int length;
    public final int vertexCount;
    
    public ByteBufferHolder(ByteBuffer buffer, int length, int vertexCount) {
        this.buffer = buffer;
        this.length = length;
        this.vertexCount = vertexCount;
    }
    
    public ByteBufferHolder(BufferBuilder buffer) {
//        THIS IS BROKEN, BRUH

        this.length = buffer.currentElement().getByteSize();
        this.buffer = MemoryTracker.create(length);
        this.buffer.put(buffer.popNextBuffer().getSecond());
        this.buffer.rewind();
        this.vertexCount = buffer.currentElement().getCount();
        //buffer.release();
    }
    
    @Override
    public ByteBuffer byteBuffer() {
        return buffer;
    }
    
    @Override
    public int length() {
        return length;
    }
    
    @Override
    public int vertexCount() {
        return vertexCount;
    }
    
}