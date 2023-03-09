package me.srrapero720.creativecore.common.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.event.GuiTooltipEvent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.creativecore.common.util.text.TextBuilder;

import java.util.List;

public abstract class GuiControl extends team.creative.creativecore.common.gui.GuiControl {

    private IGuiParent parent;
    public final String name;
    public boolean enabled = true;

    public boolean expandableX = false;
    public boolean expandableY = false;

    public boolean visible = true;

    private List<Component> customTooltip;

    public GuiControl(String name) {
        super(name);
        this.name = name;
    }

    // BASICS

    @Override
    public boolean isClient() {
        if (parent != null)
            return parent.isClient();
        return CreativeCore.loader().getOverallSide().isClient();
    }

    @Override
    public GuiControl setTooltip(List<Component> tooltip) {
        if (tooltip != null && tooltip.isEmpty())
            this.customTooltip = null;
        else
            this.customTooltip = tooltip;
        return this;
    }

    @Override
    public GuiControl setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public GuiControl setFixed() {
        this.expandableX = false;
        this.expandableY = false;
        return this;
    }

    @Override
    public GuiControl setFixedX() {
        this.expandableX = false;
        return this;
    }

    @Override
    public GuiControl setFixedY() {
        this.expandableY = false;
        return this;
    }

    @Override
    public GuiControl setExpandable() {
        this.expandableX = true;
        this.expandableY = true;
        return this;
    }


    public GuiControl setExpandableX() {
        this.expandableX = true;
        return this;
    }

    @Override
    public GuiControl setExpandableY() {
        this.expandableY = true;
        return this;
    }

    @Override
    public GuiControl setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public boolean hasGui() {
        if (parent != null)
            return parent.hasGui();
        return false;
    }


    public void setParent(IGuiParent parent) {
        this.parent = parent;
    }

    @Override
    public team.creative.creativecore.common.gui.IGuiParent getParent() {
        return parent;
    }


    @Override
    public boolean isExpandableX() {
        return expandableX;
    }

    @Override
    public boolean isExpandableY() {
        return expandableY;
    }

    @Override
    public String getNestedName() {
        if (getParent() instanceof GuiControl)
            return ((GuiControl) getParent()).getNestedName() + "." + name;
        return name;
    }

    @Override
    public boolean hasLayer() {
        if (parent instanceof GuiControl)
            return ((GuiControl) parent).hasLayer();
        return false;
    }

    @Override
    public GuiLayer getLayer() {
        if (parent instanceof GuiControl)
            return ((GuiControl) parent).getLayer();
        throw new RuntimeException("Invalid layer control");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiStyle getStyle() {
        if (parent instanceof GuiControl)
            return ((GuiControl) parent).getStyle();
        throw new RuntimeException("Invalid layer control");
    }

    @Override
    public abstract void init();

    @Override
    public abstract void closed();

    @Override
    public abstract void tick();


    public boolean is(String name) {
        if (this.name.equalsIgnoreCase(name))
            return true;
        return false;
    }

    @Override
    public boolean is(String... name) {
        for (int i = 0; i < name.length; i++) {
            if (this.name.equalsIgnoreCase(name[i]))
                return true;
        }
        return false;
    }

    // SIZE

    @Override
    public void reflow() {
        parent.reflow();
    }


    // INTERACTION

    @Override
    public boolean isInteractable() {
        return enabled && visible;
    }

    @Override
    public void mouseMoved(Rect rect, double x, double y) {}

    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseDoubleClicked(Rect rect, double x, double y, int button) {
        return mouseClicked(rect, x, y, button);
    }

    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {}

    @Override
    public void mouseDragged(Rect rect, double x, double y, int button, double dragX, double dragY, double time) {}

    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public void looseFocus() {}

    @Override
    public void raiseEvent(GuiEvent event) {
        if (parent != null)
            parent.raiseEvent(event);
    }

    // APPERANCE

    @Override
    public abstract ControlFormatting getControlFormatting();

    @OnlyIn(Dist.CLIENT)
    public int getContentOffset() {
        return getStyle().getContentOffset(getControlFormatting());
    }

    @Override
    public GuiTooltipEvent getTooltipEvent(Rect rect, double x, double y) {
        List<Component> toolTip = getTooltip();

        if (customTooltip != null)
            if (toolTip == null)
                toolTip = customTooltip;
            else
                toolTip.addAll(customTooltip);

        if (toolTip == null) {
            String langTooltip = translateOrDefault(getNestedName() + ".tooltip", null);
            if (langTooltip != null)
                toolTip = new TextBuilder(langTooltip).build();
        }

        if (toolTip != null)
            return new GuiTooltipEvent(this, toolTip);
        return null;
    }

    @Override
    public List<Component> getTooltip() {
        return null;
    }

    // RENDERING
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return display;
    }

    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return display;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack pose, GuiChildControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        Rect rectCopy = null;
        if (!enabled)
            rectCopy = controlRect.copy();

        int width;
        int height;
        if (control == null) {
            width = (int) controlRect.getWidth();
            height = (int) controlRect.getHeight();
        } else {
            width = control.getWidth();
            height = control.getHeight();
        }

        GuiStyle style = getStyle();
        ControlFormatting formatting = getControlFormatting();

        getBorder(style, style.get(formatting.border)).render(pose, 0, 0, width, height);

        int borderWidth = style.getBorder(formatting.border);

        width -= borderWidth * 2;
        height -= borderWidth * 2;

        getBackground(style, style.get(formatting.face, enabled && realRect.inside(mouseX, mouseY))).render(pose, borderWidth, borderWidth, width, height);

        controlRect.shrink(borderWidth * scale);

        renderContent(pose, control, formatting, borderWidth, controlRect, realRect, scale, mouseX, mouseY);

        if (!enabled) {
            realRect.scissor();
            style.disabled.render(pose, realRect, rectCopy);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, ControlFormatting formatting, int borderWidth, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        controlRect.shrink(formatting.padding * scale);
        if (!enabled)
            pose.pushPose();
        pose.translate(borderWidth + formatting.padding, borderWidth + formatting.padding, 0);
        renderContent(pose, control, controlRect, controlRect.intersection(realRect), scale, mouseX, mouseY);
        if (!enabled)
            pose.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        renderContent(pose, control, controlRect, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY);

    // MINECRAFT

    public Player getPlayer() {
        return parent.getPlayer();
    }

    // UTILS
//    public static MutableComponent translatable(String text) {
//        return new TextComponent(translate(text));
//    }
//
//    public static MutableComponent translatable(String text, Object... parameters) {
//        return new TextComponent(translate(text, parameters));
//    }

    public static String translate(String text) {
        return LanguageUtils.translate(text);
    }

    public static String translate(String text, Object... parameters) {
        return LanguageUtils.translate(text, parameters);
    }

    public static String translateOrDefault(String text, String defaultText) {
        return LanguageUtils.translateOr(text, defaultText);
    }

//    @Environment(EnvType.CLIENT)
//    @OnlyIn(Dist.CLIENT)
//    public static void playSound(SoundInstance sound) {
//        Minecraft.getInstance().getSoundManager().play(sound);
//    }
//
//    @Environment(EnvType.CLIENT)
//    @OnlyIn(Dist.CLIENT)
//    public static void playSound(Holder.Reference<SoundEvent> sound) {
//        playSound(sound.value());
//    }
//
//    @Environment(EnvType.CLIENT)
//    @OnlyIn(Dist.CLIENT)
//    public static void playSound(SoundEvent event) {
//        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F));
//    }
//
//    @Environment(EnvType.CLIENT)
//    @OnlyIn(Dist.CLIENT)
//    public static void playSound(SoundEvent event, float volume, float pitch) {
//        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
//    }
//
//    @Environment(EnvType.CLIENT)
//    @OnlyIn(Dist.CLIENT)
//    public static void playSound(Holder.Reference<SoundEvent> event, float volume, float pitch) {
//        playSound(event.value(), volume, pitch);
//    }
}