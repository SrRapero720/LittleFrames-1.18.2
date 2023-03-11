package team.creative.littleframes;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littleframes.common.packet.CreativePictureFramePacket;

public class LittleFrames {

    public static final String MODID = WaterFrames.ID;

    
    public static LittleFramesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleFrames.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleFrames.MODID, "main"));;
    
    public LittleFrames() {

    }
    
    public static void init(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new LittleFramesConfig());

        NETWORK.registerType(CreativePictureFramePacket.class, CreativePictureFramePacket::new);
    }
}
