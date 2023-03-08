package team.creative.littletiles.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LittleVertexBuffer extends VertexBuffer implements AutoCloseable {
    private int vertextBufferId;
    private int indexBufferId;
    private VertexFormat.IndexType indexType;
    private int arrayObjectId;
    private int indexCount;
    private VertexFormat.Mode mode;
    private boolean sequentialIndices;
    private VertexFormat format;

    public LittleVertexBuffer() {
        RenderSystem.glGenBuffers((p_85928_) -> {
            this.vertextBufferId = p_85928_;
        });
        RenderSystem.glGenVertexArrays((p_166881_) -> {
            this.arrayObjectId = p_166881_;
        });
        RenderSystem.glGenBuffers((p_166872_) -> {
            this.indexBufferId = p_166872_;
        });
    }

    @Override
    public void bind() {
        RenderSystem.glBindBuffer(34962, () -> {
            return this.vertextBufferId;
        });
        if (this.sequentialIndices) {
            RenderSystem.glBindBuffer(34963, () -> {
                RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(this.mode, this.indexCount);
                this.indexType = rendersystem$autostorageindexbuffer.type();
                return rendersystem$autostorageindexbuffer.name();
            });
        } else {
            RenderSystem.glBindBuffer(34963, () -> {
                return this.indexBufferId;
            });
        }

    }

    private void bindVertexArray() {
        RenderSystem.glBindVertexArray(() -> {
            return this.arrayObjectId;
        });
    }

    @Override
    public void close() {
        if (this.indexBufferId >= 0) {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }

        if (this.vertextBufferId > 0) {
            RenderSystem.glDeleteBuffers(this.vertextBufferId);
            this.vertextBufferId = 0;
        }

        if (this.arrayObjectId > 0) {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = 0;
        }

    }

    public boolean isInvalid() {
        return this.arrayObjectId == -1;
    }
}