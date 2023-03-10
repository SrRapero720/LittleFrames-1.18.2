package team.creative.littletiles.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBlockModel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.render.block.BETilesRenderer;
import team.creative.littletiles.client.render.block.LittleBlockClientRegistry;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class LittleTilesClient {
    public static final Minecraft mc = Minecraft.getInstance();
    public static LittleAnimationHandlerClient ANIMATION_HANDLER;
    public static BETilesRenderer blockEntityRenderer;

    public static void load(IEventBus bus) {
        bus.addListener(LittleTilesClient::setup);
        bus.addListener(LittleTilesClient::modelEvent);
    }

    private static void setup(final FMLClientSetupEvent event) {
        ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) mc.getResourceManager();
        reloadableResourceManager.registerReloadListener((p_10638_, p_10639_, p_10640_, p_10641_, p_10642_, p_10643_) -> CompletableFuture.runAsync(() -> {
            LittleChunkDispatcher.currentRenderState++;
            LittleBlockClientRegistry.clearCache();
        }, p_10643_));

        CreativeCoreClient.registerClientConfig(LittleTiles.MODID);

        blockEntityRenderer = new BETilesRenderer();
        BlockEntityRenderers.register(LittleTilesRegistry.BE_TILES_TYPE_RENDERED.get(), x -> blockEntityRenderer);
    }

    public static void modelEvent(ModelRegistryEvent event) {
        CreativeCoreClient.registerBlockModel(new ResourceLocation(LittleTiles.MODID, "empty"), new CreativeBlockModel() {
            @Override
            public List<? extends RenderBox> getBoxes(BlockState blockState, IModelData iModelData, Random random) {
                return Collections.EMPTY_LIST;
            }

            @Override
            public @NotNull IModelData getModelData(@NotNull BlockAndTintGetter blockAndTintGetter, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull IModelData iModelData) {
                return iModelData;
            }
        });
    }

}
