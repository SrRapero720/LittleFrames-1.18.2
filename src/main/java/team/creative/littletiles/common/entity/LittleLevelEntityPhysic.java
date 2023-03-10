package team.creative.littletiles.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.block.LittlePhysicBlock;
import team.creative.littletiles.common.level.little.BlockUpdateLevelSystem;
import team.creative.littletiles.common.level.little.LevelBoundsListener;
import team.creative.littletiles.common.level.little.LittleLevel;

public class LittleLevelEntityPhysic implements LevelBoundsListener {

    public final LittleEntity parent;
    
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;
    private AABB bb;
    private boolean preventPush = false;
    private boolean bbChanged = false;
    public boolean noCollision = false;
    
    public LittleLevelEntityPhysic(LittleEntity parent) {
        this.parent = parent;
        this.bb = parent.getBoundingBox();
    }
    
    public IVecOrigin getOrigin() {
        return parent.getOrigin();
    }
    
    public void load(CompoundTag nbt) {
        minX = nbt.getDouble("x");
        minY = nbt.getDouble("y");
        minZ = nbt.getDouble("z");
        maxX = nbt.getDouble("x2");
        maxY = nbt.getDouble("y2");
        maxZ = nbt.getDouble("z2");
        bb = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        bbChanged = true;
        ((LittleLevel) parent.getSubLevel()).getBlockUpdateLevelSystem().load(nbt.getCompound("bounds"));
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("x", minX);
        nbt.putDouble("y", minY);
        nbt.putDouble("z", minZ);
        nbt.putDouble("x2", maxX);
        nbt.putDouble("y2", maxY);
        nbt.putDouble("z2", maxZ);
        nbt.put("bounds", ((LittleLevel) parent.getSubLevel()).getBlockUpdateLevelSystem().save());
        return nbt;
    }
    
    public AABB getOrientatedBB() {
        return bb;
    }
    
    public AABB getBB() {
        return getOrigin().getAxisAlignedBox(bb);
    }
    
    public void ignoreCollision(Runnable run) {
        preventPush = true;
        try {
            run.run();
        } finally {
            preventPush = false;
        }
    }
    
    public boolean shouldPush() {
        return !preventPush;
    }
    
    public double get(Facing facing) {
        return switch (facing) {
            case EAST -> maxX;
            case WEST -> minX;
            case UP -> maxY;
            case DOWN -> minY;
            case SOUTH -> maxZ;
            case NORTH -> minZ;
        };
    }
    
    public void set(Facing facing, double value) {
        switch (facing) {
            case EAST -> maxX = value;
            case WEST -> minX = value;
            case UP -> maxY = value;
            case DOWN -> minY = value;
            case SOUTH -> maxZ = value;
            case NORTH -> minZ = value;
        };
    }
    
    @Override
    public void rescan(LittleLevel level, BlockUpdateLevelSystem system, Facing facing, Iterable<BlockPos> possible, int boundary) {
        double value = facing.positive ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (BlockPos pos : possible) {
            BlockState state = level.asLevel().getBlockState(pos);
            if (state.getBlock() instanceof LittlePhysicBlock block)
                value = facing.positive ? Math.max(value, block.bound(level, pos, facing)) : Math.min(value, block.bound(level, pos, facing));
            else
                value = facing.positive ? Math.max(value, pos.get(facing.axis.toVanilla()) + 1) : Math.min(value, pos.get(facing.axis.toVanilla()));
            
            if (value == boundary)
                break;
        }
        set(facing, value);
    }
    
    @Override
    public void afterChangesApplied(BlockUpdateLevelSystem system) {
        bb = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        bbChanged = true;
    }
    
    public void moveAndRotateAnimation(double x, double y, double z, double rotX, double rotY, double rotZ) {
        if (x == 0 && y == 0 && z == 0 && rotX == 0 && rotY == 0 && rotZ == 0)
            return;
        
        CollisionCoordinator coordinator = new CollisionCoordinator(x, y, z, rotX, rotY, rotZ, getOrigin(), getOrigin());
        if (LittleTiles.CONFIG.general.enableAnimationCollision)
            moveAndRotateAnimation(coordinator);
        coordinator.move();
    }
    
    public void moveAndRotateAnimation(CollisionCoordinator coordinator) {
        /*if (preventPush)
            return;
        
        noCollision = true;
        
        Level level = parent.getRealLevel();
        List<Entity> entities = level.getEntities(parent, coordinator.computeSurroundingBox(orientatedBB), noAnimation);
        List<OBB> worldCollisionBoxes = Collections.EMPTY_LIST;
        if (!entities.isEmpty()) {
            
            // PHASE ONE
            // Gather all affected boxes
            List<AABB> surroundingBoxes = new ArrayList<>(worldCollisionBoxes.size());
            for (OBB box : worldCollisionBoxes) {
                if (box.cache == null)
                    box.buildCache();
                box.cache.reset();
                
                surroundingBoxes.add(coordinator.computeSurroundingBox(box));
            }
            
            // PHASE TWO
            // Move entities by their center
            PushCache[] caches = new PushCache[entities.size()];
            for (int j = 0; j < entities.size(); j++) {
                Entity entity = entities.get(j);
                
                AABB entityBB = entity.getBoundingBox();
                Vec3d center = new Vec3d(entityBB.minX + (entityBB.maxX - entityBB.minX) * 0.5D, entityBB.minY + (entityBB.maxY - entityBB.minY) * 0.5D, entityBB.minZ + (entityBB.maxZ - entityBB.minZ) * 0.5D);
                double radius = center.distanceSqr(entityBB.minX, entityBB.minY, entityBB.minZ);
                
                Double t = null;
                OBB pushingBox = null;
                //Facing facing;
                
                checking_all_boxes: for (int i = 0; i < surroundingBoxes.size(); i++) {
                    if (surroundingBoxes.get(i).intersects(entityBB)) {
                        // Check for earliest hit
                        OBB box = worldCollisionBoxes.get(i);
                        
                        if (!box.cache.isCached())
                            box.cache.planes = CollidingPlane.getPlanes(box, box.cache, coordinator);
                        
                        // Binary search
                        for (CollidingPlane plane : box.cache.planes) {
                            Double tempT = plane.binarySearch(t, entityBB, radius, center, coordinator);
                            if (tempT != null) {
                                t = tempT;
                                pushingBox = box;
                                //facing = plane.facing;
                                if (t == 0)
                                    break checking_all_boxes;
                            }
                        }
                    }
                }
                
                // Applying found t
                if (t != null) {
                    PushCache cache = new PushCache();
                    //cache.facing = facing;
                    
                    Vec3d newCenter = new Vec3d(center);
                    coordinator.transform(newCenter, 1 - t);
                    
                    cache.pushBox = pushingBox;
                    cache.entityBox = entityBB.move(newCenter.x - center.x, newCenter.y - center.y, newCenter.z - center.z);
                    caches[j] = cache;
                }
            }
            
            coordinator.move();
            
            // PHASE THREE
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                PushCache cache = caches[i];
                
                if (cache == null) {
                    cache = new PushCache();
                    cache.entityBox = entity.getBoundingBox();
                }
                
                cache.entityBoxOrientated = coordinator.origin.getOrientatedBox(cache.entityBox);
                Vec3d center = new Vec3d(cache.entityBox.minX + (cache.entityBox.maxX - cache.entityBox.minX) * 0.5D, cache.entityBox.minY + (cache.entityBox.maxY - cache.entityBox.minY) * 0.5D, cache.entityBox.minZ + (cache.entityBox.maxZ - cache.entityBox.minZ) * 0.5D);
                coordinator.origin.transformPointToFakeWorld(center);
                
                Axis one = null;
                Axis two = null;
                
                boolean ignoreOne = false;
                Boolean positiveOne = null;
                boolean ignoreTwo = false;
                Boolean positiveTwo = null;
                
                double maxVolume = 0;
                
                List<OBB> intersecting = new ArrayList<>();
                List<Facing> intersectingFacing = new ArrayList<>();
                
                for (OBB box : worldCollisionBoxes) {
                    if (box == cache.pushBox || box.intersects(cache.entityBoxOrientated)) {
                        //box.cache.planes = CollidingPlane.getPlanes(box, box.cache, coordinator);
                        
                        Facing facing = CollidingPlane.getDirection(coordinator, box, center);
                        if (facing == null || (!coordinator.hasRotation && (!coordinator.hasTranslation || (coordinator.translation.get(facing.axis) > 0) != facing.positive)))
                            continue;
                        
                        double intersectingVolume = box.getIntersectionVolume(cache.entityBoxOrientated);
                        
                        if (maxVolume == 0 || intersectingVolume > maxVolume) {
                            maxVolume = intersectingVolume;
                            cache.facing = facing;
                        }
                        
                        intersecting.add(box);
                        intersectingFacing.add(facing);
                    }
                }
                
                if (intersecting.isEmpty())
                    continue;
                
                one = cache.facing.one();
                two = cache.facing.two();
                
                positiveOne = null;
                positiveTwo = null;
                
                for (Facing facing : intersectingFacing) {
                    
                    if (!ignoreOne && facing.axis == one) {
                        if (positiveOne == null)
                            positiveOne = facing.positive;
                        else if (facing.positive != positiveOne)
                            ignoreOne = true;
                    } else if (!ignoreTwo && facing.axis == two) {
                        if (positiveTwo == null)
                            positiveTwo = facing.positive;
                        else if (facing.positive != positiveTwo)
                            ignoreTwo = true;
                    }
                    
                    if (ignoreOne && ignoreTwo)
                        break;
                }
                
                // Now things are ready. Go through all intersecting ones and push the box out
                Vec3d pushVec = new Vec3d();
                pushVec.set(cache.facing.axis, cache.facing.offset());
                if (!ignoreOne && positiveOne != null)
                    pushVec.set(one, positiveOne ? 1 : -1);
                if (!ignoreTwo && positiveTwo != null)
                    pushVec.set(two, positiveTwo ? 1 : -1);
                
                double scale = 0;
                
                for (int j = 0; j < intersecting.size(); j++) {
                    Facing facing = intersectingFacing.get(j);
                    
                    if ((ignoreOne && facing.axis == one) || (ignoreTwo && facing.axis == two))
                        continue;
                    
                    scale = intersecting.get(j).getPushOutScale(scale, cache.entityBoxOrientated, pushVec);
                }
                
                boolean collidedHorizontally = entity.horizontalCollision;
                boolean collidedVertically = entity.verticalCollision;
                boolean onGround = entity.isOnGround();
                
                AABB originalBox = entity.getBoundingBox();
                
                Vec3d rotatedVec = new Vec3d(pushVec);
                coordinator.origin.rotation().transform(rotatedVec);
                
                double moveX = cache.entityBox.minX - originalBox.minX + rotatedVec.x * scale;
                double moveY = cache.entityBox.minY - originalBox.minY + rotatedVec.y * scale;
                double moveZ = cache.entityBox.minZ - originalBox.minZ + rotatedVec.z * scale;
                
                entity.move(MoverType.SELF, new Vec3(moveX, moveY, moveZ));
                
                if (entity instanceof ServerPlayer)
                    LittleAnimationHandlers.setPushedByDoor((ServerPlayer) entity);
                
                if (LittleTiles.CONFIG.general.enableCollisionMotion)
                    entity.getDeltaMovement().add(moveX, moveY, moveZ);
                
                if (moveX != 0 || moveZ != 0)
                    collidedHorizontally = true;
                if (moveY != 0) {
                    collidedVertically = true;
                    onGround = true;
                }
                
                entity.horizontalCollision = collidedHorizontally;
                entity.verticalCollision = collidedVertically;
                entity.setOnGround(onGround);
                
            }
            
            for (OBB box : worldCollisionBoxes)
                box.cache.reset();
        }
        
        for (OrientationAwareEntity child : parent.children()) {
            coordinator.reset(child.getOrigin());
            child.moveAndRotateAnimation(coordinator);
        }
        
        noCollision = false;*/
    }
    
    public void updateBoundingBox() {
        if (bb == null || parent.getSubLevel() == null)
            return;
        
        if (parent.getOrigin().hasChanged() || parent.getOrigin().hasChanged()) {
            parent.markOriginChange();
            parent.setBoundingBox(parent.getOrigin().getAxisAlignedBox(bb));
            parent.resetOriginChange();
            bbChanged = false;
        } else if (bbChanged) {
            parent.setBoundingBox(parent.getOrigin().getAxisAlignedBox(bb));
            bbChanged = false;
        }
    }
}
