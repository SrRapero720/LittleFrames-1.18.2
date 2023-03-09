package team.creative.littletiles.common.structure.registry.premade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.mc.NBTUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittlePremadeRegistry {
    
    private static final List<LittlePremadeType> STRUCTURES = new ArrayList<>();
    private static final HashMap<String, LittlePremadePreview> PREVIEWS = new HashMap<>();

    public static <T extends LittleStructure> LittlePremadeType register(String id, String modid, Class<T> structureClass, BiFunction<LittleStructureType, IStructureParentCollection, T> factory) {
        return register(id, modid, structureClass, factory, new LittleAttributeBuilder());
    }
    
    public static <T extends LittleStructure> LittlePremadeType register(String id, String modid, Class<T> structureClass, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
        LittlePremadeType type = (LittlePremadeType) LittleStructureRegistry.register(new LittlePremadeType(id, structureClass, factory, attribute, modid));
        STRUCTURES.add(type);
        return type;
    }
    
    public static LittlePremadeType register(LittlePremadeType type) {
        STRUCTURES.add((LittlePremadeType) LittleStructureRegistry.register(type));
        return type;
    }

    public static Collection<LittlePremadePreview> previews() {
        return PREVIEWS.values();
    }
    
    public static List<LittlePremadeType> types() {
        return STRUCTURES;
    }

    public static LittlePremadeType get(String id) {
        LittleStructureType type = LittleStructureRegistry.REGISTRY.get(id);
        if (type instanceof LittlePremadeType)
            return (LittlePremadeType) type;
        return null;
    }

    public static ItemStack createStack(String id) {
        return PREVIEWS.get(id).stack.copy();
    }
    
    public static void initStructures() {
        register("structure_builder", LittleTiles.MODID, LittleStructureBuilder.class, LittleStructureBuilder::new);
    }
    
}
