package team.creative.creativecore.common.gui.flow;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.VAlign;

public class GuiFlowFitX extends GuiStackX {
    
    @Override
    public int minHeight(List<GuiChildControl> controls, int spacing, int width, int availableHeight) {
        int line = 0;
        int lineHeight = 0;
        int total = 0;
        for (GuiChildControl child : controls) {
            if (child.getY() == line)
                lineHeight = Math.max(lineHeight, child.getMaxHeight(availableHeight));
            else {
                line = child.getY();
                total += lineHeight + spacing;
                lineHeight = child.getMaxHeight(availableHeight);
            }
        }
        total += lineHeight;
        return total;
    }
    
    @Override
    public int preferredHeight(List<GuiChildControl> controls, int spacing, int width, int availableHeight) {
        int line = 0;
        int lineHeight = 0;
        int total = 0;
        for (GuiChildControl child : controls) {
            if (child.getY() == line)
                lineHeight = Math.max(lineHeight, child.getPreferredHeight(availableHeight));
            else {
                line = child.getY();
                total += lineHeight + spacing;
                lineHeight = child.getPreferredHeight(availableHeight);
            }
        }
        total += lineHeight;
        return total;
    }
    
    @Override
    public void flowX(List<GuiChildControl> controls, int spacing, Align align, int width, int preferred) {
        int rowIndex = 0;
        int x = 0;
        List<GuiChildControl> row = new ArrayList<>();
        for (int i = 0; i < controls.size(); i++) {
            GuiChildControl child = controls.get(i);
            int pref = child.getPreferredWidth(width);
            if (width - x >= pref) {
                child.setY(rowIndex);
                row.add(child);
                x += pref + spacing;
            } else {
                super.flowX(row, spacing, align, width, Math.min(width, x));
                row.clear();
                rowIndex++;
                child.setY(rowIndex);
                row.add(child);
                x = 0;
            }
        }
        if (!row.isEmpty())
            super.flowX(row, spacing, align, width, Math.min(width, x));
    }
    
    @Override
    public void flowY(List<GuiChildControl> controls, int spacing, VAlign valign, int width, int height, int preferred) {
        List<GuiChildControl> rows = new ArrayList<>();
        List<GuiChildControl> row = new ArrayList<>();
        int line = 0;
        for (GuiChildControl child : controls) {
            if (child.getY() != line) {
                rows.add(new GuiRowControl(new ArrayList<>(row), spacing, valign, width));
                line = child.getY();
                row.clear();
            }
            
            row.add(child);
        }
        if (!row.isEmpty())
            rows.add(new GuiRowControl(new ArrayList<>(row), spacing, valign, width));
        
        GuiFlow.STACK_Y.flowY(rows, spacing, valign, width, height, preferred);
        
        for (GuiChildControl rowTemp : rows)
            for (GuiChildControl child : ((GuiRowControl) rowTemp).controls)
                child.setY(rowTemp.getY() + child.getY());
    }
    
    public static class GuiRowControl extends GuiChildControl {
        
        final List<GuiChildControl> controls;
        final int spacing;
        final int width;
        final VAlign valign;
        
        public GuiRowControl(List<GuiChildControl> controls, int spacing, VAlign valign, int width) {
            super(null);
            this.controls = controls;
            this.spacing = spacing;
            this.valign = valign;
            this.width = width;
        }
        
        @Override
        public int getContentWidth() {
            return getWidth();
        }
        
        @Override
        public int getMinWidth(int availableWidth) {
            return GuiFlow.STACK_X.minWidth(controls, spacing, availableWidth);
        }
        
        @Override
        public int getMaxWidth(int availableWidth) {
            return -1;
        }
        
        @Override
        public int getPreferredWidth(int availableWidth) {
            return GuiFlow.STACK_X.preferredWidth(controls, spacing, availableWidth);
        }
        
        @Override
        public int getContentHeight() {
            return getHeight();
        }
        
        @Override
        public int getMinHeight(int availableHeight) {
            return GuiFlow.STACK_X.minHeight(controls, spacing, width, availableHeight);
        }
        
        @Override
        public int getMaxHeight(int availableHeight) {
            return -1;
        }
        
        @Override
        public int getPreferredHeight(int availableHeight) {
            return GuiFlow.STACK_X.preferredHeight(controls, spacing, width, availableHeight);
        }
        
        @Override
        public void flowX() {}
        
        @Override
        public void flowY() {
            GuiFlow.STACK_X.flowY(controls, spacing, valign, width, getHeight(), getPreferredHeight(getHeight()));
        }
        
        @Override
        public boolean isExpandableX() {
            return false;
        }
        
        @Override
        public boolean isExpandableY() {
            return false;
        }
        
    }
    
}
