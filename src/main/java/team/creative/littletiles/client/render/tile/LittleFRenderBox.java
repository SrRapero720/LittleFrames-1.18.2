package team.creative.littletiles.client.render.tile;

import net.minecraft.world.level.block.state.BlockState;
import me.srrapero720.creativecore.client.render.box.FRenderBox;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleFRenderBox extends FRenderBox {

    public LittleBox box;

    public LittleFRenderBox(AlignedBox box) {
        super(box);
    }

    public LittleFRenderBox(AlignedBox box, BlockState state) {
        super(box, state);
    }

    public LittleFRenderBox(LittleGrid grid, LittleBox box) {
        super((float) grid.toVanillaGrid(box.minX), (float) grid.toVanillaGrid(box.minY), (float) grid.toVanillaGrid(box.minZ), (float) grid.toVanillaGrid(box.maxX), (float) grid
            .toVanillaGrid(box.maxY), (float) grid.toVanillaGrid(box.maxZ), (BlockState) null);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }

    public LittleFRenderBox(LittleGrid grid, LittleBox box, BlockState state) {
        super((float) grid.toVanillaGrid(box.minX), (float) grid.toVanillaGrid(box.minY), (float) grid.toVanillaGrid(box.minZ), (float) grid.toVanillaGrid(box.maxX), (float) grid
            .toVanillaGrid(box.maxY), (float) grid.toVanillaGrid(box.maxZ), state);
        this.color = ColorUtils.WHITE;
        this.box = box;
    }

    public LittleFRenderBox(LittleGrid grid, LittleBox box, LittleElement element) {
        super((float) grid.toVanillaGrid(box.minX), (float) grid.toVanillaGrid(box.minY), (float) grid.toVanillaGrid(box.minZ), (float) grid.toVanillaGrid(box.maxX), (float) grid
            .toVanillaGrid(box.maxY), (float) grid.toVanillaGrid(box.maxZ), element.getState());
        this.color = element.color;
        this.box = box;
    }

    @Override
    public LittleFRenderBox setColor(int color) {
        return (LittleFRenderBox) super.setColor(color);
    }

}
