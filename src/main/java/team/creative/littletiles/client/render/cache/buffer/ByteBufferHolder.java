package team.creative.littletiles.client.render.cache.buffer;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;

import java.nio.ByteBuffer;

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
        this.length = buffer.currentElement().getByteSize();
        this.buffer = MemoryTracker.create(length);
        this.buffer.put(buffer.popNextBuffer().getSecond());
        this.buffer.rewind();
        this.vertexCount = buffer.currentElement().getCount();
        buffer.clear();
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