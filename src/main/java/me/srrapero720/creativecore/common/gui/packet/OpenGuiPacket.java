package me.srrapero720.creativecore.common.gui.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.common.gui.GuiLayer;
import me.srrapero720.creativecore.common.gui.handler.GuiCreator;
import team.creative.creativecore.common.gui.integration.ContainerIntegration;
import me.srrapero720.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.common.network.CreativePacket;

public class OpenGuiPacket extends CreativePacket {
    public String name;
    public CompoundTag nbt;

    public OpenGuiPacket() {
    }

    public OpenGuiPacket(String name, CompoundTag nbt) {
        this.name = name;
        this.nbt = nbt;
    }

    public void executeClient(Player player) {
        AbstractContainerMenu var3 = player.containerMenu;
        if (var3 instanceof IGuiIntegratedParent gui) {
            gui.openLayer((GuiCreator.REGISTRY.get(this.name)).function.apply(this.nbt, player));
        }

    }

    public void executeServer(ServerPlayer player) {
        openGuiOnServer(GuiCreator.REGISTRY.get(this.name), this.nbt, player);
    }

    public static void openGuiOnServer(GuiCreator creator, CompoundTag nbt, ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, x) ->
                new ContainerIntegration(CreativeCore.GUI_CONTAINER, id, x, creator.function.apply(nbt, player)), new TextComponent(creator.getName())));
        CreativeCore.NETWORK.sendToClient(new OpenGuiPacket(creator.getName(), nbt), player);
    }
}