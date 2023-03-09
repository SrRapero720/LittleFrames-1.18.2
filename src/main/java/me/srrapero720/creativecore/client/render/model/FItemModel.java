package me.srrapero720.creativecore.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FItemModel {
    
    protected final ModelResourceLocation location;
    
    public FItemModel(ModelResourceLocation location) {
        this.location = location;
    }
    
    public void applyCustomOpenGLHackery(PoseStack pose, ItemStack stack, TransformType cameraTransformType) {}

}
