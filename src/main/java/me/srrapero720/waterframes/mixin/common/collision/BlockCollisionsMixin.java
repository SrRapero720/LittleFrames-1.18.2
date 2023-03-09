package me.srrapero720.waterframes.mixin.common.collision;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin {
    
    @Shadow
    @Final
    private AABB box;
    
    @Shadow
    @Final
    private CollisionContext context;
    
    @Shadow
    @Final
    private CollisionGetter collisionGetter;
    
    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;
    
    @Unique
    private Iterator<VoxelShape> extraShapes;
    
    @Inject(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Cursor3D;advance()Z"), cancellable = true, require = 1)
    private void computeStart(CallbackInfoReturnable<VoxelShape> info) {
        if (extraShapes != null)
            if (extraShapes.hasNext())
                info.setReturnValue(extraShapes.next());
            else
                extraShapes = null;
    }
    
    @Inject(method = "computeNext", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"),
            require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    private void computeBlock(CallbackInfoReturnable<VoxelShape> info, int i, int j, int k, int l, BlockGetter blockgetter, BlockState blockstate) {
        if (blockstate.getBlock() instanceof BlockTile block) {
            List<VoxelShape> list = block.getOddShapes(blockstate, this.collisionGetter, this.pos, this.context, this.box);
            if (list != null)
                extraShapes = list.iterator();
        }
    }
    
    @Inject(method = "<init>(Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Z)V", at = @At("RETURN"), require = 1)
    private void constructorEnd(CollisionGetter level, @Nullable Entity entity, AABB bb, boolean onlySuffocatingBlocks, CallbackInfo info) {

    }
    
}
