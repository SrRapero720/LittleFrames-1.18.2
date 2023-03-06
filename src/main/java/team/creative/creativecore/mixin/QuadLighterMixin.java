package team.creative.creativecore.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.creativecore.client.render.model.CreativeQuadLighter;

@Mixin(value = VertexLighterFlat.class, remap = false)
public abstract class QuadLighterMixin extends QuadGatheringTransformer implements CreativeQuadLighter {
    @Unique public int customTint = -1;
    @Shadow private int tint;

    @Override
    @Shadow
    public abstract void setState(BlockState state);

    @Inject(method = "updateColor([F[FFFFFI)V", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void mixColor(float[] normal, float[] color, float x, float y, float z, float tint, int multiplier, CallbackInfo ci) {
        if (customTint != -1) {
            this.tint = (int) tint;
            color[0] = ((customTint >> 16) & 0xFF) / 255F;
            color[1] = ((customTint >> 8) & 0xFF) / 255F;
            color[2] = (customTint & 0xFF) / 255F;
            ci.cancel();
        }
    }


//    @Inject(at = @At(value = "HEAD"), method = "getColorFast(I)[F", cancellable = true, remap = false)
//    public void getColorMultiplierHook(int tint, CallbackInfoReturnable info) {
//        if (customTint != -1) {
//
//            cachedTintIndex = tint;
//            cachedTintColor[0] = ((customTint >> 16) & 0xFF) / 255F;
//            cachedTintColor[1] = ((customTint >> 8) & 0xFF) / 255F;
//            cachedTintColor[2] = (customTint & 0xFF) / 255F;
//            info.setReturnValue(cachedTintColor);
//        }
//    }

//
    @Override
    public void setCustomTint(int tint) {
        this.customTint = tint;
    }
}
