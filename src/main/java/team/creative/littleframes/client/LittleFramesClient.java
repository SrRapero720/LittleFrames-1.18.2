package team.creative.littleframes.client;

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
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import me.srrapero720.creativecore.client.CreativeCoreClient;
import me.srrapero720.creativecore.client.render.box.FRenderBox;
import me.srrapero720.creativecore.client.render.model.FBlockModel;
import me.srrapero720.creativecore.client.render.model.FItemBoxModel;
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
        
        CreativeCoreClient
                .registerItemModel(new ResourceLocation(LittleFrames.MODID, "creative_pic_frame"), new FItemBoxModel(new ModelResourceLocation("minecraft", "stone", "inventory")) {
                    
                    @Override
                    public List<? extends FRenderBox> getBoxes(ItemStack stack, boolean translucent) {
                        return Collections.singletonList(new FRenderBox(0, 0, 0, BlockCreativePictureFrame.frameThickness, 1, 1, Blocks.OAK_PLANKS));
                    }
                });
        
        CreativeCoreClient.registerBlockModel(new ResourceLocation(LittleFrames.MODID, "creative_pic_frame"), new FBlockModel() {
            
            public final ModelProperty<Boolean> visibility = new ModelProperty<>();
            public final ModelDataMap visible = new ModelDataMap.Builder().withInitial(visibility, true).build();
            public final ModelDataMap invisible = new ModelDataMap.Builder().withInitial(visibility, false).build();
            
            @Override
            public @NotNull ModelDataMap getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelDataMap modelData) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BEPictureFrameF frame)
                    return frame.visibleFrame ? visible : invisible;
                return visible;
            }
            
            @Override
            public List<? extends FRenderBox> getBoxes(BlockState state, ModelDataMap data, Random source) {
                if (Boolean.FALSE.equals(data.getData(visibility)))
                    return Collections.EMPTY_LIST;
                FRenderBox box = new FRenderBox(BlockCreativePictureFrame.box(state.getValue(BlockCreativePictureFrame.FACING)), Blocks.OAK_PLANKS);
                return Collections.singletonList(box);
            }
        });
        
        BlockEntityRenderers.register(LittleFramesRegistry.BE_CREATIVE_FRAME.get(), x -> new CreativePictureFrameRenderer());
    }
    
}
