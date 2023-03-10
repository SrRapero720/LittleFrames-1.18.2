package team.creative.littletiles.common.packet.action;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.client.LittleTilesClient;

public class ActionMessagePacket extends CreativePacket {
    
    public List<Component> message;
    
    public ActionMessagePacket(List<Component> message) {
        this.message = message;
    }
    
    public ActionMessagePacket() {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClient(Player player) {
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
