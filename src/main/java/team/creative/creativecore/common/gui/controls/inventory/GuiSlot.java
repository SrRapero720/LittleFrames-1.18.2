//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.creative.creativecore.common.gui.controls.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GuiSlot extends GuiSlotBase {
    public Slot slot;
    public Container container;
    public int index;

    public GuiSlot(Container container, int index) {
        this("", container, index);
    }

    public GuiSlot(String name, Container container, int index) {
        this(name + index, new Slot(container, index, 0, 0));
    }

    public GuiSlot(String name, Slot slot) {
        super(name);
        this.slot = slot;
    }

    public GuiSlot(Slot slot) {
        this("" + slot.getContainerSlot(), slot);
    }

    public ItemStack getStack() {
        return this.container.getItem(this.index);
    }

    @Override
    public void flowY(int width, int height, int preferred) {

    }

    @Override
    protected int preferredWidth(int availableWidth) {
        return 0;
    }

    @Override
    protected int preferredHeight(int width, int availableHeight) {
        return 0;
    }
}