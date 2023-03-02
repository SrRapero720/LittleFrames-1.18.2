package team.creative.littletiles.common.structure.signal.output;

import java.text.ParseException;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.packet.update.OutputUpdate;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponentOutput;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.SignalState.SignalStateSize;
import team.creative.littletiles.common.structure.signal.component.InternalSignal;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.SignalOutputHandlerToggle;

public class InternalSignalOutput extends InternalSignal<InternalComponentOutput> {
    
    public final SignalMode defaultMode;
    public final boolean syncToClient;
    public SignalInputCondition condition;
    public SignalOutputHandler handler;
    
    public InternalSignalOutput(LittleStructure parent, InternalComponentOutput component) {
        super(parent, component);
        this.defaultMode = component.defaultMode;
        this.syncToClient = component.syncToClient;
    }
    
    @Override
    public void changed() {
        parent.performInternalOutputChange(this);
        parent.schedule();
        if (syncToClient)
            LittleTiles.NETWORK.sendToClient(new OutputUpdate(parent.getStructureLocation(), component.index, getState()), getStructureLevel(), parent.getPos());
    }
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.OUTPUT;
    }
    
    @Override
    public void load(CompoundTag nbt) {
        overwrite(getState().load(nbt.get("state")));
        try {
            if (nbt.contains("con"))
                condition = SignalInputCondition.parseInput(nbt.getString("con"));
            else
                condition = null;
        } catch (ParseException e) {
            condition = null;
        }
        SignalMode mode = defaultMode;
        if (nbt.contains("mode"))
            mode = SignalMode.valueOf(nbt.getString("mode"));
        int delay = nbt.getInt("delay");
        if (condition != null)
            delay = Math.max((int) Math.ceil(condition.calculateDelay()), nbt.getInt("delay"));
        handler = SignalOutputHandler.create(this, mode, delay, nbt, parent);
    }
    
    @Override
    public CompoundTag save(boolean preview, CompoundTag nbt) {
        nbt.put("state", getState().save());
        if (condition != null)
            nbt.putString("con", condition.write());
        nbt.putString("mode", handler == null ? defaultMode.name() : handler.getMode().name());
        if (handler != null) {
            if (handler.delay > 0)
                nbt.putInt("delay", handler.delay);
            else
                nbt.remove("delay");
            handler.write(preview, nbt);
        }
        return nbt;
    }
    
    public void toggle() {
        if (handler instanceof SignalOutputHandlerToggle)
            ((SignalOutputHandlerToggle) handler).triggerToggle();
    }
    
    public void update() {
        if (condition == null)
            return;
        int bandwidth = getBandwidth();
        if (bandwidth > 0) {
            SignalState outputState = SignalState.create(bandwidth);
            SignalState result = condition.test(getStructure(), false);
            if (result.size() == SignalStateSize.SINGLE)
                outputState = outputState.fill(result.any());
            else
                outputState = outputState.fill(result);
            handler.schedule(outputState);
        }
    }
    
}
