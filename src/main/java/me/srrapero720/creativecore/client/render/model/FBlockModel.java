package me.srrapero720.creativecore.client.render.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;
import me.srrapero720.creativecore.client.render.box.FRenderBox;

import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public abstract class FBlockModel {
    
    public abstract List<? extends FRenderBox> getBoxes(BlockState state, ModelDataMap data, Random source);
    
    public abstract @NotNull ModelDataMap getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelDataMap modelData);
    
}