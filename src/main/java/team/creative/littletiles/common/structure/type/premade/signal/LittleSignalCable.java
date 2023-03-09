package team.creative.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import me.srrapero720.creativecore.client.render.box.FRenderBox;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleFRenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.network.ISignalStructureTransmitter;

public class LittleSignalCable extends LittleSignalCableBase implements ISignalStructureTransmitter {
    
    public LittleSignalCable(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canConnect(Facing facing) {
        return true;
    }
    
    @Override
    public int getIndex(Facing facing) {
        return facing.ordinal();
    }
    
    @Override
    public Facing getFacing(int index) {
        return Facing.values()[index];
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(SurroundingBox box, LittleBox overallBox, List<LittleFRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        LittleFRenderBox block = (LittleFRenderBox) new LittleFRenderBox(box.getGrid(), overallBox, LittleTilesRegistry.CLEAN.get().defaultBlockState()).setColor(color);
        block.allowOverlap = true;
        cubes.add(block);
    }
    
    public static class LittleStructureTypeCable extends LittleStructureTypeNetwork {
        
        public <T extends LittleStructure> LittleStructureTypeCable(String id, Class<T> structureClass, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid, int bandwidth) {
            super(id, structureClass, factory, attribute, modid, bandwidth, 6);
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<FRenderBox> getItemPreview(LittleGroup previews, boolean translucent) {
            List<FRenderBox> cubes = new ArrayList<>();
            int color = getColor(previews);
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new FRenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTilesRegistry.CLEAN.get()).setColor(color));
            cubes.add(new FRenderBox(0 + size * 2, 0.5F - size * 0.8F, 0.5F - size * 0.8F, 1 - size * 2, 0.5F + size * 0.8F, 0.5F + size * 0.8F, LittleTilesRegistry.SINGLE_CABLE
                    .get()).setColor(color).setKeepUV(true));
            cubes.add(new FRenderBox(1 - size * 2, 0.5F - size, 0.5F - size, 1, 0.5F + size, 0.5F + size, LittleTilesRegistry.CLEAN.get()).setColor(color));
            return cubes;
        }
        
        @Override
        public int getBandwidth() {
            return bandwidth;
        }
        
        @Override
        public void changed() {}
        
        @Override
        public SignalState getState() {
            return null;
        }
        
        @Override
        public void overwriteState(SignalState state) {}
        
        @Override
        public SignalComponentType getComponentType() {
            return SignalComponentType.TRANSMITTER;
        }
        
    }
    
}
