package team.creative.littletiles.client.render.block;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;

@OnlyIn(Dist.CLIENT)
public class BERenderManager {

    private BETiles be;

    private int requestedIndex = -1;
    private int renderState = -1;

    private boolean queued = false;
    private boolean eraseBoxCache = false;

    public boolean hasLightChanged = false;
    private boolean neighbourChanged = false;

    private double cachedRenderDistance = 0;
    private AABB cachedRenderBoundingBox = null;
    private boolean requireRenderingBoundingBoxUpdate = false;

    private final BlockBufferCache bufferCache = new BlockBufferCache();
    public final HashMap<RenderType, List<LittleRenderBox>> boxCache = new HashMap<>();

    public BERenderManager(BETiles be) {
        this.be = be;
    }

    public void setBe(BETiles be) {
        this.be = be;
    }

    public boolean isInQueue() {
        return queued;
    }

    public void chunkUpdate(RenderChunkExtender chunk) {
        synchronized (this) {
            boolean doesNeedUpdate = neighbourChanged || hasLightChanged || requestedIndex == -1 || bufferCache.hasInvalidBuffers();
            if (renderState != LittleChunkDispatcher.currentRenderState) {
                eraseBoxCache = true;
                doesNeedUpdate = true;
            }

            hasLightChanged = false;
            neighbourChanged = false;

            if (doesNeedUpdate) queue(eraseBoxCache, chunk);
        }
    }

    public void tilesChanged() {
        requireRenderingBoundingBoxUpdate = true;
        cachedRenderDistance = 0;
        queue(true, null);
    }

    public double getMaxRenderDistance() {
        if (cachedRenderDistance == 0) {
            double renderDistance = 64;
            for (LittleStructure structure : be.rendering())
                renderDistance = Math.max(renderDistance, structure.getMaxRenderDistance());
            cachedRenderDistance = renderDistance;
        }
        return cachedRenderDistance;
    }

    public AABB getRenderBoundingBox() {
        if (requireRenderingBoundingBoxUpdate || cachedRenderBoundingBox == null) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;
            boolean found = false;
            for (LittleStructure structure : be.rendering()) {
                AABB box = structure.getRenderBoundingBox();
                if (box == null)
                    continue;
                box = box.move(be.getBlockPos());
                minX = Math.min(box.minX, minX);
                minY = Math.min(box.minY, minY);
                minZ = Math.min(box.minZ, minZ);
                maxX = Math.max(box.maxX, maxX);
                maxY = Math.max(box.maxY, maxY);
                maxZ = Math.max(box.maxZ, maxZ);
                found = true;
            }
            if (found)
                cachedRenderBoundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            else
                cachedRenderBoundingBox = new AABB(be.getBlockPos());

            requireRenderingBoundingBoxUpdate = false;
        }
        return cachedRenderBoundingBox;
    }

    public void onNeighbourChanged() {
        neighbourChanged = true;
        queue(false, null);
    }

    public void queue(boolean eraseBoxCache, @Nullable RenderChunkExtender chunk) {
        synchronized (this) {
            requestedIndex++;

            this.eraseBoxCache |= eraseBoxCache;

            // Potential Fix, is likely to cause issues
//            if (!queued && RenderingThread.queue(be, chunk)) queued = true;
        }
    }

    public int startBuildingCache() {
        synchronized (this) {
            if (eraseBoxCache) {
                boxCache.clear();
                eraseBoxCache = false;
            }
            return requestedIndex;
        }

    }

    public boolean finishBuildingCache(int index, int renderState, boolean force) {
        synchronized (this) {
            this.renderState = renderState;
            boolean done = force || (index == requestedIndex && this.renderState == renderState);
            if (done)
                queued = false;
            this.hasLightChanged = false;
            bufferCache.afterRendered();
            return done;
        }
    }

    public void resetRenderingState() {
        queued = false;
        requestedIndex = -1;
    }

    public void chunkUnload() {
        synchronized (this) {
            bufferCache.setEmpty();
            boxCache.clear();
            cachedRenderBoundingBox = null;
        }
    }

    public BlockBufferCache getBufferCache() {
        return bufferCache;
    }
}
