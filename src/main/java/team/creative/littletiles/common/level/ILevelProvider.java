package team.creative.littletiles.common.level;

import net.minecraft.world.level.Level;

public interface ILevelProvider {
    
    default boolean hasLevel() {
        return getLevel() != null;
    }
    
    Level getLevel();
    
}
