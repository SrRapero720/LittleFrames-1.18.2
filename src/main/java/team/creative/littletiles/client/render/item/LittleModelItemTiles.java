package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

public class LittleModelItemTiles extends CreativeItemBoxModel {

    public LittleModelItemTiles() {
        super(new ModelResourceLocation("minecraft", "stone", "inventory"));
    }

    @Override
    public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean hasTranslucentLayer(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getBoolean(LittleGroup.TRANSLUCENT_KEY) : false;
    }

    @Override
    public void saveCachedModel(boolean translucent, List<BakedQuad> cachedQuads, ItemStack stack, boolean threaded) {
    }

    @Override
    public List<BakedQuad> getCachedModel(boolean translucent, ItemStack stack, boolean threaded) {
        return LittleTilesClient.ITEM_RENDER_CACHE.requestCache(stack, translucent);
    }

}
