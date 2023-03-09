package team.creative.littleframes;

import java.util.function.Supplier;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.creative.littleframes.common.block.BEPictureFrameF;
import team.creative.littleframes.common.block.BlockCreativePictureFrame;
import team.creative.littleframes.watercore_supplier.DefaultTab;

@Deprecated(since = "1.18.2")
//Future replacement: WATERegister
public class LittleFramesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LittleFrames.MODID);
    public static final CreativeModeTab TAB = new DefaultTab("LittleFrames", "");
    
    // BLOCKS
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LittleFrames.MODID);
    public static final RegistryObject<Block> CREATIVE_PICTURE_FRAME = register("creative_pic_frame", () -> new BlockCreativePictureFrame());
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        RegistryObject<T> ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(TAB)));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, LittleFrames.MODID);
    
    public static final RegistryObject<BlockEntityType<BEPictureFrameF>> BE_CREATIVE_FRAME = registerBlockEntity("creative_pic_frame", () -> BlockEntityType.Builder
            .of(BEPictureFrameF::new, CREATIVE_PICTURE_FRAME.get()));
    
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType.Builder<T>> sup) {
        return BLOCK_ENTITIES.register(name, () -> sup.get().build(Util.fetchChoiceType(References.BLOCK_ENTITY, name)));
    }
    
}
