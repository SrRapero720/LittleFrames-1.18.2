package team.creative.littletiles.common.structure;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupHolder;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.LittleNeighborUpdateCollector;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.update.StructureUpdate;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.connection.block.StructureBlockConnector;
import team.creative.littletiles.common.structure.connection.children.LevelChildrenList;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.MissingChildException;
import team.creative.littletiles.common.structure.exception.MissingParentException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.exception.RemovedStructureException;
import team.creative.littletiles.common.structure.signal.LittleSignalHandler;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.InternalSignalInput;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;

public abstract class LittleStructure implements ISignalSchedulable, ILevelPositionProvider {
    
    public final LittleStructureType type;
    public final IStructureParentCollection mainBlock;
    private final List<StructureBlockConnector> blocks = new ArrayList<>();
    
    public String name;
    
    public final LevelChildrenList children = new LevelChildrenList(this);
    
    private HashMap<Integer, SignalExternalOutputHandler> externalHandler;
    private final InternalSignalInput[] inputs;
    private final InternalSignalOutput[] outputs;
    
    private boolean signalChanged = false;
    
    public LittleStructure(LittleStructureType type, IStructureParentCollection mainBlock) {
        this.type = type;
        this.mainBlock = mainBlock;
        this.inputs = type.createInputs(this);
        this.outputs = type.createOutputs(this);
    }
    
    // ================Basics================
    
    @Override
    public Level getLevel() {
        if (mainBlock.isRemoved())
            return null;
        return mainBlock.getLevel();
    }
    
    @Override
    public Level getComponentLevel() {
        return getLevel();
    }
    
    public boolean hasLevel() {
        return mainBlock != null && mainBlock.getLevel() != null && !mainBlock.isRemoved();
    }
    
    public boolean isClient() {
        return mainBlock != null && mainBlock.isClient();
    }
    
    @Override
    public BlockPos getPos() {
        return mainBlock.getPos();
    }
    
    public int getIndex() {
        return mainBlock.getIndex();
    }
    
    public int getAttribute() {
        return type.attribute;
    }
    
    public StructureLocation getStructureLocation() {
        return new StructureLocation(this);
    }
    
    // ================Connections================
    
    public boolean hasParent() {
        return children.hasParent();
    }
    
    public StructureChildConnection getParent() {
        return children.getParent();
    }
    
    public void checkConnections() throws CorruptedConnectionException, NotYetConnectedException {
        if (mainBlock.isRemoved())
            throw new RemovedStructureException();
        
        for (StructureBlockConnector block : blocks)
            block.connect();
        
        try {
            if (hasParent())
                getParent().checkConnection();
        } catch (CorruptedConnectionException e) {
            throw new MissingParentException(getParent(), e);
        }
        
        for (StructureChildConnection child : children.all())
            try {
                child.getStructure().checkConnections();
            } catch (CorruptedConnectionException e) {
                throw new MissingChildException(child, e);
            }
    }
    
    /** use it at your own risk getAttribute() must return the new attribute */
    public void tryAttributeChangeForBlocks() throws CorruptedConnectionException, NotYetConnectedException {
        int attribute = getAttribute();
        mainBlock.setAttribute(attribute);
        for (StructureBlockConnector block : blocks)
            try {
                block.getList().setAttribute(attribute);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public int count() throws CorruptedConnectionException, NotYetConnectedException {
        int count = mainBlock.size();
        for (StructureBlockConnector block : blocks)
            count += block.count();
        return count;
    }
    
    public boolean isChildOf(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException {
        if (structure == this)
            return true;
        if (hasParent())
            return getParent().getStructure().isChildOf(structure);
        return false;
    }
    
    public LittleStructure findTopStructure() throws CorruptedConnectionException, NotYetConnectedException {
        if (hasParent())
            return getParent().getStructure().findTopStructure();
        return this;
    }
    
    // ================Tiles================
    
    public void addBlock(StructureParentCollection block) {
        blocks.add(new StructureBlockConnector(this, block.getPos().subtract(getPos())));
    }
    
    public Iterable<BlockPos> positions() {
        return new Iterable<BlockPos>() {
            
            @Override
            public Iterator<BlockPos> iterator() {
                
                return new Iterator<BlockPos>() {
                    
                    boolean first = true;
                    Iterator<StructureBlockConnector> iterator = blocks.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return first || iterator.hasNext();
                    }
                    
                    @Override
                    public BlockPos next() {
                        if (first) {
                            first = false;
                            return mainBlock.getPos();
                        }
                        return iterator.next().getAbsolutePos();
                    }
                };
            }
        };
    }
    
    public Iterable<BETiles> blocks() throws CorruptedConnectionException, NotYetConnectedException {
        checkConnections();
        return new Iterable<BETiles>() {
            
            @Override
            public Iterator<BETiles> iterator() {
                
                return new Iterator<BETiles>() {
                    
                    boolean first = true;
                    Iterator<StructureBlockConnector> iterator = blocks.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return first || iterator.hasNext();
                    }
                    
                    @Override
                    public BETiles next() {
                        if (first) {
                            first = false;
                            return mainBlock.getBE();
                        }
                        try {
                            return iterator.next().getBlockEntity();
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
    
    public Iterable<IStructureParentCollection> blocksList() throws CorruptedConnectionException, NotYetConnectedException {
        checkConnections();
        return new Iterable<IStructureParentCollection>() {
            
            @Override
            public Iterator<IStructureParentCollection> iterator() {
                
                return new Iterator<IStructureParentCollection>() {
                    
                    boolean first = true;
                    Iterator<StructureBlockConnector> iterator = blocks.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return first || iterator.hasNext();
                    }
                    
                    @Override
                    public IStructureParentCollection next() {
                        if (first) {
                            first = false;
                            return mainBlock;
                        }
                        try {
                            return iterator.next().getList();
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
    
    public Iterable<Pair<IStructureParentCollection, LittleTile>> tiles() throws CorruptedConnectionException, NotYetConnectedException {
        Iterator<IStructureParentCollection> iterator = blocksList().iterator();
        return new Iterable<Pair<IStructureParentCollection, LittleTile>>() {
            
            @Override
            public Iterator<Pair<IStructureParentCollection, LittleTile>> iterator() {
                return new Iterator<Pair<IStructureParentCollection, LittleTile>>() {
                    
                    Iterator<LittleTile> inBlock = null;
                    Pair<IStructureParentCollection, LittleTile> pair = null;
                    
                    @Override
                    public boolean hasNext() {
                        while (inBlock == null || !inBlock.hasNext()) {
                            if (!iterator.hasNext())
                                return false;
                            IStructureParentCollection list = iterator.next();
                            pair = new Pair<>(list, null);
                            inBlock = list.iterator();
                        }
                        return true;
                    }
                    
                    @Override
                    public Pair<IStructureParentCollection, LittleTile> next() {
                        pair.setValue(inBlock.next());
                        return pair;
                    }
                };
            }
        };
    }
    
    public HashMapList<BlockPos, IStructureParentCollection> collectAllBlocksListSameWorld() throws CorruptedConnectionException, NotYetConnectedException {
        return collectAllBlocksListSameWorld(new HashMapList<>());
    }
    
    protected HashMapList<BlockPos, IStructureParentCollection> collectAllBlocksListSameWorld(HashMapList<BlockPos, IStructureParentCollection> map) throws CorruptedConnectionException, NotYetConnectedException {
        for (IStructureParentCollection list : blocksList())
            map.add(list.getPos(), list);
        for (StructureChildConnection child : children.all())
            if (!child.isLinkToAnotherWorld())
                child.getStructure().collectAllBlocksListSameWorld(map);
        return map;
    }
    
    // ================Placing================
    
    /** takes name of stack and connects the structure to its children (does so recursively)
     * 
     * @param stack */
    public void placedStructure(@Nullable ItemStack stack) {
        CompoundTag nbt;
        if (name == null && stack != null && (nbt = stack.getTagElement("display")) != null && nbt.contains("Name", 8))
            name = nbt.getString("Name");
        if (!isClient())
            schedule();
    }
    
    public void notifyAfterPlaced() {
        afterPlaced();
        for (StructureChildConnection child : children.all())
            try {
                child.getStructure().notifyAfterPlaced();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    protected void afterPlaced() {}
    
    // ================Save and loading================
    
    public void load(CompoundTag nbt) {
        blocks.clear();
        
        // LoadTiles
        if (nbt.contains("b")) {
            blocks.clear();
            int[] array = nbt.getIntArray("b");
            for (int i = 0; i + 2 < array.length; i += 3)
                blocks.add(new StructureBlockConnector(this, new BlockPos(array[i], array[i + 1], array[i + 2])));
        }
        
        if (nbt.contains("n"))
            name = nbt.getString("n");
        else
            name = null;
        
        children.load(nbt);
        
        for (StructureDirectionalField field : type.directional) {
            if (nbt.contains(field.saveKey))
                field.createAndSet(this, nbt);
            else
                field.set(this, failedLoadingRelative(nbt, field));
        }
        
        if (nbt.contains("s")) {
            ListTag list = nbt.getList("s", 10);
            externalHandler = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                try {
                    SignalExternalOutputHandler handler = new SignalExternalOutputHandler(this, list.getCompound(i));
                    externalHandler.put(handler.index, handler);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        loadExtra(nbt);
        if (inputs != null)
            for (int i = 0; i < inputs.length; i++)
                inputs[i].load(nbt);
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                outputs[i].load(nbt.getCompound(outputs[i].component.identifier));
    }
    
    protected Object failedLoadingRelative(CompoundTag nbt, StructureDirectionalField field) {
        return field.getDefault();
    }
    
    protected abstract void loadExtra(CompoundTag nbt);
    
    public CompoundTag savePreview(CompoundTag nbt, BlockPos newCenter) {
        LittleVecGrid vec = new LittleVecGrid(new LittleVec(mainBlock.getGrid(), getPos().subtract(newCenter)), mainBlock.getGrid());
        
        LittleVecGrid inverted = vec.copy();
        inverted.invert();
        
        for (StructureDirectionalField field : type.directional) {
            Object value = field.get(this);
            field.move(value, vec);
            field.save(nbt, value);
            field.move(value, inverted);
        }
        
        saveInternalExtra(nbt, true);
        return nbt;
    }
    
    public void save(CompoundTag nbt) {
        children.save(nbt);
        
        int[] array = new int[blocks.size() * 3];
        for (int i = 0; i < blocks.size(); i++) {
            StructureBlockConnector block = blocks.get(i);
            array[i * 3] = block.pos.getX();
            array[i * 3 + 1] = block.pos.getY();
            array[i * 3 + 2] = block.pos.getZ();
        }
        if (array.length > 0)
            nbt.putIntArray("b", array);
        
        for (StructureDirectionalField field : type.directional) {
            Object value = field.get(this);
            field.save(nbt, value);
        }
        
        saveInternalExtra(nbt, false);
    }
    
    protected void saveInternalExtra(CompoundTag nbt, boolean preview) {
        nbt.putString("id", type.id);
        if (name != null)
            nbt.putString("n", name);
        else
            nbt.remove("n");
        
        if (externalHandler != null && !externalHandler.isEmpty()) {
            ListTag list = new ListTag();
            for (SignalExternalOutputHandler handler : externalHandler.values())
                list.add(handler.write(preview));
            nbt.put("s", list);
        }
        if (inputs != null)
            for (int i = 0; i < inputs.length; i++)
                inputs[i].save(preview, nbt);
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                nbt.put(outputs[i].component.identifier, outputs[i].save(preview, new CompoundTag()));
            
        saveExtra(nbt);
    }
    
    protected abstract void saveExtra(CompoundTag nbt);
    
    public void unload() {}
    
    // ====================Destroy====================
    
    public void onLittleTileDestroy() throws CorruptedConnectionException, NotYetConnectedException {
        if (hasParent()) {
            getParent().getStructure().onLittleTileDestroy();
            return;
        }
        
        checkConnections();
        LittleNeighborUpdateCollector neighbor = new LittleNeighborUpdateCollector(getLevel());
        removeStructure(neighbor);
        neighbor.process();
    }
    
    public void removeStructure(LittleNeighborUpdateCollector neighbor) throws CorruptedConnectionException, NotYetConnectedException {
        checkConnections();
        onStructureDestroyed();
        
        for (StructureChildConnection child : children.all())
            child.destroyStructure(neighbor);
        
        if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated())
            ((IAnimatedStructure) this).destroyAnimation();
        else {
            neighbor.add(mainBlock.getPos());
            for (StructureBlockConnector block : blocks) {
                neighbor.add(block.getAbsolutePos());
                block.remove();
            }
            mainBlock.getBE().updateTilesSecretly((x) -> x.removeStructure(getIndex()));
        }
        
    }
    
    public void callStructureDestroyedToSameWorld() {
        for (StructureChildConnection child : children.all())
            if (!child.isLinkToAnotherWorld())
                try {
                    child.getStructure().callStructureDestroyedToSameWorld();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        onStructureDestroyed();
    }
    
    /** Is called before the structure is removed */
    @Override
    public void onStructureDestroyed() {
        unload();
    }
    
    // ================Signal================
    
    public Iterable<ISignalStructureComponent> inputs() {
        return new Iterable<ISignalStructureComponent>() {
            
            @Override
            public Iterator<ISignalStructureComponent> iterator() {
                return new Iterator<ISignalStructureComponent>() {
                    
                    Iterator<StructureChildConnection> iterator = children.iteratorAll();
                    ISignalStructureComponent next;
                    
                    @Override
                    public boolean hasNext() {
                        if (next == null) {
                            while (iterator.hasNext()) {
                                StructureChildConnection connection = iterator.next();
                                try {
                                    if (connection.getStructure() instanceof ISignalStructureComponent && ((ISignalStructureComponent) connection.getStructure())
                                            .getComponentType() == SignalComponentType.INPUT) {
                                        next = (ISignalStructureComponent) connection.getStructure();
                                        break;
                                    }
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                            }
                        }
                        return next != null;
                    }
                    
                    @Override
                    public ISignalStructureComponent next() {
                        ISignalStructureComponent result = next;
                        next = null;
                        return result;
                    }
                };
            }
        };
    }
    
    public Iterable<ISignalStructureComponent> outputs() {
        return new Iterable<ISignalStructureComponent>() {
            
            @Override
            public Iterator<ISignalStructureComponent> iterator() {
                return new Iterator<ISignalStructureComponent>() {
                    
                    Iterator<StructureChildConnection> iterator = children.iteratorAll();
                    ISignalStructureComponent next;
                    
                    @Override
                    public boolean hasNext() {
                        if (next == null) {
                            while (iterator.hasNext()) {
                                StructureChildConnection connection = iterator.next();
                                try {
                                    if (connection.getStructure() instanceof ISignalStructureComponent && ((ISignalStructureComponent) connection.getStructure())
                                            .getComponentType() == SignalComponentType.OUTPUT) {
                                        next = (ISignalStructureComponent) connection.getStructure();
                                        break;
                                    }
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                            }
                        }
                        return next != null;
                    }
                    
                    @Override
                    public ISignalStructureComponent next() {
                        ISignalStructureComponent result = next;
                        next = null;
                        return result;
                    }
                };
            }
        };
    }
    
    @Override
    public void notifyChange() {
        if (hasParent())
            try {
                getParent().getStructure().processSignalChanges();
                return;
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        processSignalChanges();
    }
    
    protected void processSignalChanges() {
        if (externalHandler != null && !externalHandler.isEmpty())
            for (SignalExternalOutputHandler handler : externalHandler.values())
                handler.update();
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                outputs[i].update();
        for (StructureChildConnection child : children.all())
            try {
                child.getStructure().processSignalChanges();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @Override
    public boolean isStillAvailable() {
        return !mainBlock.isRemoved();
    }
    
    @Override
    public boolean hasChanged() {
        return signalChanged;
    }
    
    @Override
    public void markChanged() {
        signalChanged = true;
    }
    
    @Override
    public void markUnchanged() {
        signalChanged = false;
    }
    
    public void changed(ISignalComponent changed) {
        schedule();
    }
    
    public InternalSignalInput getInput(int id) {
        if (inputs != null && id < inputs.length)
            return inputs[id];
        return null;
    }
    
    public int internalInputCount() {
        return inputs == null ? 0 : inputs.length;
    }
    
    public InternalSignalOutput getOutput(int id) {
        if (outputs != null && id < outputs.length)
            return outputs[id];
        return null;
    }
    
    public int internalOutputCount() {
        return outputs == null ? 0 : outputs.length;
    }
    
    public SignalExternalOutputHandler getExternalOutput(int index) {
        return externalHandler.get(index);
    }
    
    public boolean hasExternalOutputs() {
        return externalHandler != null && !externalHandler.isEmpty();
    }
    
    public Iterable<SignalExternalOutputHandler> externalOutputs() {
        return externalHandler.values();
    }
    
    public void setExternalOutputs(HashMap<Integer, SignalExternalOutputHandler> handlers) {
        this.externalHandler = handlers;
    }
    
    public void performInternalOutputChange(InternalSignalOutput output) {}
    
    public void receiveInternalOutputChange(InternalSignalOutput output) {}
    
    // ====================Previews====================
    
    public LittleGroupAbsolute getAbsolutePreviews(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        return new LittleGroupAbsolute(pos, getPreviews(pos));
    }
    
    public LittleGroup getPreviews(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        CompoundTag structureNBT = new CompoundTag();
        this.savePreview(structureNBT, pos);
        
        List<LittleGroup> childrenGroup = new ArrayList<>();
        for (StructureChildConnection child : children.children())
            childrenGroup.add(child.getStructure().getPreviews(pos));
        
        LittleGroup previews = new LittleGroup(structureNBT, childrenGroup);
        
        for (Pair<IStructureParentCollection, LittleTile> pair : tiles())
            LittleGroupAbsolute.add(previews, pos, pair.key, pair.value);
        
        for (Entry<String, StructureChildConnection> entry : children.extensionEntries())
            previews.children.addExtension(entry.getKey(), entry.getValue().getStructure().getPreviews(pos));
        
        previews.convertToSmallest();
        return previews;
    }
    
    public LittleGroupAbsolute getAbsolutePreviewsSameWorldOnly(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        return new LittleGroupAbsolute(pos, getPreviewsSameWorldOnly(pos));
    }
    
    public LittleGroup getPreviewsSameWorldOnly(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        CompoundTag structureNBT = new CompoundTag();
        this.savePreview(structureNBT, pos);
        
        List<LittleGroup> childrenGroup = new ArrayList<>();
        for (StructureChildConnection child : children.children())
            if (child.isLinkToAnotherWorld())
                childrenGroup.add(new LittleGroupHolder(child.getStructure()));
            else
                childrenGroup.add(child.getStructure().getPreviewsSameWorldOnly(pos));
            
        LittleGroup previews = new LittleGroup(structureNBT, childrenGroup);
        
        for (Pair<IStructureParentCollection, LittleTile> pair : tiles())
            LittleGroupAbsolute.add(previews, pos, pair.key, pair.value);
        
        for (Entry<String, StructureChildConnection> entry : children.extensionEntries())
            if (entry.getValue().isLinkToAnotherWorld())
                previews.children.addExtension(entry.getKey(), new LittleGroupHolder(entry.getValue().getStructure()));
            else
                previews.children.addExtension(entry.getKey(), entry.getValue().getStructure().getPreviewsSameWorldOnly(pos));
            
        previews.convertToSmallest();
        return previews;
    }
    
    public MutableBlockPos getMinPos(MutableBlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        for (BlockPos tePos : positions())
            pos.set(Math.min(pos.getX(), tePos.getX()), Math.min(pos.getY(), tePos.getY()), Math.min(pos.getZ(), tePos.getZ()));
        
        for (StructureChildConnection child : children.all())
            child.getStructure().getMinPos(pos);
        
        return pos;
    }
    
    // ====================Helpers====================
    
    public SurroundingBox getSurroundingBox() throws CorruptedConnectionException, NotYetConnectedException {
        SurroundingBox box = new SurroundingBox(true, getLevel());
        box.add(mainBlock);
        for (StructureBlockConnector block : blocks)
            box.add(block.getList());
        return box;
    }
    
    public double getPercentVolume() throws CorruptedConnectionException, NotYetConnectedException {
        return getSurroundingBox().getPercentVolume();
    }
    
    public Vec3d getHighestCenterVec() throws CorruptedConnectionException, NotYetConnectedException {
        return getSurroundingBox().getHighestCenterVec();
    }
    
    public LittleVecAbsolute getHighestCenterPoint() throws CorruptedConnectionException, NotYetConnectedException {
        return getSurroundingBox().getHighestCenterPoint();
    }
    
    // ====================Packets====================
    
    public void updateStructure() {
        if (getLevel() == null || isClient())
            return;
        LittleSignalHandler.queueStructureForUpdatePacket(this);
    }
    
    public void sendUpdatePacket() {
        if (mainBlock.isRemoved())
            return;
        CompoundTag nbt = new CompoundTag();
        save(nbt);
        LittleTiles.NETWORK.sendToClient(new StructureUpdate(getStructureLocation(), nbt), getLevel(), getPos());
    }
    
    // ====================Extra====================
    
    public ItemStack getStructureDrop() throws CorruptedConnectionException, NotYetConnectedException {
        if (hasParent())
            return findTopStructure().getStructureDrop();
        
        checkConnections();
        BlockPos pos = getMinPos(getPos().mutable());
        
        ItemStack stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.get());
        stack.setTag(LittleGroup.save(getPreviews(pos)));
        
        if (name != null) {
            CompoundTag display = new CompoundTag();
            display.putString("Name", name);
            stack.getTag().put("display", display);
        }
        return stack;
    }
    
    public boolean canInteract() {
        return false;
    }
    
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        return InteractionResult.PASS;
    }
    
    public boolean isBed(LivingEntity player) {
        return false;
    }
    
    public Direction getBedDirection() {
        return null;
    }
    
    public void onEntityCollidedWithBlock(Level level, IStructureParentCollection parent, BlockPos pos, Entity entityIn) {}
    
    public void onUpdatePacketReceived() {}
    
    public int getLightValue(BlockPos pos) {
        return 0;
    }
    
    public float getExplosionResistance() {
        return 0;
    }
    
    // ====================Active====================
    
    public boolean hasStructureColor() {
        return false;
    }
    
    public int getStructureColor() {
        return -1;
    }
    
    public int getDefaultColor() {
        return -1;
    }
    
    public void paint(int color) {}
    
    public void tick() {}
    
    /** only server side **/
    public void queueForNextTick() {
        LittleSignalHandler.queueStructureForNextTick(this);
    }
    
    /** only server side **/
    public boolean queueTick() {
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderTick(PoseStack pose, MultiBufferSource buffer, BlockPos pos, float partialTickTime) {}
    
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistance() {
        return 0;
    }
    
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void getRenderingBoxes(BlockPos pos, RenderType layer, List<LittleRenderBox> boxes) {}
    
    public VoxelShape getExtraShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    
    public List<VoxelShape> collectOddShapes(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, List<VoxelShape> shapes, AABB bb) {
        return shapes;
    }
    
    public void neighbourChanged() {}
    
    // ====================Mods====================
    
    @Deprecated
    public void mirrorForWarpDrive(LittleGrid context, Axis axis) {
        List<StructureBlockConnector> newBlocks = new ArrayList<>(blocks.size());
        for (StructureBlockConnector block : blocks)
            newBlocks.add(new StructureBlockConnector(this, axis.mirror(block.pos)));
        
        blocks.clear();
        blocks.addAll(newBlocks);
        
        for (StructureDirectionalField relative : type.directional)
            relative.mirror(relative.get(this), context, axis, context.rotationCenter);
    }
    
    @Deprecated
    public void rotateForWarpDrive(LittleGrid context, Rotation rotation, int steps) {
        List<StructureBlockConnector> newBlocks = new ArrayList<>(blocks.size());
        for (StructureBlockConnector block : blocks) {
            BlockPos pos = block.pos;
            for (int rotationStep = 0; rotationStep < steps; rotationStep++)
                pos = rotation.transform(pos);
            newBlocks.add(new StructureBlockConnector(this, pos));
        }
        
        blocks.clear();
        blocks.addAll(newBlocks);
        
        for (StructureDirectionalField relative : type.directional)
            relative.rotate(relative.get(this), context, rotation, context.rotationCenter);
    }
    
    public String info() {
        List<String> infos = new ArrayList<>();
        if (inputs != null)
            for (int i = 0; i < inputs.length; i++)
                infos.add("a" + i + ":" + inputs[i].getState().print(inputs[i].getBandwidth()));
        for (ISignalStructureComponent component : inputs())
            try {
                infos.add("i" + component.getId() + ":" + component.getState().print(component.getBandwidth()) + component.getNetwork());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                infos.add("i" + component.getId() + ":broken");
            }
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                infos.add("b" + i + ":" + outputs[i].getState().print(outputs[i].getBandwidth()));
        for (ISignalStructureComponent component : outputs())
            try {
                infos.add("o" + component.getId() + ":" + component.getState().print(component.getBandwidth()) + component.getNetwork());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                infos.add("o" + component.getId() + ":broken");
            }
        return String.join(",", infos);
    }
    
}
