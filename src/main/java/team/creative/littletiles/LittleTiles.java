package team.creative.littletiles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.*;
import team.creative.littletiles.common.action.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.entity.EntitySizeHandler;
import team.creative.littletiles.common.ingredient.rules.IngredientRules;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.packet.LittlePacketTypes;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.item.MirrorPacket;
import team.creative.littletiles.common.packet.item.RotatePacket;
import team.creative.littletiles.common.packet.level.LittleLevelInitPacket;
import team.creative.littletiles.common.packet.level.LittleLevelPacket;
import team.creative.littletiles.common.packet.level.LittleLevelPackets;
import team.creative.littletiles.common.packet.level.LittleLevelPhysicPacket;
import team.creative.littletiles.common.packet.structure.BedUpdate;
import team.creative.littletiles.common.packet.update.*;
import team.creative.littletiles.common.recipe.StructureIngredient.StructureIngredientSerializer;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.LittleSignalHandler;
import team.creative.littletiles.common.structure.type.bed.LittleBedEventHandler;
import team.creative.littletiles.server.LittleTilesServer;

@Mod(LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    
    public static LittleTilesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleTiles.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleTiles.MODID, "main"));
    
    public static TagKey<Block> STORAGE_BLOCKS;
    
    public LittleTiles() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LittleTilesClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
        
        LittleTilesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
//    public void buildContents(CreativeModeTabEvent.Register event) {
//        event.registerCreativeModeTab(new ResourceLocation(MODID, "items"), x -> x.title(new TranslatableComponent("itemGroup.littletiles"))
//                .icon(() -> new ItemStack(LittleTilesRegistry.HAMMER.get())).displayItems((features, output, permission) -> {
//                    for (ExampleStructures example : ExampleStructures.values())
//                        if (example.stack != null)
//                            output.accept(example.stack);
//
//                    for (LittlePremadeType entry : LittlePremadeRegistry.types())
//                        if (entry.showInCreativeTab && !entry.hasCustomTab())
//                            output.accept(entry.createItemStack());
//
//                    output.accept(LittleTilesRegistry.HAMMER.get());
//                    output.accept(LittleTilesRegistry.CHISEL.get());
//                    output.accept(LittleTilesRegistry.BLUEPRINT.get());
//
//                    output.accept(LittleTilesRegistry.BAG.get());
//                    output.accept(LittleTilesRegistry.GLOVE.get());
//
//                    output.accept(LittleTilesRegistry.PAINT_BRUSH.get());
//                    output.accept(LittleTilesRegistry.SAW.get());
//                    output.accept(LittleTilesRegistry.SCREWDRIVER.get());
//                    output.accept(LittleTilesRegistry.WRENCH.get());
//
//                    output.accept(LittleTilesRegistry.SIGNAL_CONVERTER.get());
//                    output.accept(LittleTilesRegistry.STORAGE_BLOCK.get());
//
//                    output.accept(LittleTilesRegistry.CLEAN.get());
//                    output.accept(LittleTilesRegistry.FLOOR.get());
//                    output.accept(LittleTilesRegistry.GRAINY_BIG.get());
//                    output.accept(LittleTilesRegistry.GRAINY.get());
//                    output.accept(LittleTilesRegistry.GRAINY_LOW.get());
//                    output.accept(LittleTilesRegistry.BRICK.get());
//                    output.accept(LittleTilesRegistry.BRICK_BIG.get());
//                    output.accept(LittleTilesRegistry.BORDERED.get());
//                    output.accept(LittleTilesRegistry.CHISELED.get());
//                    output.accept(LittleTilesRegistry.BROKEN_BRICK_BIG.get());
//                    output.accept(LittleTilesRegistry.CLAY.get());
//                    output.accept(LittleTilesRegistry.STRIPS.get());
//                    output.accept(LittleTilesRegistry.GRAVEL.get());
//                    output.accept(LittleTilesRegistry.SAND.get());
//                    output.accept(LittleTilesRegistry.STONE.get());
//                    output.accept(LittleTilesRegistry.CORK.get());
//
//                    output.accept(LittleTilesRegistry.WATER.get());
//                    output.accept(LittleTilesRegistry.WHITE_WATER.get());
//
//                    output.accept(LittleTilesRegistry.LAVA.get());
//                    output.accept(LittleTilesRegistry.WHITE_LAVA.get());
//
//                }));
//    }
    
    private void init(final FMLCommonSetupEvent event) {
        
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
        LittlePacketTypes.init();
        
        NETWORK.registerType(ActionMessagePacket.class, ActionMessagePacket::new);
        NETWORK.registerType(VanillaBlockPacket.class, VanillaBlockPacket::new);
        NETWORK.registerType(BlockPacket.class, BlockPacket::new);
        
        NETWORK.registerType(BedUpdate.class, BedUpdate::new);
        
        NETWORK.registerType(RotatePacket.class, RotatePacket::new);
        NETWORK.registerType(MirrorPacket.class, MirrorPacket::new);
        
        NETWORK.registerType(StructureUpdate.class, StructureUpdate::new);
        NETWORK.registerType(NeighborUpdate.class, NeighborUpdate::new);
        NETWORK.registerType(BlockUpdate.class, BlockUpdate::new);
        NETWORK.registerType(BlocksUpdate.class, BlocksUpdate::new);
        NETWORK.registerType(OutputUpdate.class, OutputUpdate::new);
        
        NETWORK.registerType(LittleLevelPacket.class, LittleLevelPacket::new);
        NETWORK.registerType(LittleLevelPackets.class, LittleLevelPackets::new);
        NETWORK.registerType(LittleLevelInitPacket.class, LittleLevelInitPacket::new);
        NETWORK.registerType(LittleLevelPhysicPacket.class, LittleLevelPhysicPacket::new);
        
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new LittleTilesConfig());
        
        LittleActionRegistry.register(LittleActions.class, LittleActions::new);
        LittleActionRegistry.register(LittleActionPlace.class, LittleActionPlace::new);
        LittleActionRegistry.register(LittleActionActivated.class, LittleActionActivated::new);
        LittleActionRegistry.register(LittleActionColorBoxes.class, LittleActionColorBoxes::new);
        LittleActionRegistry.register(LittleActionColorBoxesFiltered.class, LittleActionColorBoxesFiltered::new);
        LittleActionRegistry.register(LittleActionDestroyBoxes.class, LittleActionDestroyBoxes::new);
        LittleActionRegistry.register(LittleActionDestroyBoxesFiltered.class, LittleActionDestroyBoxesFiltered::new);
        LittleActionRegistry.register(LittleActionDestroy.class, LittleActionDestroy::new);
        
        MinecraftForge.EVENT_BUS.register(new LittleBedEventHandler());
        MinecraftForge.EVENT_BUS.register(LittleAnimationHandlers.class);
        // MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        MinecraftForge.EVENT_BUS.register(new LittleSignalHandler());
        
        LittleTilesServer.init(event);
        
        //MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        MinecraftForge.EVENT_BUS.register(EntitySizeHandler.class);
        
        STORAGE_BLOCKS = BlockTags.create(new ResourceLocation(MODID, "storage_blocks"));
        
        CraftingHelper.register(new ResourceLocation(MODID, "structure"), StructureIngredientSerializer.INSTANCE);
        
        LittleTilesGuiRegistry.init();
    }
    
    /*public static List<LittleDoor> findDoors(LittleAnimationHandler handler, AABB box) {
        List<LittleDoor> doors = new ArrayList<>();
        for (LittleLevelEntity entity : handler.entities)
            try {
                if (entity.getStructure() instanceof LittleDoor && entity.getBoundingBox().intersects(box) && !doors.contains(entity.getStructure()))
                    doors.add(((LittleDoor) entity.getStructure()).getParentDoor());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return doors;
    }*/
    
    protected boolean checkStructureName(LittleStructure structure, String[] args) {
        for (int i = 0; i < args.length; i++)
            if (structure.name != null && structure.name.equalsIgnoreCase(args[i]))
                return true;
        return false;
    }
    
}
