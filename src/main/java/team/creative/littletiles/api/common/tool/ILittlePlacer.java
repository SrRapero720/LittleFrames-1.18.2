package team.creative.littletiles.api.common.tool;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;


public interface ILittlePlacer {
    
    boolean hasTiles(ItemStack stack);
    
    LittleGroup getTiles(ItemStack stack);
    
    LittleGroup getLow(ItemStack stack);
    
    default LittleGroup get(ItemStack stack, boolean low) {
        if (low)
            return getLow(stack);
        return getTiles(stack);
    }

    void saveTiles(ItemStack stack, LittleGroup group);

    boolean containsIngredients(ItemStack stack);

    /** needs to be implemented by any ILittleTile which supports low resolution and
     * only uses full blocks
     * 
     * @param stack
     * @return */
    default LittleVec getCachedSize(ItemStack stack) {
        return null;
    }
    
    /** needs to be implemented by any ILittleTile which supports low resolution and
     * only uses full blocks
     * 
     * @param stack
     * @return */
    default LittleVec getCachedMin(ItemStack stack) {
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    default List<RenderBox> getPositingCubes(Level level, BlockPos pos, ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("structure")) {
            LittleStructureType type = LittleStructureRegistry.REGISTRY.get(stack.getTag().getCompound("structure").getString("id"));
            if (type != null)
                return type.getPositingCubes(level, pos, stack);
        }
        return null;
    }
    
}
