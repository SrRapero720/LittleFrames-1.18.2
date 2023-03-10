package team.creative.littletiles;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littletiles.common.action.LittleActionActivated;
import team.creative.littletiles.common.action.LittleActionRegistry;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.level.LittleLevelInitPacket;
import team.creative.littletiles.common.packet.level.LittleLevelPacket;
import team.creative.littletiles.common.packet.level.LittleLevelPackets;
import team.creative.littletiles.common.packet.level.LittleLevelPhysicPacket;
import team.creative.littletiles.common.packet.update.*;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.LittleSignalHandler;
import team.creative.littletiles.server.LittleTilesServer;

public class LittleTiles {
    
    public static final String MODID = WaterFrames.ID;
    
    public static LittleTilesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleTiles.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleTiles.MODID, "main"));

    public LittleTiles() {

    }
    
    public static void init(final FMLCommonSetupEvent event) {

        LittleStructureRegistry.initStructures();
        
        NETWORK.registerType(ActionMessagePacket.class, ActionMessagePacket::new);
        NETWORK.registerType(VanillaBlockPacket.class, VanillaBlockPacket::new);
        NETWORK.registerType(BlockPacket.class, BlockPacket::new);
        
        NETWORK.registerType(StructureUpdate.class, StructureUpdate::new); // < This can cause a crash (uses StructureLocation)
        NETWORK.registerType(NeighborUpdate.class, NeighborUpdate::new);
        NETWORK.registerType(BlockUpdate.class, BlockUpdate::new);
        NETWORK.registerType(BlocksUpdate.class, BlocksUpdate::new);
        NETWORK.registerType(OutputUpdate.class, OutputUpdate::new);
        
        NETWORK.registerType(LittleLevelPacket.class, LittleLevelPacket::new); // < This causes the crash
        NETWORK.registerType(LittleLevelPackets.class, LittleLevelPackets::new);
        NETWORK.registerType(LittleLevelInitPacket.class, LittleLevelInitPacket::new);
        NETWORK.registerType(LittleLevelPhysicPacket.class, LittleLevelPhysicPacket::new);
        
        LittleActionRegistry.register(LittleActions.class, LittleActions::new);
        LittleActionRegistry.register(LittleActionActivated.class, LittleActionActivated::new);

        MinecraftForge.EVENT_BUS.register(LittleAnimationHandlers.class);
        MinecraftForge.EVENT_BUS.register(new LittleSignalHandler());
        
        LittleTilesServer.init(event);

    }
    
}
