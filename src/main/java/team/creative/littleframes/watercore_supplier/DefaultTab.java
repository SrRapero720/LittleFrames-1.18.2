package team.creative.littleframes.watercore_supplier;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.creative.littleframes.LittleFramesRegistry;

public class DefaultTab extends CreativeModeTab {
    final String iconName;
    public DefaultTab(String label, String item_registry) {
        super(label);
        iconName = item_registry;
    }

    @Override
    public @NotNull ItemStack makeIcon() { return new ItemStack(LittleFramesRegistry.CREATIVE_PICTURE_FRAME.get()); }
}
