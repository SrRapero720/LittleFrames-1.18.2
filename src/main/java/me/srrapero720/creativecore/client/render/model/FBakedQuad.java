package me.srrapero720.creativecore.client.render.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import me.srrapero720.creativecore.client.render.box.FRenderBox;
import team.creative.creativecore.common.util.mc.ColorUtils;

public class FBakedQuad extends BakedQuad {
    
    public final FRenderBox cube;
    public boolean shouldOverrideColor;
    
    public FBakedQuad(BakedQuad quad, FRenderBox cube, int tintedColor, boolean shouldOverrideColor, Direction facing) {
        this(quad, cube, tintedColor, shouldOverrideColor, facing, false);
    }
    
    private FBakedQuad(BakedQuad quad, FRenderBox cube, int tintedColor, boolean shouldOverrideColor, Direction facing, boolean something) {
        super(copyArray(quad.getVertices()), shouldOverrideColor ? tintedColor : quad.getTintIndex(), facing, quad.getSprite(), quad.isShade());
        this.cube = cube;
        this.shouldOverrideColor = shouldOverrideColor;
    }
    
    private static int[] copyArray(int[] array) {
        int[] newarray = new int[array.length];
        for (int i = 0; i < array.length; i++)
            newarray[i] = array[i];
        return newarray;
    }
    
    public void updateAlpha() {
        int alpha = ColorUtils.alpha(cube.color);
        if (alpha == 255)
            return;
        for (int k = 0; k < 4; k++) {
            int index = k * DefaultVertexFormat.BLOCK.getIntegerSize() + findOffset(DefaultVertexFormat.ELEMENT_COLOR);
            getVertices()[index] = ColorUtils.setAlpha(getVertices()[index], alpha);
        }
    }

    private static int findOffset(VertexFormatElement element) {
        // Divide by 4 because we want the int offset
        var index = DefaultVertexFormat.BLOCK.getElements().indexOf(element);
        return index < 0 ? -1 : DefaultVertexFormat.BLOCK.getOffset(index) / 4;
    }
}
