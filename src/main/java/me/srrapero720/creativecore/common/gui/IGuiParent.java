package me.srrapero720.creativecore.common.gui;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.event.GuiEvent;
import me.srrapero720.creativecore.common.gui.integration.IGuiIntegratedParent;

public interface IGuiParent {

    boolean isContainer();

    boolean isClient();

    Player getPlayer();

    void closeTopLayer();

    void closeLayer(GuiLayer layer);

    void raiseEvent(GuiEvent event);

    void reflow();

    boolean hasGui();

    IGuiIntegratedParent getIntegratedParent();

}