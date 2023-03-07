package team.creative.littletiles.client.render.item;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.model.CreativeBakedBoxModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.level.LevelAwareHandler;

public class ItemRenderCache implements LevelAwareHandler {

    public static final RenderingThreadItem THREAD = new RenderingThreadItem();

    public static CreativeItemBoxModel get(ItemStack stack) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        if (model instanceof CreativeBakedBoxModel)
            return (CreativeItemBoxModel) ((CreativeBakedBoxModel) model).item;
        return null;
    }

    private HashMap<ItemStack, ItemModelCache> caches = new HashMap<>();

    public ItemRenderCache() {
    }

    @Override
    public void unload() {
        caches.clear();
        THREAD.items.clear();
    }

    @Override
    public void slowTick() {
        for (Iterator<ItemModelCache> iterator = caches.values().iterator(); iterator.hasNext(); )
            if (iterator.next().expired())
                iterator.remove();
    }

    public static class RenderingThreadItem extends Thread {

        public ConcurrentLinkedQueue<Pair<ItemStack, ItemModelCache>> items = new ConcurrentLinkedQueue<>();

        public RenderingThreadItem() {
            start();
        }

        @Override
        public void run() {
            while (true) {
                if (Minecraft.getInstance().level != null && !items.isEmpty()) {
                    Pair<ItemStack, ItemModelCache> pair = items.poll();
                    CreativeItemBoxModel renderer = get(pair.getKey());

                    if (renderer != null) {
                        boolean translucent = renderer.hasTranslucentLayer(pair.key);
                        Random rand = new Random();
                        List<BakedQuad> quads = new ArrayList<>();
                        for (int j = 0; j < Facing.VALUES.length; j++)
                            CreativeBakedBoxModel.compileBoxes(renderer.getBoxes(pair.key, false), Facing.VALUES[j], Sheets.cutoutBlockSheet(), rand, true, quads);
                        pair.value.setQuads(false, quads);
                        if (translucent) {
                            quads = new ArrayList<>();
                            for (int j = 0; j < Facing.VALUES.length; j++)
                                CreativeBakedBoxModel.compileBoxes(renderer.getBoxes(pair.key, true), Facing.VALUES[j], Sheets.translucentCullBlockSheet(), rand, true, quads);
                            pair.value.setQuads(true, quads);
                        }
                        pair.value.complete();
                    }

                } else
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                    }
            }
        }
    }

}
