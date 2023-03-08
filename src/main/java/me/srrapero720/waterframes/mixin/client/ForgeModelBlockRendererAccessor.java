package me.srrapero720.waterframes.mixin.client;

import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ForgeBlockModelRenderer.class, remap = false)
public interface ForgeModelBlockRendererAccessor {
    
    @Accessor
    ThreadLocal<VertexLighterFlat> getLighterFlat();
    
    @Accessor
    ThreadLocal<VertexLighterSmoothAo> getLighterSmooth();
    
}
