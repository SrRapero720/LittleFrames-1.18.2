package team.creative.creativecore.common.gui.controls.parent;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.ControlFormatting.ControlStyleFace;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;

public class GuiScrollX extends GuiParent {
    
    public int maxScroll = 0;
    public SmoothValue scrolled = new SmoothValue(200);
    public boolean dragged;
    public int scrollbarHeight = 3;
    protected int cachedWidth;
    
    public GuiScrollX() {
        this("");
    }
    
    public GuiScrollX(String name) {
        super(name, GuiFlow.STACK_X);
    }
    
    @Override
    public GuiScrollX setDim(int width, int height) {
        return (GuiScrollX) super.setDim(width, height);
    }
    
    @Override
    public GuiScrollX setDim(GuiSizeRule dim) {
        return (GuiScrollX) super.setDim(dim);
    }
    
    @Override
    public GuiScrollX setExpandable() {
        return (GuiScrollX) super.setExpandable();
    }
    
    @Override
    public double getOffsetX() {
        return -scrolled.current();
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED;
    }
    
    public void onScrolled() {
        if (this.scrolled.aimed() < 0)
            this.scrolled.set(0);
        if (this.scrolled.aimed() > maxScroll)
            this.scrolled.set(maxScroll);
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double scrolled) {
        if (super.mouseScrolled(rect, x, y, scrolled))
            return true;
        this.scrolled.set(this.scrolled.aimed() - scrolled * 10);
        onScrolled();
        return true;
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == 0 && rect.getHeight() - y <= scrollbarHeight && needsScrollbar(rect)) {
            playSound(SoundEvents.UI_BUTTON_CLICK);
            dragged = true;
            return true;
        }
        return super.mouseClicked(rect, x, y, button);
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        if (dragged) {
            GuiStyle style = getStyle();
            ControlFormatting formatting = getControlFormatting();
            int completeWidth = (int) (rect.getWidth() - style.getBorder(formatting.border) * 2);
            
            int scrollThingWidth = Math.max(10, Math.min(completeWidth, (int) ((float) completeWidth / cachedWidth * completeWidth)));
            if (cachedWidth < completeWidth)
                scrollThingWidth = completeWidth;
            
            double percent = (x) / (completeWidth - scrollThingWidth);
            this.scrolled.set((int) (percent * maxScroll));
            onScrolled();
        }
        super.mouseMoved(rect, x, y);
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        super.mouseReleased(rect, x, y, button);
        dragged = false;
    }
    
    public boolean needsScrollbar(Rect rect) {
        return cachedWidth > rect.getWidth() - getContentOffset() * 2;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, ControlFormatting formatting, int borderWidth, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        matrix.pushPose();
        super.renderContent(matrix, control, formatting, borderWidth, controlRect, realRect, scale, mouseX, mouseY);
        matrix.popPose();
        
        float controlInvScale = (float) scaleFactorInv();
        matrix.scale(controlInvScale, controlInvScale, controlInvScale);
        
        realRect.scissor();
        GuiStyle style = getStyle();
        
        scrolled.tick();
        
        int completeWidth = control.getWidth() - borderWidth * 2;
        
        int scrollThingWidth = Math.max(10, Math.min(completeWidth, (int) ((float) completeWidth / cachedWidth * completeWidth)));
        if (scrollThingWidth < completeWidth)
            scrollThingWidth = completeWidth;
        double percent = scrolled.current() / maxScroll;
        
        style.get(ControlStyleFace.CLICKABLE, false).render(matrix, control
                .getWidth() - scrollbarHeight - borderWidth, (int) (percent * (completeWidth - scrollThingWidth)) + borderWidth, scrollbarHeight, scrollThingWidth);
        
        maxScroll = Math.max(0, (cachedWidth - completeWidth) + formatting.padding * 2 + 1);
        
        float controlScale = (float) scaleFactor();
        matrix.scale(controlScale, controlScale, controlScale);
    }
    
    @Override
    protected int minWidth(int availableWidth) {
        return 10;
    }
    
    @Override
    protected int minHeight(int width, int availableHeight) {
        return 10;
    }
    
    @Override
    public void flowX(int width, int preferred) {
        int x = 0;
        for (GuiChildControl child : controls) {
            child.setWidth(child.getPreferredWidth(width), width);
            child.setX(x);
            child.flowX();
            x += child.getWidth() + spacing;
        }
        cachedWidth = x;
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {
        super.flowY(width, height - scrollbarHeight, preferred);
    }
    
}
