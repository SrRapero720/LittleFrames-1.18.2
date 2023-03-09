package team.creative.littletiles;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETilesRendered;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;

import java.util.function.Supplier;

public class LittleTilesRegistry {
    // BLOCKS
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LittleTiles.MODID);
    
    public static final RegistryObject<Block> BLOCK_TILES = BLOCKS.register("tiles", () -> new BlockTile(Material.STONE, false, false));
    public static final RegistryObject<Block> BLOCK_TILES_TICKING = BLOCKS.register("tiles_ticking", () -> new BlockTile(Material.STONE, true, false));
    public static final RegistryObject<Block> BLOCK_TILES_RENDERED = BLOCKS.register("tiles_rendered", () -> new BlockTile(Material.STONE, false, true));
    public static final RegistryObject<Block> BLOCK_TILES_TICKING_RENDERED = BLOCKS.register("tiles_ticking_rendered", () -> new BlockTile(Material.STONE, true, true));
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        RegistryObject<T> ret = BLOCKS.register(name, sup);
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, LittleTiles.MODID);
    
    public static final RegistryObject<BlockEntityType<BETiles>> BE_TILES_TYPE = registerBlockEntity("tiles", () -> BlockEntityType.Builder
            .of(BETiles::new, BLOCK_TILES.get(), BLOCK_TILES_TICKING.get()));
    public static final RegistryObject<BlockEntityType<BETilesRendered>> BE_TILES_TYPE_RENDERED = registerBlockEntity("tiles_rendered", () -> BlockEntityType.Builder
            .of(BETilesRendered::new, BLOCK_TILES_RENDERED.get(), BLOCK_TILES_TICKING_RENDERED.get()));
    
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType.Builder<T>> sup) {
        return BLOCK_ENTITIES.register(name, () -> sup.get().build(Util.fetchChoiceType(References.BLOCK_ENTITY, name)));
    }
    
    // ENTITIES
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, LittleTiles.MODID);
    
    public static final RegistryObject<EntityType<PrimedSizedTnt>> SIZED_TNT_TYPE = ENTITIES
            .register("primed_size_tnt", () -> EntityType.Builder.<PrimedSizedTnt>of(PrimedSizedTnt::new, MobCategory.MISC).build("primed_size_tnt"));
    public static final RegistryObject<EntityType<EntitySit>> SIT_TYPE = ENTITIES
            .register("sit", () -> EntityType.Builder.<EntitySit>of(EntitySit::new, MobCategory.MISC).build("sit"));
    
    public static final RegistryObject<EntityType<LittleLevelEntity>> ENTITY_LEVEL_LARGE = ENTITIES
            .register("little_level_large", () -> EntityType.Builder.<LittleLevelEntity>of(LittleLevelEntity::new, MobCategory.MISC).build("little_level_large"));
    
    // DIMENSION
    
    public static final ResourceKey<DimensionType> FAKE_DIMENSION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(LittleTiles.MODID, "fake"));
    
}
