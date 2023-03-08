package me.srrapero720.waterframes.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor
    double getLastMouseEventTime();
}