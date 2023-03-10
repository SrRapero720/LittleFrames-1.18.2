package team.creative.littletiles.client.render.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import me.srrapero720.creativecore.client.render.face.FRenderBoxFace;
import me.srrapero720.creativecore.client.render.face.FRenderBoxFaceSpecial;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.builder.RenderingBlockContext;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.tile.LittleFRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.face.LittleFaceState;
import team.creative.littletiles.common.math.face.LittleServerFace;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

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
    public final HashMap<RenderType, List<LittleFRenderBox>> boxCache = new HashMap<>();

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

    public void beforeBuilding(RenderingBlockContext context) {
        if (neighbourChanged) {
            neighbourChanged = false;

            for (Map.Entry<RenderType, List<LittleFRenderBox>> entry : boxCache.entrySet()) {
                if (entry.getValue() == null)
                    continue;
                for (LittleFRenderBox cube : entry.getValue())
                    for (int k = 0; k < Facing.VALUES.length; k++) {
                        Facing facing = Facing.VALUES[k];
                        LittleFaceState state = cube.box.getFaceState(facing);

                        if (state.outside())
                            calculateFaces(facing, state, context, (LittleTile) cube.customData, cube);
                    }
            }
        }
    }

    private void calculateFaces(Facing facing, LittleFaceState state, RenderingBlockContext context, @Nullable LittleTile tile, LittleFRenderBox cube) {
        if (state.coveredFully()) {
            cube.setFace(facing, FRenderBoxFace.NOT_RENDER);
            return;
        }

        if (tile != null && tile.isTranslucent() && state.partially()) {
            LittleFace face = cube.box.generateFace(be.getGrid(), facing);
            BETiles toCheck = be;
            if (state.outside()) {
                toCheck = context.getNeighbour(facing);
                face.move(facing);
            }
            if (toCheck.shouldFaceBeRendered(face, tile))
                cube.setFace(facing, new FRenderBoxFaceSpecial(face.generateFans(), (float) face.grid.pixelLength));
            else
                cube.setFace(facing, FRenderBoxFace.NOT_RENDER);

            cube.customData = tile;
        } else
            cube.setFace(facing, FRenderBoxFace.RENDER);
    }

    public List<LittleFRenderBox> getRenderingBoxes(RenderingBlockContext context, RenderType layer) {
        List<LittleFRenderBox> cachedCubes = boxCache.get(layer);
        if (cachedCubes != null)
            return cachedCubes;

        List<LittleFRenderBox> boxes = null;
        LittleServerFace serverFace = new LittleServerFace(be);

        for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
            LittleTile tile = pair.value;

            if (!tile.canRenderInLayer(layer))
                continue;

            for (LittleBox box : tile) {
                box.hasOrCreateFaceState(pair.key, tile, serverFace);

                // Check for sides which does not need to be rendered
                LittleFRenderBox cube = pair.key.getRenderingBox(tile, box, layer);
                if (cube == null)
                    continue;

                for (int k = 0; k < Facing.VALUES.length; k++)
                    calculateFaces(Facing.VALUES[k], cube.box.getFaceState(Facing.VALUES[k]), context, tile, cube);

                if (boxes == null)
                    boxes = new ArrayList<>();
                boxes.add(cube);
            }

        }
        for (LittleStructure structure : be.loadedStructures(LittleStructureAttribute.EXTRA_RENDERING))
            try {
                if (boxes == null)
                    boxes = new ArrayList<>();
                structure.checkConnections();
                structure.getRenderingBoxes(be.getBlockPos(), layer, boxes);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}

        boxCache.put(layer, boxes);
        return boxes;
    }
}
