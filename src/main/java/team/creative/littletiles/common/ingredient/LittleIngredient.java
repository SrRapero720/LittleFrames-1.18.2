package team.creative.littletiles.common.ingredient;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException.NotEnoughSpaceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class LittleIngredient<T extends LittleIngredient> extends LittleIngredientBase<T> {
    
    private static HashMap<Class<? extends LittleIngredient>, Integer> typesInv = new HashMap<>();
    private static List<Class<? extends LittleIngredient>> types = new ArrayList<>();
    private static List<IngredientOverflowHandler> overflowHandlers = new ArrayList<>();
    private static List<IngredientConvertionHandler> converationHandlers = new ArrayList<>();
    
    public static int indexOf(LittleIngredient ingredient) {
        return indexOf(ingredient.getClass());
    }
    
    public static int indexOf(Class<? extends LittleIngredient> type) {
        return typesInv.get(type);
    }
    
    public static List<ItemStack> handleOverflow(LittleIngredient ingredient) throws NotEnoughSpaceException {
        return overflowHandlers.get(indexOf(ingredient)).handleOverflow(ingredient);
    }
    
    public static int typeCount() {
        return types.size();
    }
    
    static void extract(LittleIngredients ingredients, LittleGroup group, boolean onlyStructure) {
        if (!onlyStructure)
            for (IngredientConvertionHandler handler : converationHandlers)
                ingredients.add(handler.extract(group));
            
        if (group.hasStructure())
            group.getStructureType().addIngredients(group, ingredients);
        
        if (group.hasChildren())
            for (LittleGroup child : group.children.all())
                extract(ingredients, child, onlyStructure);
    }
    
    public static LittleIngredients extract(LittleElement tile, double volume) {
        LittleIngredients ingredients = new LittleIngredients();
        for (IngredientConvertionHandler handler : converationHandlers)
            ingredients.add(handler.extract(tile, volume));
        return ingredients;
    }
    
    public static LittleIngredients extract(LittleGroup group) {
        LittleIngredients ingredients = new LittleIngredients();
        extract(ingredients, group, false);
        return ingredients;
    }

    public static <T extends LittleIngredient> void registerType(Class<T> type, IngredientOverflowHandler<T> overflowHandler, IngredientConvertionHandler<T> converationHandler) {
        if (typesInv.containsKey(type))
            throw new RuntimeException("Duplicate found! " + types);
        
        typesInv.put(type, types.size());
        types.add(type);
        
        overflowHandlers.add(overflowHandler);
        converationHandlers.add(converationHandler);
    }
    
    static {
        registerType(BlockIngredient.class, new IngredientOverflowHandler<BlockIngredient>() {
            
            @Override
            public List<ItemStack> handleOverflow(BlockIngredient overflow) throws NotEnoughSpaceException {
                List<ItemStack> stacks = new ArrayList<>();
                for (BlockIngredientEntry entry : overflow) {
                    double volume = entry.value;
                    if (volume >= 1) {
                        ItemStack stack = entry.getBlockStack();
                        stack.setCount((int) volume);
                        volume -= stack.getCount();
                        stacks.add(stack);
                    }

//                    if (volume > 0) {
//                        ItemStack stack = new ItemStack(LittleTilesRegistry.BLOCK_INGREDIENT.get());
//                        stack.setTag(new CompoundTag());
//                        ItemBlockIngredient.saveIngredient(stack, entry);
//                        stacks.add(stack);
//                    }
                }
                return stacks;
            }
        }, new IngredientConvertionHandler<>() {
            
            @Override
            public BlockIngredient extract(ItemStack stack) {
                Block block = Block.byItem(stack.getItem());
                if (block != null && !(block instanceof AirBlock) && LittleAction.isBlockValid(block.defaultBlockState())) {
                    BlockIngredient ingredient = new BlockIngredient();
                    ingredient.add(IngredientUtils.getBlockIngredient(LittleBlockRegistry.get(block), 1));
                    return ingredient;
                }
                return null;
            }
            
            @Override
            public BlockIngredient extract(LittleGroup previews) {
                BlockIngredient ingredient = new BlockIngredient();
                if (previews.containsIngredients())
                    for (LittleTile preview : previews) {}
                    
                if (ingredient.isEmpty())
                    return null;
                return ingredient;
            }
            
            @Override
            public BlockIngredient extract(LittleElement tile, double volume) {
                BlockIngredient ingredient = new BlockIngredient();
                ingredient.add(IngredientUtils.getBlockIngredient(tile.getBlock(), volume));
                return ingredient;
            }
            
        });
        registerType(ColorIngredient.class, new IngredientOverflowHandler<ColorIngredient>() {
            
            @Override
            public List<ItemStack> handleOverflow(ColorIngredient overflow) throws NotEnoughSpaceException {
                return new ArrayList<>();
            }
        }, new IngredientConvertionHandler<ColorIngredient>() {
            
            @Override
            public ColorIngredient extract(ItemStack stack) {
                if (stack.getItem() instanceof DyeItem) {
                    DyeColor dyeColor = ((DyeItem) stack.getItem()).getDyeColor();
                    float[] rgb = dyeColor.getTextureDiffuseColors();
                    ColorIngredient color = ColorIngredient.getColors(ColorUtils.rgb(rgb[0], rgb[1], rgb[2]));
                    color.scale(LittleTiles.CONFIG.general.dyeVolume);
                    return color;
                }
                return null;
            }
            
            @Override
            public ColorIngredient extract(LittleGroup previews) {
                ColorIngredient ingredient = new ColorIngredient();
                if (previews.containsIngredients())
                    for (LittleTile preview : previews)
                        ingredient.add(ColorIngredient.getColors(previews.getGrid(), preview));
                    
                if (ingredient.isEmpty())
                    return null;
                return ingredient;
            }
            
            @Override
            public ColorIngredient extract(LittleElement tile, double volume) {
                ColorIngredient ingredient = new ColorIngredient();
                ingredient.add(ColorIngredient.getColors(tile, volume));
                return ingredient;
            }
            
        });
        registerType(StackIngredient.class, new IngredientOverflowHandler<StackIngredient>() {
            
            @Override
            public List<ItemStack> handleOverflow(StackIngredient overflow) throws NotEnoughSpaceException {
                List<ItemStack> stacks = new ArrayList<ItemStack>();
                for (StackIngredientEntry entry : overflow) {
                    ItemStack stack = entry.stack.copy();
                    stack.setCount(entry.count);
                    stacks.add(stack);
                }
                return stacks;
            }
        }, new IngredientConvertionHandler<StackIngredient>() {
            
            @Override
            public StackIngredient extract(LittleElement tile, double volume) {
                return null;
            }
            
            @Override
            public StackIngredient extract(LittleGroup previews) {
                return null;
            }
            
            @Override
            public StackIngredient extract(ItemStack stack) {
                return null;
            }
            
            @Override
            public boolean requiresExtraHandler() {
                return true;
            }
            
            @Override
            public boolean handleExtra(StackIngredient ingredient, ItemStack stack, LittleIngredients overflow) {
                StackIngredient stackIngredients = new StackIngredient();
                stackIngredients.add(new StackIngredientEntry(stack, 1));
                int amount = ingredient.getMinimumCount(stackIngredients, stack.getCount());
                if (amount > -1) {
                    stackIngredients.scale(amount);
                    overflow.add(ingredient.sub(stackIngredients));
                    stack.shrink(amount);
                    
                    if (ingredient.isEmpty())
                        return true;
                }
                
                return false;
            }
            
        });
        
        registerType(ItemIngredient.class, new IngredientOverflowHandler<ItemIngredient>() {
            
            @Override
            public List<ItemStack> handleOverflow(ItemIngredient overflow) throws NotEnoughSpaceException {
                throw new NotEnoughSpaceException(overflow);
            }
        }, new IngredientConvertionHandler<ItemIngredient>() {
            
            @Override
            public ItemIngredient extract(ItemStack stack) {
                return null;
            }
            
            @Override
            public ItemIngredient extract(LittleGroup group) {
                return null;
            }
            
            @Override
            public ItemIngredient extract(LittleElement tile, double volume) {
                return null;
            }
            
            @Override
            public boolean requiresExtraHandler() {
                return true;
            }
            
            @Override
            public boolean handleExtra(ItemIngredient ingredient, ItemStack stack, LittleIngredients overflow) {
                for (Iterator<ItemIngredientEntry> itr = ingredient.iterator(); itr.hasNext();) {
                    ItemIngredientEntry entry = itr.next();
                    if (entry.is(stack)) {
                        int count = Math.min(entry.count, stack.getCount());
                        stack.shrink(count);
                        entry.count -= count;
                        if (entry.isEmpty())
                            itr.remove();
                        
                        if (ingredient.isEmpty())
                            return true;
                    }
                }
                return false;
            }
            
        });
    }
    
    @Override
    public abstract T copy();
    
    public abstract TextBuilder toText();
    
    @Override
    public abstract T add(T ingredient);
    
    @Override
    public abstract T sub(T ingredient);
    
    public abstract void scale(int count);
    
    public abstract void scaleAdvanced(double scale);
    
    public abstract int getMinimumCount(T other, int availableCount);
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return this.getClass() == obj.getClass();
    }
    
    public abstract void print(TextBuilder text);
    
    public static abstract class IngredientOverflowHandler<T extends LittleIngredient> {
        
        public abstract List<ItemStack> handleOverflow(T overflow) throws NotEnoughSpaceException;
        
    }
    
    public static abstract class IngredientConvertionHandler<T extends LittleIngredient> {
        
        public abstract T extract(ItemStack stack);
        
        public abstract T extract(LittleGroup group);
        
        public abstract T extract(LittleElement tile, double volume);
        
        public boolean requiresExtraHandler() {
            return false;
        }
        
        public boolean handleExtra(T ingredient, ItemStack stack, LittleIngredients overflow) {
            return false;
        }
        
    }
    
}
