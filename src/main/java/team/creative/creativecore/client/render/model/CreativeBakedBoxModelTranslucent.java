package team.creative.creativecore.client.render.model;

import net.minecraft.client.resources.model.ModelResourceLocation;

public class CreativeBakedBoxModelTranslucent extends CreativeBakedBoxModel {
    
    public CreativeBakedBoxModelTranslucent(ModelResourceLocation location, CreativeItemBoxModel item, CreativeBlockModel block) {
        super(location, item, block, false);
    }
    
    @Override
    public boolean translucent() {
        return true;
    }
    
}
