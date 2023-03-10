package team.creative.littletiles.common.structure.registry;

import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;
import team.creative.littletiles.common.structure.type.LittleItemHolder;
import team.creative.littletiles.common.structure.type.LittleStructureMessage;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

import java.util.function.BiFunction;

public class LittleStructureRegistry {
    
    public static final NamedHandlerRegistry<LittleStructureType> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static <T extends LittleStructure> LittleStructureType register(String id, Class<T> classStructure, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
        LittleStructureType type = new LittleStructureType(id, classStructure, factory, attribute);
        REGISTRY.register(id, type);
        return type;
    }
    
    public static <T extends LittleStructure> LittleStructureType register(LittleStructureType type) {
        REGISTRY.register(type.id, type);
        return type;
    }
    
    public static void initStructures() {
        REGISTRY.registerDefault("fixed", new LittleStructureType("fixed", LittleFixedStructure.class, LittleFixedStructure::new, LittleAttributeBuilder.NONE));
        
        register("message", LittleStructureMessage.class, LittleStructureMessage::new, LittleAttributeBuilder.NONE).addOutput("message", 1, SignalMode.EQUAL);
        
        LittleStructureBuilder
                .register(new LittleStructureBuilderType(register("item_holder", LittleItemHolder.class, LittleItemHolder::new, new LittleAttributeBuilder().extraRendering())
                        .addInput("filled", 1), "frame"));
        
        LittlePremadeRegistry.initStructures();
    }
}
