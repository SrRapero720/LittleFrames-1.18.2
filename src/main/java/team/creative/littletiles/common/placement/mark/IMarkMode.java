package team.creative.littletiles.common.placement.mark;

import com.mojang.blaze3d.vertex.PoseStack;

import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.placement.PlacementPosition;

public interface IMarkMode {
    
    public boolean allowLowResolution();
    
    public PlacementPosition getPosition();
    
    public void render(LittleGrid positionGrid, PoseStack pose);
    
    public void move(LittleGrid positionGrid, Facing facing);
    
    public void done();
    
}
