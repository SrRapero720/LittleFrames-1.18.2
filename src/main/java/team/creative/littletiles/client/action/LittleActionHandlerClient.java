package team.creative.littletiles.client.action;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import me.srrapero720.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.level.handler.LevelHandler;

@OnlyIn(Dist.CLIENT)
public class LittleActionHandlerClient extends LevelHandler {

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isUsingSecondMode() {
        if (mc.player == null)
            return false;
        if (LittleTiles.CONFIG.building.useALTForEverything)
            return Screen.hasAltDown();
        if (LittleTiles.CONFIG.building.useAltWhenFlying)
            return mc.player.getAbilities().flying ? Screen.hasAltDown() : mc.player.isCrouching();
        return mc.player.isCrouching();
    }

    private List<LittleAction> lastActions = new ArrayList<>();
    private int index = 0;

    public LittleActionHandlerClient(Level level) {
        super(level);
    }

}
