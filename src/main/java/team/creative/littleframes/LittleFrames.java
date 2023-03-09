package team.creative.littleframes;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littleframes.common.packet.CreativePictureFramePacket;
import team.creative.littleframes.common.packet.LittlePictureFramePacket;
import team.creative.littleframes.common.structure.LittlePictureFrame;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

public class LittleFrames {

    public static final String MODID = WaterFrames.ID;

    
    public static LittleFramesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleFrames.MODID);
    public static final CreativeNetwork NETWORK = LittleTiles.NETWORK;
    
    public LittleFrames() {

    }
    
    public static void init(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new LittleFramesConfig());
        
        NETWORK.registerType(CreativePictureFramePacket.class, CreativePictureFramePacket::new);
        NETWORK.registerType(LittlePictureFramePacket.class, LittlePictureFramePacket::new);

        LittleStructureBuilder.register(new LittleStructureBuilderType(LittleStructureRegistry
                .register("little_picture_frame", LittlePictureFrame.class, LittlePictureFrame::new, new LittleAttributeBuilder().tickRendering().ticking()), "frame"));
    }
}
