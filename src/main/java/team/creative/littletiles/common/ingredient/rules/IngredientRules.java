package team.creative.littletiles.common.ingredient.rules;

import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.filter.premade.BlockFilters;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.LittleTilesRegistry;

public class IngredientRules {
    
    private static PairList<Filter<Block>, BlockIngredientRule> blockRules = new PairList<>();
    
    public static PairList<Filter<Block>, BlockIngredientRule> getBlockRules() {
        return blockRules;
    }
}
