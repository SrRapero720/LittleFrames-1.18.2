package team.creative.littletiles.api.common.block;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.render.tile.LittleFRenderBox;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

import java.util.Random;

public interface LittleBlock {
    
    boolean isTranslucent();
    
    boolean is(ItemStack stack);
    
    boolean is(Block block);
    
    boolean is(TagKey<Block> tag);
    
    boolean noCollision();
    
    ItemStack getStack();
    
    String blockName();
    
    BlockState getState();
    
    boolean canBeConvertedToVanilla();
    
    BlockState mirror(BlockState state, Axis axis, LittleVec doubledCenter);
    
    BlockState rotate(BlockState state, Rotation rotation, LittleVec doubledCenter);
    
    SoundType getSoundType();
    
    float getExplosionResistance(LittleTile tile);
    
    void exploded(IParentCollection parent, LittleTile tile, Explosion explosion);
    
    boolean canInteract();
    
    InteractionResult use(IParentCollection parent, LittleTile tile, LittleBox box, Player player, BlockHitResult result);
    
    int getLightValue();
    
    float getEnchantPowerBonus(IParentCollection parent, LittleTile tile);
    
    float getFriction(IParentCollection parent, LittleTile tile, @Nullable Entity entity);
    
    boolean isMaterial(Material material);
    
    boolean isLiquid();
    
    Vec3d modifyAcceleration(IParentCollection parent, LittleTile tile, Entity entity, Vec3d motion);
    
    LittleFRenderBox getRenderBox(LittleGrid grid, RenderType layer, LittleBox box, LittleElement element);
    
    boolean canBeRenderCombined(LittleTile one, LittleTile two);
    
    boolean checkEntityCollision();
    
    void entityCollided(IParentCollection parent, LittleTile tile, Entity entity);
    
    boolean shouldUseStateForRenderType();

    default void randomDisplayTick(IParentCollection parent, LittleTile tile, Random rand) {}
    
}
