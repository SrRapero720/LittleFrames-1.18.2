package team.creative.creativecore.common.gui;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.integration.IGuiIntegratedParent;
import team.creative.creativecore.common.util.math.geo.Rect;

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