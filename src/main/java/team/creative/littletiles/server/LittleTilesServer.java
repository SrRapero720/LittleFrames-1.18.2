package team.creative.littletiles.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import team.creative.littletiles.common.action.LittleActionActivated;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandler;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.server.level.handler.LittleActionHandlerServer;
import team.creative.littletiles.server.level.util.NeighborUpdateOrganizer;

public class LittleTilesServer {
    
    public static final LittleAnimationHandlers ANIMATION_HANDLERS = new LittleAnimationHandlers();
    public static NeighborUpdateOrganizer NEIGHBOR;
    
    public static void init(FMLCommonSetupEvent event) {
        NEIGHBOR = new NeighborUpdateOrganizer();
    }

}
