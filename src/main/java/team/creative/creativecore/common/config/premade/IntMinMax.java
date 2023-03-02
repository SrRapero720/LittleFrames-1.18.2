package team.creative.creativecore.common.config.premade;

import java.util.Random;

import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;

public class IntMinMax implements ICreativeConfig {
    
    @CreativeConfig
    public int min;
    @CreativeConfig
    public int max;
    
    public IntMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    public int next(Random rand) {
        if (min == max)
            return min;
        return min + rand.nextInt(max - min);
    }
    
    @Override
    public void configured(Side side) {
        if (min > max) {
            int temp = min;
            this.min = max;
            this.max = temp;
        }
    }
    
    public int spanLength() {
        return max - min;
    }
    
}
