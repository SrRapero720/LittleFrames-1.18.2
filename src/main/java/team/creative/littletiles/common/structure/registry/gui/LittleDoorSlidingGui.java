package team.creative.littletiles.common.structure.registry.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.animation.timeline.AnimationTimeline;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;

@OnlyIn(Dist.CLIENT)
public class LittleDoorSlidingGui extends LittleDoorBaseGui {
    
    public LittleDoorSlidingGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void populateTimeline(AnimationTimeline timeline, int interpolation) {}
    
}