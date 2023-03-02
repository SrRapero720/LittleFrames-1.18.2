package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiNotFound extends LittleStructureGuiControl {
    
    public LittleStructureGuiNotFound(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {}
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        structure.load(item.group.getStructureTag());
        return structure;
    }
    
}