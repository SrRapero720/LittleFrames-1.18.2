//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.srrapero720.creativecore.common.gui.controls.inventory;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import me.srrapero720.creativecore.common.gui.GuiControl;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.inventory.GuiSlotBase;
import team.creative.creativecore.common.gui.style.ControlFormatting;

public class GuiInventoryGrid extends GuiParent {
    public final Container container;
    protected boolean hasFixedSize;
    private int fixedSize;
    protected boolean reverse;
    private int cols;
    private int rows;
    private int cachedCols;
    private int cachedRows;
    private boolean allChanged = false;
    private List<Consumer<GuiSlot>> listeners;

    public GuiInventoryGrid(String name, Container container) {
        this(name, container, (int)Math.ceil(Math.sqrt((double)container.getContainerSize())));
        this.hasFixedSize = false;
    }

    public GuiInventoryGrid(String name, Container container, int cols) {
        this(name, container, cols, (int)Math.ceil((double)container.getContainerSize() / (double)cols));
    }

    public GuiInventoryGrid(String name, Container container, int cols, int rows) {
        this(name, container, cols, rows, (c, i) -> new Slot(c, i, 0, 0));
    }

    public GuiInventoryGrid(String name, Container container, int cols, int rows, BiFunction<Container, Integer, Slot> slotFactory) {
        super(name);
        this.hasFixedSize = true;
        this.cols = cols;
        this.rows = rows;
        this.container = container;
        this.fixedSize = Math.min(container.getContainerSize(), cols * rows);
        for (int i = 0; i < fixedSize; i++) {
            GuiChildControl child = super.add(new GuiSlot(slotFactory.apply(container, i)));
            child.rect.maxX = GuiSlotBase.SLOT_SIZE;
            child.rect.maxY = GuiSlotBase.SLOT_SIZE;
        }
    }

    public void setChanged() {
        allChanged = true;
        if (listeners != null)
            for (Consumer<GuiSlot> listener : listeners)
                listener.accept(null);
    }

    /** @deprecated */
    @Deprecated
    public GuiChildControl add(GuiControl control) {
        throw new UnsupportedOperationException();
    }

    public int getMinWidth() {
        return this.hasFixedSize ? this.cols * 18 : 18;
    }

    public int getMinHeight() {
        return this.hasFixedSize ? this.rows * 18 : 18;
    }

    public void flowX(int width, int preferred) {
        this.cachedCols = width / 18;
        if (this.hasFixedSize) {
            this.cachedCols = Math.min(this.cachedCols, this.cols);
        }

        int offset = (width - this.cachedCols * 18) / 2;
        int i = 0;

        for(Iterator var5 = this.controls.iterator(); var5.hasNext(); ++i) {
            GuiChildControl control = (GuiChildControl)var5.next();
            control.setX(offset + i % this.cachedCols * 18);
            control.flowX();
        }

    }

    public void flowY(int height, int preferred) {
        this.cachedRows = height / 18;
        if (this.hasFixedSize) {
            this.cachedRows = Math.min(this.cachedRows, this.rows);
        }

        int offset = (height - this.cachedRows * 18) / 2;
        int i = this.reverse ? this.controls.size() - 1 : 0;
        Iterator var5 = this.controls.iterator();

        while(var5.hasNext()) {
            GuiChildControl control = (GuiChildControl)var5.next();
            int row = i / this.cachedCols;
            control.setY(offset + row * 18);
            control.flowY();
            if (row > this.cachedRows) {
                control.control.visible = false;
            } else {
                control.control.visible = true;
            }

            if (this.reverse) {
                --i;
            } else {
                ++i;
            }
        }

    }

    protected int preferredWidth() {
        return this.cols * 18;
    }

    protected int preferredHeight() {
        return (int)Math.ceil((double)this.container.getContainerSize() / (double)this.cachedCols) * 18;
    }

    public ControlFormatting getControlFormatting() {
        return ControlFormatting.TRANSPARENT;
    }
}
