package team.creative.creativecore.common.gui.controls.parent;

import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class GuiTable extends GuiParent {
    
    public GuiTable() {
        super(GuiFlow.STACK_Y);
        this.spacing = 0;
    }
    
    public GuiTable(GuiRow... rows) {
        this();
        for (int i = 0; i < rows.length; i++)
            addRow(rows[i]);
    }
    
    public GuiTable addRow(GuiRow row) {
        super.add(row);
        return this;
    }
    
    @Override
    @Deprecated
    public GuiChildControl add(GuiControl control) {
        throw new UnsupportedOperationException();
    }
    
}
