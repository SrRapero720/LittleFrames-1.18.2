package team.creative.littletiles.common.level.handler;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.server.LittleTilesServer;
import team.creative.littletiles.server.level.handler.LittleAnimationHandlerServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class LittleAnimationHandlers {
    private final List<Function<Level, LittleAnimationHandlerServer>> factories = new ArrayList<>();
    private final HashMapList<Level, LittleAnimationHandlerServer> handlers = new HashMapList<>();
    private final boolean client;
    public LittleAnimationHandlers() {
        this.client = true;
        MinecraftForge.EVENT_BUS.register(this);
        register(LittleAnimationHandlerServer::new);
    }

    @OnlyIn(Dist.CLIENT)
    private static LittleAnimationHandler getClient() {
        return LittleTilesClient.ANIMATION_HANDLER;
    }
    
    public static LittleAnimationHandler get(Level level) {
        if (level instanceof ISubLevel sub) level = sub.getRealLevel();
        if (level.isClientSide) return getClient();
        return LittleTilesServer.ANIMATION_HANDLERS.getForLevel(level);
    }
    
    public static LittleEntity find(boolean client, UUID uuid) {
        if (client)
            return findClient(uuid);
        return findServer(uuid);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static LittleEntity findClient(UUID uuid) {
        return LittleTilesClient.ANIMATION_HANDLER.find(uuid);
    }
    
    public static LittleEntity findServer(UUID uuid) {
        for (LittleAnimationHandler handler : LittleTilesServer.ANIMATION_HANDLERS.all()) {
            var entity = handler.find(uuid);
            if (entity != null) return entity;
        }
        return null;
    }

    protected LittleAnimationHandlerServer getForLevel(Level level) {
        List<LittleAnimationHandlerServer> handlers = getHandlers(level);
        if (handlers.size() == 1) return handlers.get(0);
        return new LittleAnimationHandlerServer(level);
    }
    
    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        //if (!event.world.isClientSide) getHandlers(event.world).forEach(x -> x.tickServer(event));
    }

    protected Iterable<LittleAnimationHandlerServer> all() {
        return handlers;
    }

    protected List<LittleAnimationHandlerServer> getHandlers(Level level) {
        return handlers.get(level);
    }

    public void register(Function<Level, LittleAnimationHandlerServer> function) {
        factories.add(function);
    }

    protected void load(Level level) {
        var levelHandlers = handlers.removeKey(level);
        if (levelHandlers != null) throw new RuntimeException("This should not happen");

        var newHandlers = new ArrayList<LittleAnimationHandlerServer>(factories.size());
        for (var func : factories) newHandlers.add(func.apply(level));
        handlers.add(level, newHandlers);
        for (var handler : newHandlers) handler.load();
    }

    protected void unload(Level level) {
        List<LittleAnimationHandlerServer> levelHandlers = handlers.removeKey(level);
        if (levelHandlers != null) for (var handler : levelHandlers) handler.unload();
    }

    @SubscribeEvent
    public void load(WorldEvent.Load event) {
        if (event.getWorld().isClientSide() != client || event.getWorld() instanceof LittleLevel) return;
        load((Level) event.getWorld());
    }

    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide() != client) return;
        unload((Level) event.getWorld());
    }

}
