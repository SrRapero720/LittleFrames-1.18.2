package team.creative.littletiles.common.action;

import me.srrapero720.creativecore.common.util.mc.PlayerUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.config.LittleTilesConfig.AreaProtected;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToConvertBlockException;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToPlaceColorException;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.packet.update.BlockUpdate;
import team.creative.littletiles.common.packet.update.BlocksUpdate;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class LittleAction<T> extends CreativePacket {

    /**
     * Must be implemented by every action
     **/
    public LittleAction() {

    }

    @OnlyIn(Dist.CLIENT)
    public abstract boolean canBeReverted();

    public abstract T action(Player player) throws LittleActionException;

    public abstract boolean wasSuccessful(T result);

    public abstract T failed();

    @Override
    public final void executeClient(Player player) {
    }

    @Override
    public final void executeServer(ServerPlayer player) {
        try {
            action(player);
        } catch (LittleActionException e) {
            player.sendMessage(new TextComponent(e.getLocalizedMessage()), ChatType.SYSTEM, Util.NIL_UUID);
        }
    }

    public abstract LittleAction mirror(Axis axis, LittleBoxAbsolute box);

    public static boolean canConvertBlock(Player player, Level level, BlockPos pos, BlockState state, int affected) throws LittleActionException {
        if (LittleTiles.CONFIG.build.get(player).limitAffectedBlocks && LittleTiles.CONFIG.build.get(player).maxAffectedBlocks < affected)
            throw new NotAllowedToConvertBlockException(player);
        if (!LittleTiles.CONFIG.build.get(player).editUnbreakable && state.getBlock().defaultDestroyTime() < 0)
            throw new NotAllowedToConvertBlockException(player);
        return LittleTiles.CONFIG.canEditBlock(player, state, pos);
    }

    public static BETiles loadBE(Player player, Level level, BlockPos pos, MutableInt affected, boolean shouldConvert, int attribute) throws LittleActionException {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BETiles)) {
            LittleGroup tiles = null;
            if (blockEntity == null && shouldConvert) {
                BlockState state = level.getBlockState(pos);
                if (isBlockValid(state) && canConvertBlock(player, level, pos, state, affected == null ? 0 : affected.incrementAndGet())) {

                    tiles = new LittleGroup();
                    LittleBox box = new LittleBox(0, 0, 0, tiles.getGrid().count, tiles.getGrid().count, tiles.getGrid().count);
                    tiles.add(tiles.getGrid(), new LittleElement(state, ColorUtils.WHITE), box);
                } else if (state.getMaterial().isReplaceable()) {
                    if (!level.setBlock(pos, BlockTile.getStateByAttribute(attribute), 3))
                        return null;
                    blockEntity = level.getBlockEntity(pos);
                }
            }

            if (tiles != null && !tiles.isEmpty()) {
                level.setBlock(pos, BlockTile.getStateByAttribute(attribute), 3);
                BETiles te = (BETiles) level.getBlockEntity(pos);
                te.convertTo(tiles.getGrid());
                final LittleGroup toAdd = tiles;
                te.updateTilesSecretly((x) -> {
                    for (LittleTile tile : toAdd)
                        x.noneStructureTiles().add(tile);
                });
                te.convertToSmallest();
                blockEntity = te;
            }
        }

        if (blockEntity instanceof BETiles)
            return (BETiles) blockEntity;
        return null;
    }

    public static void fireBlockBreakEvent(Level level, BlockPos pos, Player player) throws AreaProtected {
        if (level.isClientSide)
            return;
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            sendBlockResetToClient(level, player, pos);
            throw new AreaProtected();
        }
    }

    private static Method loadWorldEditEvent() {
        try {
            Class clazz = Class.forName("com.sk89q.worldedit.forge.ForgeWorldEdit");
            worldEditInstance = clazz.getField("inst").get(null);
            return clazz.getMethod("onPlayerInteract", PlayerInteractEvent.class);
        } catch (Exception e) {

        }
        return null;
    }

    private static Method WorldEditEvent = loadWorldEditEvent();
    private static Object worldEditInstance = null;

    public static void sendBlockResetToClient(LevelAccessor level, Player player, BlockPos pos) {
        if (!(player instanceof ServerPlayer))
            return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null)
            sendBlockResetToClient(level, player, be);
        else
            LittleTiles.NETWORK.sendToClient(new BlockUpdate(level, pos, be), (ServerPlayer) player);
    }

    public static void sendBlockResetToClient(LevelAccessor level, Player player, BlockEntity be) {
        if (!(player instanceof ServerPlayer))
            return;
        LittleTiles.NETWORK.sendToClient(new BlockUpdate(level, be.getBlockPos(), be), (ServerPlayer) player);
    }

    public static void sendBlockResetToClient(LevelAccessor level, Player player, Iterable<BETiles> blockEntities) {
        if (!(player instanceof ServerPlayer))
            return;
        LittleTiles.NETWORK.sendToClient(new BlocksUpdate(level, blockEntities), (ServerPlayer) player);
    }

    public static void sendBlockResetToClient(LevelAccessor level, Player player, LittleStructure structure) {
        if (!(player instanceof ServerPlayer))
            return;
        try {
            sendBlockResetToClient(level, player, structure.blocks());
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAllowedToInteract(Player player, LittleEntity entity, boolean rightClick) {
        if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.mayBuild())))
            return false;

        return true;
    }

    public static boolean isAllowedToInteract(LevelAccessor level, Player player, BlockPos pos, boolean rightClick, Facing facing) {
        if (player == null || player.level.isClientSide)
            return true;

        if (player.isSpectator() || (!rightClick && (PlayerUtils.isAdventure(player) || !player.mayBuild())))
            return false;

        if (player.isSpectator())
            return false;

        if (!rightClick && PlayerUtils.isAdventure(player)) {
            ItemStack stack = player.getMainHandItem();
            BlockInWorld blockinworld = new BlockInWorld(level, pos, false);
            if (!stack.hasAdventureModePlaceTagForBlock(level.registryAccess().registryOrThrow(DefaultedRegistry.BLOCK_REGISTRY), blockinworld))
                return false;
        } else if (!rightClick && !player.mayBuild())
            return false;

        if (WorldEditEvent != null) {
            PlayerInteractEvent event = rightClick ? new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, new BlockHitResult(Vec3
                .atBottomCenterOf(pos), facing.toVanilla(), pos, true)) : new PlayerInteractEvent.LeftClickBlock(player, pos, facing.toVanilla());
            try {
                if (worldEditInstance == null)
                    loadWorldEditEvent();
                WorldEditEvent.invoke(worldEditInstance, event);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (event.isCanceled())
                return false;
        }

        return !player.getServer().isUnderSpawnProtection((ServerLevel) player.level, pos, player);
    }

    public static boolean isAllowedToPlacePreview(Player player, LittleTile tile) throws LittleActionException {
        if (tile == null)
            return true;

        if (tile.hasColor() && ColorUtils.alpha(tile.color) < LittleTiles.CONFIG.getMinimumTransparency(player))
            throw new NotAllowedToPlaceColorException(player);

        return true;
    }

    public static boolean needIngredients(Player player) {
        return !player.isCreative();
    }

    public static LittleIngredients getIngredients(IParentCollection parent, LittleTile tile) {
        return LittleIngredient.extract(tile, tile.getPercentVolume(parent.getGrid()));
    }

    public static LittleIngredients getIngredients(IParentCollection parent, List<LittleTile> tiles) {
        LittleIngredients ingredients = new LittleIngredients();
        for (LittleTile tile : tiles)
            ingredients.add(LittleIngredient.extract(tile, tile.getPercentVolume(parent.getGrid())));
        return ingredients;
    }

    public static LittleIngredients getIngredients(LittleGroup previews) {
        return LittleIngredient.extract(previews);
    }

    public static LittleIngredients getIngredients(LittleGroupAbsolute previews) {
        return LittleIngredient.extract(previews.group);
    }

    public static LittleIngredients getIngredients(LittleTile tile, double volume) {
        return LittleIngredient.extract(tile, volume);
    }

    public static boolean checkAndTake(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (needIngredients(player)) {
            try {
                inventory.startSimulation();
                inventory.take(ingredients.copy());
            } finally {
                inventory.stopSimulation();
            }
            inventory.take(ingredients.copy());
        }
        return true;
    }

    public static boolean take(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (needIngredients(player))
            inventory.take(ingredients.copy());
        return true;
    }

    public static boolean checkAndGive(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (needIngredients(player)) {
            try {
                inventory.startSimulation();
                inventory.give(ingredients.copy());
            } finally {
                inventory.stopSimulation();
            }
            inventory.give(ingredients.copy());
        }
        return true;
    }

    public static boolean give(Player player, LittleInventory inventory, LittleIngredients ingredients) throws NotEnoughIngredientsException {
        if (needIngredients(player))
            inventory.give(ingredients.copy());
        return true;
    }

    public static boolean giveOrDrop(Player player, LittleInventory inventory, ParentCollection parent, List<LittleTile> tiles) {
        if (needIngredients(player) && !tiles.isEmpty()) {
            try {
                checkAndGive(player, inventory, getIngredients(parent, tiles));
            } catch (NotEnoughIngredientsException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static List<ItemStack> getInventories(Player player) {
        List<ItemStack> inventories = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ILittleIngredientInventory)
                inventories.add(stack);
        }
        return inventories;
    }

    private static boolean isBlockValid(Block block) {
        if (block instanceof EntityBlock || block instanceof SlabBlock)
            return false;
        return block instanceof GlassBlock || block instanceof StainedGlassBlock || block instanceof HalfTransparentBlock;
    }

    public static boolean isBlockValid(BlockState state) {
        if (isBlockValid(state.getBlock())) return true;
        return state.isSolidRender(null, null);
    }

}
