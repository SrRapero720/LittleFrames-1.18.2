package me.srrapero720.creativecore.common.util.mc;

import team.creative.creativecore.client.CreativeCoreClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TickUtils {

    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    private static float getFrameTimeClient() {
        return CreativeCoreClient.getDeltaFrameTime();
    }

    public static float getFrameTime(LevelAccessor level) {
        if (level.isClientSide())
            return getFrameTimeClient();
        return 1.0F;
    }

}