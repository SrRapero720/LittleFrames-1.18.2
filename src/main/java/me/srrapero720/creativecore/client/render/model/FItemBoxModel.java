package me.srrapero720.creativecore.client.render.model;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import me.srrapero720.creativecore.client.render.box.FRenderBox;

@OnlyIn(Dist.CLIENT)
public abstract class FItemBoxModel extends FItemModel {
    
    public static final Minecraft mc = Minecraft.getInstance();
    
    public static final FItemBoxModel EMPTY = new FItemBoxModel(new ModelResourceLocation("minecraft", "stone", "inventory")) {
        
        @Override
        public List<? extends FRenderBox> getBoxes(ItemStack stack, boolean translucent) {
            return Collections.EMPTY_LIST;
        }
    };
    
    public FItemBoxModel(ModelResourceLocation location) {
        super(location);
    }
    
    public abstract List<? extends FRenderBox> getBoxes(ItemStack stack, boolean translucent);
    
    public boolean hasTranslucentLayer(ItemStack stack) {
        return false;
    }
    
    public List<BakedQuad> getCachedModel(boolean translucent, ItemStack stack, boolean threaded) {
        return null;
    }
    
    public void saveCachedModel(boolean translucent, List<BakedQuad> cachedQuads, ItemStack stack, boolean threaded) {}
    
    public void reload() {}

}