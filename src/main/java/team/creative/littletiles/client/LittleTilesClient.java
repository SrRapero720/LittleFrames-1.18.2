package team.creative.littletiles.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBlockModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.level.LevelHandlersClient;
import team.creative.littletiles.client.render.block.BETilesRenderer;
import team.creative.littletiles.client.render.item.ItemRenderCache;
import team.creative.littletiles.client.render.item.LittleModelItemPreview;
import team.creative.littletiles.client.render.item.LittleModelItemTilesBig;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class LittleTilesClient {

    public static final Minecraft mc = Minecraft.getInstance();

    public static final LevelHandlersClient LEVEL_HANDLERS = new LevelHandlersClient();
//    public static LittleActionHandlerClient ACTION_HANDLER;
//    public static LittleAnimationHandlerClient ANIMATION_HANDLER;
//    public static LittleInteractionHandlerClient INTERACTION_HANDLER;
    public static ItemRenderCache ITEM_RENDER_CACHE;

    public static KeyMapping flip;
    public static KeyMapping mark;
    public static KeyMapping configure;
    public static KeyMapping up;
    public static KeyMapping down;
    public static KeyMapping right;
    public static KeyMapping left;

    public static KeyMapping undo;
    public static KeyMapping redo;

    public static BETilesRenderer blockEntityRenderer;

    public static void displayActionMessage(List<Component> message) {
        // TODO Readd action message overlay
    }

    public static void load(IEventBus bus) {
        bus.addListener(LittleTilesClient::setup);
        bus.addListener(LittleTilesClient::modelEvent);
        bus.addListener(LittleTilesClient::modelLoader);
    }

    private static void setup(final FMLClientSetupEvent event) {
//        LEVEL_HANDLERS.register(LittleActionHandlerClient::new, x -> ACTION_HANDLER = x);
//        LEVEL_HANDLERS.register(LittleAnimationHandlerClient::new, x -> ANIMATION_HANDLER = x);
//        LEVEL_HANDLERS.register(LittleInteractionHandlerClient::new, x -> INTERACTION_HANDLER = x);
        LEVEL_HANDLERS.register(ITEM_RENDER_CACHE = new ItemRenderCache());

        ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) mc.getResourceManager();
        reloadableResourceManager.registerReloadListener((p_10638_, p_10639_, p_10640_, p_10641_, p_10642_, p_10643_) -> CompletableFuture.runAsync(() -> {
            LittleChunkDispatcher.currentRenderState++;
        }, p_10643_));

        CreativeCoreClient.registerClientConfig(LittleTiles.MODID);

        blockEntityRenderer = new BETilesRenderer();
        BlockEntityRenderers.register(LittleTilesRegistry.BE_TILES_TYPE_RENDERED.get(), x -> blockEntityRenderer);

        ResourceLocation filled = new ResourceLocation(LittleTiles.MODID, "filled");
        ClampedItemPropertyFunction function = (stack, level, entity, x) -> ((ItemColorIngredient) stack.getItem()).getColor(stack) / (float) ColorIngredient.BOTTLE_SIZE;
    }

    public static void modelLoader(ModelRegistryEvent event) {
    }

    public static void modelEvent(ModelRegistryEvent event) {
        CreativeCoreClient.registerBlockModel(new ResourceLocation(LittleTiles.MODID, "empty"), new CreativeBlockModel() {

            @Override
            public List<? extends RenderBox> getBoxes(BlockState state, ModelDataMap data, Random source) {
                return Collections.EMPTY_LIST;
            }

            @Override
            public @NotNull ModelDataMap getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelDataMap modelData) {
                return modelData;
            }
        });

        CreativeCoreClient.registerItemModel(new ResourceLocation(LittleTiles.MODID, "tiles"), new LittleModelItemTilesBig());
        CreativeCoreClient
            .registerItemModel(new ResourceLocation(LittleTiles.MODID, "glove"), new LittleModelItemPreview(new ModelResourceLocation(LittleTiles.MODID, "glove_background", "inventory"), null) {

                @Override
                public boolean shouldRenderFake(ItemStack stack) {
                    return true;
                }

                @Override
                protected ItemStack getFakeStack(ItemStack current) {
                    // Temporary fix, may not work
                    return new ItemStack(Items.STONE);
                }
            });

        CreativeCoreClient.registerItemModel(new ResourceLocation(LittleTiles.MODID, "blockingredient"), new CreativeItemBoxModel(new ModelResourceLocation("miencraft", "stone", "inventory")) {

                @Override
                public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) {
                    List<RenderBox> cubes = new ArrayList<>();
                    BlockIngredientEntry ingredient = ItemBlockIngredient.loadIngredient(stack);
                    if (ingredient == null)
                        return null;

                    double volume = Math.min(1, ingredient.value);
                    LittleGrid context = LittleGrid.defaultGrid();
                    long pixels = (long) (volume * context.count3d);
                    if (pixels < context.count * context.count)
                        cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, ingredient.block.getState()));
                    else {
                        long remainingPixels = pixels;
                        long planes = pixels / context.count2d;
                        remainingPixels -= planes * context.count2d;
                        long rows = remainingPixels / context.count;
                        remainingPixels -= rows * context.count;

                        float height = (float) (planes * context.pixelLength);

                        if (planes > 0)
                            cubes.add(new RenderBox(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F, ingredient.block.getState()));

                        float width = (float) (rows * context.pixelLength);

                        if (rows > 0)
                            cubes.add(new RenderBox(0.0F, height, 0.0F, 1.0F, height + (float) context.pixelLength, width, ingredient.block.getState()));

                        if (remainingPixels > 0)
                            cubes.add(new RenderBox(0.0F, height, width, 1.0F, height + (float) context.pixelLength, width + (float) context.pixelLength, ingredient.block
                                .getState()));
                    }
                    return cubes;
                }
            });
    }

}
