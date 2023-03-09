package team.creative.littletiles.common.entity.level;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.matrix.ChildVecOrigin;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import me.srrapero720.creativecore.common.util.type.itr.FilterIterator;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.client.render.entity.LittleLevelRenderManager;
import team.creative.littletiles.common.entity.INoPushEntity;
import team.creative.littletiles.common.entity.OrientationAwareEntity;
import team.creative.littletiles.common.entity.physic.LittleLevelEntityPhysic;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.LittleChunkSerializer;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.math.vec.LittleHitResult;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.direct.StructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.server.level.little.LittleServerLevel;
import team.creative.littletiles.server.level.little.SubServerLevel;

public abstract class LittleEntity extends Entity implements OrientationAwareEntity, INoPushEntity {
    
    private Iterable<OrientationAwareEntity> childrenItr = () -> new FilterIterator<OrientationAwareEntity>(entities(), OrientationAwareEntity.class);
    
    private LittleSubLevel subLevel;
    private StructureAbsolute center;
    private IVecOrigin origin;
    protected boolean hasOriginChanged = false;
    private StructureConnection structure;
    
    public final LittleLevelEntityPhysic physic = new LittleLevelEntityPhysic(this);
    
    public double initalOffX;
    public double initalOffY;
    public double initalOffZ;
    public double initalRotX;
    public double initalRotY;
    public double initalRotZ;
    
    // ================Constructors================
    
    public LittleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public LittleEntity(EntityType<?> type, Level level, LittleSubLevel subLevel, StructureAbsolute center, LocalStructureLocation location) {
        super(type, level);
        setSubLevel(subLevel);
        setCenter(center);
        this.structure = new StructureConnection((Level) subLevel, location);
        
        setPos(center.baseOffset.getX(), center.baseOffset.getY(), center.baseOffset.getZ());
        
        physic.ignoreCollision(() -> {
            initialTick();
            this.initalOffX = origin.offX();
            this.initalOffY = origin.offY();
            this.initalOffZ = origin.offZ();
            this.initalRotX = origin.rotX();
            this.initalRotY = origin.rotY();
            this.initalRotZ = origin.rotZ();
        });
        
        origin.tick();
    }
    
    public boolean isReal() {
        if (level instanceof ISubLevel sub)
            level = sub.getRealLevel();
        return !(level instanceof IOrientatedLevel);
    }
    
    // ================Origin================
    
    @Override
    public void markOriginChange() {
        hasOriginChanged = true;
        for (OrientationAwareEntity child : children())
            child.markOriginChange();
    }
    
    public void resetOriginChange() {
        hasOriginChanged = false;
    }
    
    @Override
    protected void defineSynchedData() {}
    
    @Override
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    public LittleSubLevel getSubLevel() {
        return subLevel;
    }
    
    public Level getRealLevel() {
        if (level instanceof ISubLevel sub)
            return sub.getRealLevel();
        return level;
    }
    
    public LittleEntity getTopLevelEntity() {
        if (level instanceof ISubLevel sub)
            return ((LittleEntity) sub.getHolder()).getTopLevelEntity();
        return this;
    }
    
    public CompoundTag saveExtraClientData() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("physic", physic.save());
        return nbt;
    }
    
    public void initSubLevelClient(StructureAbsolute absolute, CompoundTag extraData) {
        setSubLevel(SubServerLevel.createSubLevel(level));
        setCenter(absolute);
        physic.load(extraData.getCompound("physic"));
    }
    
    protected void setSubLevel(LittleSubLevel subLevel) {
        this.subLevel = subLevel;
        this.subLevel.setHolder(this);
        ((LittleLevel) this.subLevel).registerLevelBoundListener(physic);
    }
    
    public StructureAbsolute getCenter() {
        return center;
    }
    
    public void setCenter(StructureAbsolute center) {
        this.center = center;
        this.subLevel.setOrigin(center.rotationCenter);
        this.origin = this.subLevel.getOrigin();
        for (OrientationAwareEntity entity : children())
            entity.parentVecOriginChange(origin);
    }
    
    @Override
    public void parentVecOriginChange(IVecOrigin origin) {
        ((ChildVecOrigin) origin).parent = origin;
    }
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        return structure.getStructure();
    }
    
    public void markRemoved() {
        // TODO THINK ABOUT WHAT TO DO WITH THIS METHOD
    }
    
    @Override
    public void moveAndRotateAnimation(CollisionCoordinator coordinator) {
        physic.moveAndRotateAnimation(coordinator);
    }
    
    public AABB getRealBB() {
        if (level instanceof ISubLevel or)
            return or.getOrigin().getAxisAlignedBox(getBoundingBox());
        return getBoundingBox();
    }
    
    public Vec3 getRealCenter() {
        if (level instanceof ISubLevel or)
            return or.getOrigin().transformPointToWorld(position());
        return position();
    }
    
    // ================Children================
    
    public Iterable<Entity> entities() {
        return ((LittleLevel) subLevel).entities();
    }
    
    public Iterable<OrientationAwareEntity> children() {
        return childrenItr;
    }
    
    // ================Rendering================
    
    @OnlyIn(Dist.CLIENT)
    public LittleLevelRenderManager getRenderManager() {
        return ((LittleClientLevel) subLevel).renderManager;
    }
    
    // ================Ticking================
    
    public abstract void initialTick();
    
    public abstract void onTick();
    
    @Override
    public void performTick() {
        
        origin.tick();
        
        if (level instanceof ISubLevel) {
            if (!level.isClientSide)
                this.setSharedFlag(6, this.isCurrentlyGlowing());
            super.baseTick();
        } else
            super.tick();
        
        children().forEach(x -> x.performTick());
        onTick();
        subLevel.tick();
        
        subLevel.getBlockUpdateLevelSystem().tick(this);
        physic.updateBoundingBox();
        
        setPosRaw(center.baseOffset.getX() + origin.offXLast(), center.baseOffset.getY() + origin.offYLast(), center.baseOffset.getZ() + origin.offZLast());
        setOldPosAndRot();
        setPosRaw(center.baseOffset.getX() + origin.offX(), center.baseOffset.getY() + origin.offY(), center.baseOffset.getZ() + origin.offZ());
        
        if (!level.isClientSide && subLevel.getBlockUpdateLevelSystem().isEntirelyEmpty())
            destroyAnimation();
    }
    
    // ================Save&Load================
    
    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        setSubLevel(SubServerLevel.createSubLevel(level));
        
        physic.load(nbt.getCompound("physic"));
        
        this.initalOffX = nbt.getDouble("iX");
        this.initalOffY = nbt.getDouble("iY");
        this.initalOffZ = nbt.getDouble("iZ");
        this.initalRotX = nbt.getDouble("iX");
        this.initalRotY = nbt.getDouble("iY");
        this.initalRotZ = nbt.getDouble("iZ");
        
        setCenter(new StructureAbsolute("center", nbt));
        
        LittleServerLevel sub = (LittleServerLevel) subLevel;
        ListTag chunks = nbt.getList("chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag chunk = chunks.getCompound(i);
            sub.load(new ChunkPos(chunk.getInt("xPos"), chunk.getInt("zPos")), chunk);
        }
        
        this.structure = new StructureConnection((Level) subLevel, nbt.getCompound("structure"));
        try {
            this.structure.getStructure();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            e.printStackTrace();
        }
        
        loadLevelEntity(nbt);
        
        physic.updateBoundingBox();
    }
    
    public abstract void loadLevelEntity(CompoundTag nbt);
    
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.put("physic", physic.save());
        
        center.save("center", nbt);
        
        nbt.putDouble("iX", initalOffX);
        nbt.putDouble("iY", initalOffY);
        nbt.putDouble("iZ", initalOffZ);
        nbt.putDouble("iX", initalRotX);
        nbt.putDouble("iY", initalRotY);
        nbt.putDouble("iZ", initalRotZ);
        
        LittleServerLevel sub = (LittleServerLevel) subLevel;
        ListTag chunks = new ListTag();
        for (ChunkAccess chunk : sub.chunks())
            chunks.add(LittleChunkSerializer.write(sub, chunk));
        nbt.put("chunks", chunks);
        
        // TODO May need to save entities?????
        
        nbt.put("structure", structure.write());
        
        saveLevelEntity(nbt);
    }
    
    public abstract void saveLevelEntity(CompoundTag nbt);
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        LittleAnimationHandlers.get(level).add(this);
    }
    
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        LittleAnimationHandlers.get(level).remove(this);
    }
    
    // ================MC Hooks================
    
    @Override
    public void setPos(double x, double y, double z) {
        super.setPosRaw(x, y, z); // Fixes bounding box
    }
    
    @Override
    protected AABB makeBoundingBox() {
        return physic.getBB();
    }
    
    @Override
    public boolean isOnFire() {
        return false;
    }
    
    @Override
    public boolean fireImmune() {
        return true;
    }
    
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
    
    public void destroyAnimation() {
        this.kill();
    }
    
    @Override
    public void kill() {
        subLevel.unload();
        super.kill();
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        return InteractionResult.PASS;
    }
    
    // ================Hit Result================
    
    public LittleHitResult rayTrace(Vec3 pos, Vec3 look) {
        LittleHitResult result = null;
        double distance = 0;
        for (Entity entity : entities()) {
            if (entity instanceof LittleEntity levelEntity) {
                Vec3 newPos = levelEntity.origin.transformPointToFakeWorld(pos);
                Vec3 newLook = levelEntity.origin.transformPointToFakeWorld(look);
                
                if (levelEntity.physic.getOrientatedBB().intersects(newPos, newLook)) {
                    LittleHitResult tempResult = levelEntity.rayTrace(pos, look);
                    if (tempResult == null)
                        continue;
                    double tempDistance = newPos.distanceTo(tempResult.hit.getLocation());
                    if (result == null || tempDistance < distance) {
                        result = tempResult;
                        distance = tempDistance;
                    }
                }
            } else {
                Vec3 newPos = origin.transformPointToFakeWorld(pos);
                Vec3 newLook = origin.transformPointToFakeWorld(look);
                if (entity.getBoundingBox().intersects(newPos, newLook)) {
                    LittleHitResult tempResult = new LittleHitResult(this, new EntityHitResult(entity, entity.getBoundingBox().clip(newPos, newLook).get()), subLevel);
                    double tempDistance = newPos.distanceTo(tempResult.hit.getLocation());
                    if (result == null || tempDistance < distance) {
                        result = tempResult;
                        distance = tempDistance;
                    }
                }
            }
        }
        
        Vec3 newPos = origin.transformPointToFakeWorld(pos);
        Vec3 newLook = origin.transformPointToFakeWorld(look);
        HitResult tempResult = subLevel.asLevel().clip(new ClipContext(newPos, newLook, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        if (tempResult == null || tempResult.getType() != Type.BLOCK || !(tempResult instanceof BlockHitResult))
            return result;
        if (result == null || pos.distanceTo(tempResult.getLocation()) < distance)
            return new LittleHitResult(this, tempResult, subLevel);
        return result;
    }
    
    public InteractionResult onRightClick(@Nullable Player player, HitResult result) {
        if (result == null || !(result instanceof BlockHitResult))
            return InteractionResult.PASS;
        
        return subLevel.asLevel().getBlockState(((BlockHitResult) result).getBlockPos()).use((Level) subLevel, player, InteractionHand.MAIN_HAND, (BlockHitResult) result);
    }
    
    // ================CLIENT================
    
    @Override
    public boolean shouldRender(double x, double y, double z) {
        Vec3 center = getRealCenter();
        double d0 = center.x - x;
        double d1 = center.y - y;
        double d2 = center.z - z;
        return this.shouldRenderAtSqrDistance(d0 * d0 + d1 * d1 + d2 * d2);
    }
    
    public boolean hasLoaded() {
        return subLevel != null;
    }
}
