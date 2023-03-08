package me.srrapero720.waterframes.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.StringSplitter;

@Mixin(StringSplitter.class)
public interface StringSplitterAccessor {
    @Accessor
    StringSplitter.WidthProvider getWidthProvider();
}