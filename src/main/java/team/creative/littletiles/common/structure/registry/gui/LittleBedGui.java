package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.controls.GuiDirectionIndicator;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.bed.LittleBed;

public class LittleBedGui extends LittleStructureGuiControl {
    
    public LittleBedGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        //registerEventClick(x -> get("tileviewer", GuiTileViewer.class).updateIndicator(((GuiStateButtonMapped<Facing>) get("direction")).getSelected().opposite(), get("relativeDirection", GuiDirectionIndicator.class)));
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        GuiParent right = new GuiParent();
        add(right);
        
        /*GuiTileViewer tile = new GuiTileViewer("tileviewer", group.getGrid());
        tile.setViewDirection(Facing.UP);
        right.add(tile);*/
        
        GuiParent left = new GuiParent();
        add(left);
        
        LittleVec size = item.group.getSize();
        Facing facing = Facing.EAST;
        if (size.x < size.z)
            facing = Facing.SOUTH;
        if (structure instanceof LittleBed)
            facing = ((LittleBed) structure).direction;
        GuiStateButtonMapped<Facing> button = new GuiStateButtonMapped<Facing>("direction", new TextMapBuilder<Facing>()
                .addComponent(Facing.HORIZONTA_VALUES, x -> new TextComponent(x.name)));
        button.select(facing);
        left.add(button);
        
        GuiDirectionIndicator indicator = new GuiDirectionIndicator("relativeDirection", Facing.UP);
        left.add(indicator);
        //tile.updateIndicator(facing.opposite(), indicator);
    }
    
    @Override
    public LittleStructure save(LittleStructure bed) {
        ((LittleBed) bed).direction = ((GuiStateButtonMapped<Facing>) get("direction")).getSelected();
        return bed;
    }
    
}