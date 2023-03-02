package team.creative.littletiles.common.gui.controls;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.gui.controls.GuiAnimationViewer.GuiAnimationViewerStorage;

public class GuiAnimationPanel extends GuiParent {
    
    public final GuiTree tree;
    public final GuiAnimationViewerStorage storage;
    public final boolean options;
    
    public GuiAnimationPanel(GuiTree tree, GuiAnimationViewerStorage storage, boolean options) {
        super("animation", GuiFlow.STACK_Y);
        setExpandable();
        
        this.tree = tree;
        this.storage = storage;
        this.options = options;
        
        GuiAnimationViewer viewer = new GuiAnimationViewer("viewer", storage);
        add(viewer.setExpandable());
        
        GuiParent animationButtons = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(animationButtons.setExpandableX());
        
        animationButtons
                .add(new GuiIconButton("perspective", GuiIcon.CAMERA, x -> viewer.nextProjection()).setTooltip(new TextBuilder().translate("gui.recipe.perspective").build()));
        animationButtons.add(new GuiIconButton("home", GuiIcon.HOUSE, x -> viewer.resetView()).setTooltip(new TextBuilder().translate("gui.recipe.home").build()));
        
        GuiParent checkboxes = new GuiParent(GuiFlow.FIT_X).setAlign(Align.CENTER);
        add(checkboxes.setExpandableX());
        
        if (options) {
            checkboxes.add(new GuiCheckBox("filter", tree.hasCheckboxes()).setTranslate("gui.recipe.view.filter").consumeChanged(x -> {
                tree.setCheckboxes(x, false);
                tree.updateTree();
            }));
            
            checkboxes.add(new GuiCheckBox("highlight", storage.highlightSelected()).setTranslate("gui.recipe.view.highlight").consumeChanged(x -> storage.highlightSelected(x)));
        }
    }
    
    public void refresh() {
        if (options) {
            get("filter", GuiCheckBox.class).value = tree.hasCheckboxes();
            get("highlight", GuiCheckBox.class).value = storage.highlightSelected();
        }
    }
    
}
