package team.creative.littletiles.common.placement;

import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.level.LittleEntity;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

/** PlacementPosition + Previews -> PlacementPreview (can be rendered) + Player/ Cause -> Placement */
public class PlacementPreview {


    public final LittleGroup previews;
    private final boolean canBeMoved;
    public final PlacementMode mode;
    public final PlacementPosition position;
    public final UUID levelUUID;
    public final LittleBoxAbsolute box;

    PlacementPreview(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box, boolean canBeMoved) {
        this(level instanceof ISubLevel ? ((ISubLevel) level).getHolder().getUUID() : null, previews, mode, position, box, canBeMoved);
    }

    PlacementPreview(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box, boolean canBeMoved) {
        this.levelUUID = levelUUID;
        this.previews = previews;
        if (previews.hasStructureIncludeChildren() && !mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = position;
        this.box = box;
        this.canBeMoved = canBeMoved;
    }

    PlacementPreview(Level level, LittleGroupAbsolute previews, PlacementMode mode, Facing facing, boolean canBeMoved) {
        this(level instanceof ISubLevel ? ((ISubLevel) level).getHolder().getUUID() : null, previews, mode, facing, canBeMoved);
    }

    PlacementPreview(UUID levelUUID, LittleGroupAbsolute previews, PlacementMode mode, Facing facing, boolean canBeMoved) {
        this.levelUUID = levelUUID;
        this.previews = previews.group;
        if (this.previews.hasStructureIncludeChildren() && !mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = new PlacementPosition(previews.pos, new LittleVecGrid(), facing);
        this.box = previews.getBox();
        this.canBeMoved = canBeMoved;
    }

    public Level getLevel(Entity entity) throws MissingAnimationException {
        Level level = entity.level;
        if (levelUUID != null) {
            LittleEntity levelEntity = LittleAnimationHandlers.find(level.isClientSide, levelUUID);
            if (levelEntity == null)
                throw new MissingAnimationException(levelUUID);

            level = (Level) levelEntity.getSubLevel();
        }
        return level;
    }

    public Set<BlockPos> getPositions() {
        return previews.getPositions(position.getPos());
    }

    public PlacementPreview copy() {
        return new PlacementPreview(levelUUID, previews.copy(), mode, position.copy(), box.copy(), canBeMoved);
    }

    public void mirror(Axis axis, LittleBoxAbsolute box) {
        position.mirror(axis, box);
        previews.mirror(axis, box.getDoubledCenter(position.getPos()));
    }

    public LittleIngredients getBeforePlaceIngredients() {
        return mode.getBeforePlaceIngredients(previews);
    }
}
