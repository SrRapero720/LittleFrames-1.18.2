package me.srrapero720.waterframes;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.LittleFramesRegistry;
import team.creative.littleframes.client.LittleFramesClient;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";

    public WaterFrames() {
        MinecraftForge.EVENT_BUS.register(this);

//        loadLittleTiles();
        loadLittleFrames();
    }

    private void loadLittleFrames() {
//        LittlePacketTypes.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LittleFrames::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LittleFramesClient.load(FMLJavaModLoadingContext.get().getModEventBus()));

        LittleFramesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleFramesRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleFramesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    private void loadLittleTiles() {
//        FMLJavaModLoadingContext.get().getModEventBus().addListener(LittleTiles::init);
//        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LittleTilesClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
//
//        LittleTilesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
//        LittleTilesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
//        LittleTilesRegistry.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            //SrConsole.debug(MODULE, "Registrando bloque: " + blockRegistryEvent.getName().toString());
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            //SrConsole.debug(MODULE, "Registrando item: " + itemRegistryEvent.getName().toString());
        }
    }
}
