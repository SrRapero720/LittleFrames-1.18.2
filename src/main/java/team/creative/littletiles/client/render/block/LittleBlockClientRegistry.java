package team.creative.littletiles.client.render.block;

import java.util.HashMap;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.api.common.block.LittleBlock;

public class LittleBlockClientRegistry {

//    private static final ChunkRenderTypeSet SOLID = ChunkRenderTypeSet.of(RenderType.solid());
    private static final HashMap<LittleBlock, ChunkRenderTypeSet> CACHED_LAYERS = new HashMap<>();

    public static boolean canRenderInLayer(LittleBlock block, RenderType layer) {
        ChunkRenderTypeSet layers = CACHED_LAYERS.get(block);
        if (layers == null) {
            layers = CACHED_LAYERS.get(block);
        }
        return layers.contains(layer);
    }

    public static void clearCache() {
        CACHED_LAYERS.clear();
    }

}