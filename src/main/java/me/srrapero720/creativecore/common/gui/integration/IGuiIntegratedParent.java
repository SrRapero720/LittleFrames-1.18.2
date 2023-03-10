package me.srrapero720.creativecore.common.gui.integration;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import team.creative.creativecore.common.gui.GuiControl;
import me.srrapero720.creativecore.common.gui.IGuiParent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.event.GuiTooltipEvent;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.geo.Rect;

import java.util.List;
import java.util.Optional;

public interface IGuiIntegratedParent extends IGuiParent {
    
    GuiLayer EMPTY = new GuiLayer("empty") {
        @Override
        public void create() {}
    };
    
    List<GuiLayer> getLayers();
    
    GuiLayer getTopLayer();
    
    default boolean isOpen(Class<? extends GuiLayer> clazz) {
        for (GuiLayer layer : getLayers())
            if (clazz.isInstance(layer))
                return true;
        return false;
    }
    
    @OnlyIn(value = Dist.CLIENT)
    default void render(PoseStack matrixStack, Screen screen, ScreenEventListener listener, int mouseX, int mouseY) {
        int width = screen.width;
        int height = screen.height;
        
        listener.tick();
        Rect screenRect = Rect.getScreenRect();
        
        List<GuiLayer> layers = getLayers();
        for (int i = 0; i < layers.size(); i++) {
            GuiLayer layer = layers.get(i);
            
            if (i == layers.size() - 1) {
                RenderSystem.disableDepthTest();
                if (layer.hasGrayBackground())
                    GuiRenderHelper.gradientRect(matrixStack, 0, 0, width, height, -1072689136, -804253680);
                if (screen instanceof AbstractContainerScreen)
                    MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.DrawBackground((AbstractContainerScreen<?>) screen, matrixStack, mouseX, mouseY));
            }
            
            matrixStack.pushPose();
            int offX = (width - layer.getWidth()) / 2;
            int offY = (height - layer.getHeight()) / 2;
            matrixStack.translate(offX, offY, 0);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Rect controlRect = new Rect(offX, offY, offX + layer.getWidth(), offY + layer.getHeight());
            layer.render(matrixStack, null, controlRect, screenRect.intersection(controlRect), mouseX, mouseY);
            matrixStack.popPose();
            
            RenderSystem.disableScissor();
        }
        
        if (layers.isEmpty())
            return;
        
        GuiLayer layer = getTopLayer();
        GuiTooltipEvent event = layer.getTooltipEvent(null, mouseX - listener.getOffsetX(), mouseY - listener.getOffsetY());
        if (event != null) {
            layer.raiseEvent(event);
            if (!event.isCanceled())
                screen.renderTooltip(matrixStack, event.tooltip, Optional.empty(), mouseX, mouseY, Minecraft.getInstance().font);
        }
    }
    
    @Override
    public default void raiseEvent(GuiEvent event) {}
    
    @Override
    public default void reflow() {}
    
    public void openLayer(GuiLayer layer);
    
    public void closeLayer(int layer);

    @Override
    public default boolean hasGui() {
        return true;
    }
    
    public default GuiControl get(String control) {
        for (GuiLayer layer : getLayers())
            if (control.startsWith(layer.getNestedName()))
                    return layer.get(control.substring(layer.getNestedName().length() + 1));
        return null;
    }
    
//    @Override
//    default Rect toScreenRect(GuiControl control, Rect rect) {
//        if (control instanceof GuiLayer layer) {
//            int offX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() - layer.getWidth()) / 2;
//            int offY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() - layer.getHeight()) / 2;
//            rect.inside(offX, offY);
//        }
//        return rect;
//    }

//    @Override
//    public default Rect toLayerRect(GuiControl control, Rect rect) {
//        return rect;
//    }
//
//    @Override
//    public default IGuiIntegratedParent getIntegratedParent() {
//        return this;
//    }
    
    public void send(CreativePacket message);
    
}