package team.creative.littleframes.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBlockModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleFramesRegistry;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.block.BEPictureFrameF;
import team.creative.littleframes.common.block.BlockCreativePictureFrame;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class LittleFramesClient {

    public static void load(IEventBus bus) {
        bus.addListener(LittleFramesClient::setup);
    }

    public static void setup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(TextureCache.class);

        CreativeCoreClient.registerClientConfig(LittleFrames.MODID);
        CreativeCoreClient.registerItemModel(new ResourceLocation(LittleFrames.MODID, "creative_pic_frame"),
                new CreativeItemBoxModel(new ModelResourceLocation("minecraft", "stone", "inventory")) {

                    @Override
                    public List<? extends RenderBox> getBoxes(ItemStack itemStack, RenderType renderType) {
                        return Collections.singletonList(new RenderBox(0, 0, 0, BlockCreativePictureFrame.frameThickness, 1, 1, Blocks.OAK_PLANKS));
                    }
                });


        CreativeCoreClient.registerBlockModel(new ResourceLocation(LittleFrames.MODID, "creative_pic_frame"), new CreativeBlockModel() {
            public final ModelProperty<Boolean> visibility = new ModelProperty<>();
            public final ModelDataMap visible = new ModelDataMap.Builder().withInitial(visibility, true).build();
            public final ModelDataMap invisible = new ModelDataMap.Builder().withInitial(visibility, false).build();
            @Override
            public List<? extends RenderBox> getBoxes(BlockState blockState, IModelData iModelData, Random random) {
                if (Boolean.FALSE.equals(iModelData.getData(visibility)))
                    return Collections.EMPTY_LIST;
                RenderBox box = new RenderBox(BlockCreativePictureFrame.box(blockState.getValue(BlockCreativePictureFrame.FACING)), Blocks.OAK_PLANKS);
                return Collections.singletonList(box);
            }

            @Override
            public @NotNull IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull IModelData iModelData) {
                BlockEntity be = level.getBlockEntity(blockPos);
                if (be instanceof BEPictureFrameF frame)
                    return frame.visibleFrame ? visible : invisible;
                return visible;
            }
        });

        BlockEntityRenderers.register(LittleFramesRegistry.BE_CREATIVE_FRAME.get(), x -> new CreativePictureFrameRenderer());
    }

}
