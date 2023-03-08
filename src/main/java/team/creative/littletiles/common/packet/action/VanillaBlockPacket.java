package team.creative.littletiles.common.packet.action;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;

public class VanillaBlockPacket extends CreativePacket {
    public BlockPos pos;

    
    public VanillaBlockPacket() {
        
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        player.inventoryMenu.broadcastChanges();
    }
    
}
