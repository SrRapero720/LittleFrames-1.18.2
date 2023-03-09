package team.creative.littletiles.server.level.little;

import me.srrapero720.waterframes.backport.packets.SrvPlayerActionPacket;
import me.srrapero720.waterframes.backport.packets.SrvboundUseItemPacket;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.packet.level.LittleLevelPacket;

import javax.annotation.Nullable;
import java.util.Objects;

public class LittleServerPlayerHandler implements ServerPlayerConnection, PacketListener, ServerGamePacketListener {

    private static final Logger LOGGER = LittleTiles.LOGGER;
    private final MinecraftServer server;
    public final ServerPlayer player;

    protected LittleServerLevel level;

    private boolean isDestroyingBlock;
    private int destroyProgressStart;
    private BlockPos destroyPos = BlockPos.ZERO;
    private int gameTicks;
    private boolean hasDelayedDestroy;
    private BlockPos delayedDestroyPos = BlockPos.ZERO;
    private int delayedTickStart;
    private int lastSentState = -1;
    private int ackBlockChangesUpTo = -1;

    public LittleServerPlayerHandler(LittleServerLevel level, ServerPlayer player) {
        this.server = player.getServer();
        this.level = level;
        this.player = player;
    }

    @Override
    public @NotNull ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public @NotNull Connection getConnection() {
        return player.connection.getConnection();
    }

    public ServerGamePacketListenerImpl getVanilla() {
        return player.connection;
    }

    @Override
    public void handlePlayerInput(@NotNull ServerboundPlayerInputPacket packet) {
        getVanilla().handlePlayerInput(packet);
    }

    @Override
    public void handleMoveVehicle(@NotNull ServerboundMoveVehiclePacket packet) {
        getVanilla().handleMoveVehicle(packet);
    }

    @Override
    public void handleAcceptTeleportPacket(@NotNull ServerboundAcceptTeleportationPacket packet) {
        getVanilla().handleAcceptTeleportPacket(packet);
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(@NotNull ServerboundRecipeBookSeenRecipePacket packet) {
        getVanilla().handleRecipeBookSeenRecipePacket(packet);
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(@NotNull ServerboundRecipeBookChangeSettingsPacket packet) {
        getVanilla().handleRecipeBookChangeSettingsPacket(packet);
    }

    @Override
    public void handleSeenAdvancements(@NotNull ServerboundSeenAdvancementsPacket packet) {
        getVanilla().handleSeenAdvancements(packet);
    }

    @Override
    public void handleCustomCommandSuggestions(@NotNull ServerboundCommandSuggestionPacket packet) {
        getVanilla().handleCustomCommandSuggestions(packet);
    }

    @Override
    public void handleSetCommandBlock(@NotNull ServerboundSetCommandBlockPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (!this.server.isCommandBlockEnabled())
            this.player.sendMessage(new TranslatableComponent("advMode.notEnabled"), ChatType.SYSTEM, Util.NIL_UUID);
        else if (!this.player.canUseGameMasterBlocks())
            this.player.sendMessage(new TranslatableComponent("advMode.notAllowed"), ChatType.SYSTEM, Util.NIL_UUID);
        else {
            BaseCommandBlock basecommandblock = null;
            CommandBlockEntity commandblockentity = null;
            BlockPos blockpos = packet.getPos();
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof CommandBlockEntity) {
                commandblockentity = (CommandBlockEntity) blockentity;
                basecommandblock = commandblockentity.getCommandBlock();
            }

            String s = packet.getCommand();
            boolean flag = packet.isTrackOutput();
            if (basecommandblock != null) {
                CommandBlockEntity.Mode commandblockentity$mode = commandblockentity.getMode();
                BlockState blockstate = level.getBlockState(blockpos);
                Direction direction = blockstate.getValue(CommandBlock.FACING);
                BlockState blockstate1;
                blockstate1 = switch (packet.getMode()) {
                    case SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                    case AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
                    default -> Blocks.COMMAND_BLOCK.defaultBlockState();
                };

                BlockState blockstate2 = blockstate1.setValue(CommandBlock.FACING, direction).setValue(CommandBlock.CONDITIONAL, packet.isConditional());
                if (blockstate2 != blockstate) {
                    level.setBlock(blockpos, blockstate2, 2);
                    blockentity.setBlockState(blockstate2);
                    level.getChunkAt(blockpos).setBlockEntity(blockentity);
                }

                basecommandblock.setCommand(s);
                basecommandblock.setTrackOutput(flag);
                if (!flag)
                    basecommandblock.setLastOutput((Component) null);

                commandblockentity.setAutomatic(packet.isAutomatic());
                if (commandblockentity$mode != packet.getMode())
                    commandblockentity.onModeSwitch();

                basecommandblock.onUpdated();
                if (!StringUtil.isNullOrEmpty(s))
                    this.player.sendMessage(new TranslatableComponent("advMode.setCommand.success"), ChatType.SYSTEM, Util.NIL_UUID);
            }

        }
    }

    @Override
    public void handleSetCommandMinecart(@NotNull ServerboundSetCommandMinecartPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (!this.server.isCommandBlockEnabled())
            this.player.sendMessage(new TranslatableComponent("advMode.notEnabled"), ChatType.SYSTEM, Util.NIL_UUID);
        else if (!this.player.canUseGameMasterBlocks())
            this.player.sendMessage(new TranslatableComponent("advMode.notAllowed"), ChatType.SYSTEM, Util.NIL_UUID);
        else {
            BaseCommandBlock basecommandblock = packet.getCommandBlock(level);
            if (basecommandblock != null) {
                basecommandblock.setCommand(packet.getCommand());
                basecommandblock.setTrackOutput(packet.isTrackOutput());
                if (!packet.isTrackOutput())
                    basecommandblock.setLastOutput((Component) null);

                basecommandblock.onUpdated();
                this.player.sendMessage(new TranslatableComponent("advMode.setCommand.success"), ChatType.SYSTEM, Util.NIL_UUID);
            }

        }
    }

    @Override
    public void handlePickItem(@NotNull ServerboundPickItemPacket packet) {
        getVanilla().handlePickItem(packet);
    }

    @Override
    public void handleRenameItem(@NotNull ServerboundRenameItemPacket packet) {
        getVanilla().handleRenameItem(packet);
    }

    @Override
    public void handleSetBeaconPacket(@NotNull ServerboundSetBeaconPacket packet) {
        getVanilla().handleSetBeaconPacket(packet);
    }

    @Override
    public void handleSetStructureBlock(@NotNull ServerboundSetStructureBlockPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = packet.getPos();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof StructureBlockEntity structure) {
                structure.setMode(packet.getMode());
                structure.setStructureName(packet.getName());
                structure.setStructurePos(packet.getOffset());
                structure.setStructureSize(packet.getSize());
                structure.setMirror(packet.getMirror());
                structure.setRotation(packet.getRotation());
                structure.setMetaData(packet.getData());
                structure.setIgnoreEntities(packet.isIgnoreEntities());
                structure.setShowAir(packet.isShowAir());
                structure.setShowBoundingBox(packet.isShowBoundingBox());
                structure.setIntegrity(packet.getIntegrity());
                structure.setSeed(packet.getSeed());
                if (structure.hasStructureName()) {
                    String s = structure.getStructureName();
                    if (packet.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA)
                        if (structure.saveStructure())

                            this.player.displayClientMessage(new TranslatableComponent("structure_block.save_success", s), false);
                        else
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.save_failure", s), false);
                    else if (packet.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA)
                        if (!structure.isStructureLoadable())
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.load_not_found", s), false);
                        else if (structure.loadStructure(level))
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.load_success", s), false);
                        else
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.load_prepare", s), false);
                    else if (packet.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA)
                        if (structure.detectSize())
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.size_success", s), false);
                        else
                            this.player.displayClientMessage(new TranslatableComponent("structure_block.size_failure"), false);
                } else
                    this.player.displayClientMessage(new TranslatableComponent("structure_block.invalid_structure_name", packet.getName()), false);

                structure.setChanged();
                level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            }

        }
    }

    @Override
    public void handleSetJigsawBlock(@NotNull ServerboundSetJigsawBlockPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (this.player.canUseGameMasterBlocks()) {
            BlockPos blockpos = packet.getPos();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof JigsawBlockEntity jigsaw) {
                jigsaw.setName(packet.getName());
                jigsaw.setTarget(packet.getTarget());
                jigsaw.setPool(packet.getPool());
                jigsaw.setFinalState(packet.getFinalState());
                jigsaw.setJoint(packet.getJoint());
                jigsaw.setChanged();
                level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
            }

        }
    }

    @Override
    public void handleJigsawGenerate(@NotNull ServerboundJigsawGeneratePacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (this.player.canUseGameMasterBlocks()) {
            BlockEntity blockentity = level.getBlockEntity(packet.getPos());
            if (blockentity instanceof JigsawBlockEntity jigsawblockentity)
                jigsawblockentity.generate(level, packet.levels(), packet.keepJigsaws());
        }
    }

    @Override
    public void handleSelectTrade(@NotNull ServerboundSelectTradePacket packet) {
        getVanilla().handleSelectTrade(packet);
    }

    @Override
    public void handleEditBook(@NotNull ServerboundEditBookPacket packet) {
        getVanilla().handleEditBook(packet);
    }

    @Override
    public void handleEntityTagQuery(@NotNull ServerboundEntityTagQuery packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (this.player.hasPermissions(2)) {
            Entity entity = level.getEntity(packet.getEntityId());
            if (entity != null)
                send(new ClientboundTagQueryPacket(packet.getTransactionId(), entity.saveWithoutId(new CompoundTag())));
        }
    }

    @Override
    public void handleBlockEntityTagQuery(@NotNull ServerboundBlockEntityTagQuery packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        if (this.player.hasPermissions(2)) {
            BlockEntity blockentity = level.getBlockEntity(packet.getPos());
            CompoundTag compoundtag = blockentity != null ? blockentity.saveWithoutMetadata() : null;
            send(new ClientboundTagQueryPacket(packet.getTransactionId(), compoundtag));
        }
    }

    @Override
    public void handleMovePlayer(@NotNull ServerboundMovePlayerPacket packet) {
        getVanilla().handleMovePlayer(packet);
    }

    @Override
    public void handlePlayerAction(@NotNull ServerboundPlayerActionPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        BlockPos blockpos = packet.getPos();
        this.player.resetLastActionTime();
        switch (packet.getAction()) {
            case SWAP_ITEM_WITH_OFFHAND:
                if (!this.player.isSpectator()) {
                    ItemStack itemstack = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, itemstack);
                    this.player.stopUsingItem();
                }

                return;
            case DROP_ITEM:
                if (!this.player.isSpectator())
                    this.player.drop(false);

                return;
            case DROP_ALL_ITEMS:
                if (!this.player.isSpectator())
                    this.player.drop(true);

                return;
            case RELEASE_USE_ITEM:
                this.player.releaseUsingItem();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                handleBlockBreakAction(blockpos, packet.getAction(), packet.getDirection(), level.getMaxBuildHeight(), ((SrvPlayerActionPacket) packet).getSequence());
                ackBlockChangesUpTo(((SrvPlayerActionPacket) packet).getSequence());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    @Override
    public void handleUseItem(@NotNull ServerboundUseItemPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, level);
        this.ackBlockChangesUpTo(((SrvboundUseItemPacket) packet).getSequence());
        InteractionHand interactionhand = packet.getHand();
        ItemStack itemstack = this.player.getItemInHand(interactionhand);
        this.player.resetLastActionTime();
        if (!itemstack.isEmpty())
            if (useItem(this.player, itemstack, interactionhand).shouldSwing())
                this.player.swing(interactionhand, true);
    }

    @Override
    public void handleTeleportToEntityPacket(@NotNull ServerboundTeleportToEntityPacket packet) {
        getVanilla().handleTeleportToEntityPacket(packet);
    }

    @Override
    public void handleResourcePackResponse(@NotNull ServerboundResourcePackPacket packet) {
        getVanilla().handleResourcePackResponse(packet);
    }

    @Override
    public void handlePaddleBoat(@NotNull ServerboundPaddleBoatPacket packet) {
        getVanilla().handlePaddleBoat(packet);
    }

    @Override
    public void handlePong(@NotNull ServerboundPongPacket packet) {
        getVanilla().handlePong(packet);
    }

    @Override
    public void onDisconnect(@NotNull Component component) {
        getVanilla().onDisconnect(component);
    }

    public void ackBlockChangesUpTo(int sequence) {
        if (sequence < 0)
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        this.ackBlockChangesUpTo = Math.max(sequence, this.ackBlockChangesUpTo);
    }

    @Override
    public void send(@NotNull Packet<?> packet) {
        this.send(packet, (ServerGamePacketListener) packet);
    }

    public void send(Packet<?> packet, @Nullable PacketListener listener) {
        try {
            LittleTiles.NETWORK.sendToClient(new LittleLevelPacket(level, packet), player);
//            TODO: remove if breaks things. idk why but onSuccess on packet no exists in 1.19.2 (maybe 1.19.3 feature)
            if (listener != null) listener.getConnection().send(packet);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Sending packet");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Packet being sent");
            crashreportcategory.setDetail("Packet class", () -> packet.getClass().getCanonicalName());
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public void handleSetCarriedItem(@NotNull ServerboundSetCarriedItemPacket packet) {
        getVanilla().handleSetCarriedItem(packet);
    }

    @Override
    public void handleChat(@NotNull ServerboundChatPacket packet) {
        getVanilla().handleChat(packet);
    }

    @Override
    public void handleAnimate(@NotNull ServerboundSwingPacket packet) {
        getVanilla().handleAnimate(packet);
    }

    @Override
    public void handlePlayerCommand(@NotNull ServerboundPlayerCommandPacket packet) {
        getVanilla().handlePlayerCommand(packet);
    }

    @Override
    public void handleInteract(@NotNull ServerboundInteractPacket packet) {
        getVanilla().handleInteract(packet);
    }

    @Override
    public void handleClientCommand(@NotNull ServerboundClientCommandPacket packet) {
        getVanilla().handleClientCommand(packet);
    }

    @Override
    public void handleContainerClose(@NotNull ServerboundContainerClosePacket packet) {
        getVanilla().handleContainerClose(packet);
    }

    @Override
    public void handleContainerClick(@NotNull ServerboundContainerClickPacket packet) {
        getVanilla().handleContainerClick(packet);
    }

    @Override
    public void handlePlaceRecipe(@NotNull ServerboundPlaceRecipePacket packet) {
        getVanilla().handlePlaceRecipe(packet);
    }

    @Override
    public void handleContainerButtonClick(@NotNull ServerboundContainerButtonClickPacket packet) {
        getVanilla().handleContainerButtonClick(packet);
    }

    @Override
    public void handleSetCreativeModeSlot(@NotNull ServerboundSetCreativeModeSlotPacket packet) {
        getVanilla().handleSetCreativeModeSlot(packet);
    }

    @Override
    public void handleSignUpdate(@NotNull ServerboundSignUpdatePacket packet) {
//        List<String> list = Stream.of(packet.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
//        ((ServerGamePacketListenerImplAccessor) getVanilla()).callFilterTextPacket(list).thenAcceptAsync((lines) -> this.updateSignText(packet, lines), this.server);
    }

    @Override
    public void handleUseItemOn(@NotNull ServerboundUseItemOnPacket p_133783_) {

    }

    @Override
    public void handleKeepAlive(@NotNull ServerboundKeepAlivePacket packet) {
        getVanilla().handleKeepAlive(packet);
    }

    @Override
    public void handlePlayerAbilities(@NotNull ServerboundPlayerAbilitiesPacket packet) {
        getVanilla().handlePlayerAbilities(packet);
    }

    @Override
    public void handleClientInformation(@NotNull ServerboundClientInformationPacket packet) {
        getVanilla().handleClientInformation(packet);
    }

    @Override
    public void handleCustomPayload(@NotNull ServerboundCustomPayloadPacket packet) {
        getVanilla().handleCustomPayload(packet); // not sure if it makes sense, but for now there is nothing else to do here
    }

    @Override
    public void handleChangeDifficulty(@NotNull ServerboundChangeDifficultyPacket packet) {
        getVanilla().handleChangeDifficulty(packet);
    }

    @Override
    public void handleLockDifficulty(@NotNull ServerboundLockDifficultyPacket packet) {
        getVanilla().handleLockDifficulty(packet);
    }


//    TODO: MISSING?
//    IF CAUSE TICKING PROBLEMS, THEN BACKPORT MINECRAFT PACKETS HERE
    public void tick() {
//        if (this.ackBlockChangesUpTo > -1) {
//            this.send(new ClientboundSectionBlocksUpdatePacket(this.));
//            this.ackBlockChangesUpTo = -1;
//        }
//
//        ++this.gameTicks;
//        if (this.hasDelayedDestroy) {
//            BlockState blockstate = this.level.getBlockState(this.delayedDestroyPos);
//            if (blockstate.isAir())
//                this.hasDelayedDestroy = false;
//            else {
//                float f = this.incrementDestroyProgress(blockstate, this.delayedDestroyPos, this.delayedTickStart);
//                if (f >= 1.0F) {
//                    this.hasDelayedDestroy = false;
//                    this.destroyBlock(this.delayedDestroyPos);
//                }
//            }
//        } else if (this.isDestroyingBlock) {
//            BlockState blockstate1 = this.level.getBlockState(this.destroyPos);
//            if (blockstate1.isAir()) {
//                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
//                this.lastSentState = -1;
//                this.isDestroyingBlock = false;
//            } else
//                this.incrementDestroyProgress(blockstate1, this.destroyPos, this.destroyProgressStart);
//        }

    }

    private void debugLogging(BlockPos pos, boolean p_215127_, int sequence, String message) {
    }

    public boolean isCreative() {
        return player.isCreative();
    }

    public GameType getGameMode() {
        return this.player.gameMode.getGameModeForPlayer();
    }

    public void handleBlockBreakAction(BlockPos pos, SrvPlayerActionPacket.Action action, Direction direction, int buildHeight, int sequence) {
        PlayerInteractEvent.LeftClickBlock event = ForgeHooks.onLeftClickBlock(player, pos, direction);
        if (event.isCanceled() || (!this.isCreative() && event.getResult() == Event.Result.DENY))
            return;

        if (!this.player.canInteractWith(pos, 1))
            this.debugLogging(pos, false, sequence, "too far");
        else if (pos.getY() >= buildHeight) {
            send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
            this.debugLogging(pos, false, sequence, "too high");
        } else {
            if (action == SrvPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (!this.level.mayInteract(this.player, pos)) {
                    send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
                    this.debugLogging(pos, false, sequence, "may not interact");
                    return;
                }

                if (this.isCreative()) {
                    this.destroyAndAck(pos, sequence, "creative destroy");
                    return;
                }

                if (this.player.blockActionRestricted(this.level, pos, getGameMode())) {
                    send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
                    this.debugLogging(pos, false, sequence, "block action restricted");
                    return;
                }

                this.destroyProgressStart = this.gameTicks;
                float f = 1.0F;
                BlockState blockstate = this.level.getBlockState(pos);
                if (!blockstate.isAir()) {
                    if (event.getUseBlock() != Event.Result.DENY)
                        blockstate.attack(this.level, pos, this.player);
                    f = blockstate.getDestroyProgress(this.player, level, pos);
                }

                if (!blockstate.isAir() && f >= 1.0F) {
                    this.destroyAndAck(pos, sequence, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                        this.debugLogging(pos, false, sequence, "abort destroying since another started (client insta mine, server disagreed)");
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = pos.immutable();
                    int i = (int) (f * 10.0F);
                    this.level.destroyBlockProgress(this.player.getId(), pos, i);
                    this.debugLogging(pos, true, sequence, "actual start of destroying");
                    this.lastSentState = i;
                }
            } else if (action == SrvPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (pos.equals(this.destroyPos)) {
                    int j = this.gameTicks - this.destroyProgressStart;
                    BlockState blockstate1 = this.level.getBlockState(pos);
                    if (!blockstate1.isAir()) {
                        float f1 = blockstate1.getDestroyProgress(this.player, level, pos) * (j + 1);
                        if (f1 >= 0.7F) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                            this.destroyAndAck(pos, sequence, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = pos;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.debugLogging(pos, true, sequence, "stopped destroying");
            } else if (action == SrvPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false;
                if (!Objects.equals(this.destroyPos, pos)) {
                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, pos);
                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                    this.debugLogging(pos, true, sequence, "aborted mismatched destroying");
                }

                this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                this.debugLogging(pos, true, sequence, "aborted destroying");
            }

        }
    }

    public void destroyAndAck(BlockPos pos, int sequence, String message) {
        if (this.destroyBlock(pos))
            this.debugLogging(pos, true, sequence, message);
        else {
            send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
            this.debugLogging(pos, false, sequence, message);
        }

    }

    public boolean destroyBlock(BlockPos pos) {
        BlockState blockstate = this.level.getBlockState(pos);
        int exp = onBlockBreakEvent(level, getGameMode(), player, pos);
        if (exp == -1)
            return false;

        BlockEntity blockentity = this.level.getBlockEntity(pos);
        Block block = blockstate.getBlock();
        if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
            this.level.sendBlockUpdated(pos, blockstate, blockstate, 3);
            return false;
        }

        if (player.getMainHandItem().onBlockStartBreak(pos, player))
            return false;

        if (this.player.blockActionRestricted(this.level, pos, getGameMode()))
            return false;

        if (this.isCreative()) {
            removeBlock(pos, false);
            return true;
        }

        ItemStack itemstack = this.player.getMainHandItem();
        ItemStack itemstack1 = itemstack.copy();
        boolean flag1 = blockstate.canHarvestBlock(this.level, pos, this.player); // previously player.hasCorrectToolForDrops(blockstate)
        itemstack.mineBlock(this.level, blockstate, pos, this.player);
        if (itemstack.isEmpty() && !itemstack1.isEmpty())
            ForgeEventFactory.onPlayerDestroyItem(this.player, itemstack1, InteractionHand.MAIN_HAND);
        boolean flag = removeBlock(pos, flag1);

        if (flag && flag1)
            block.playerDestroy(this.level, this.player, pos, blockstate, blockentity, itemstack1);

        if (flag && exp > 0)
            blockstate.getBlock().popExperience(level, pos, exp);

        return true;
    }

    private boolean removeBlock(BlockPos pos, boolean canHarvest) {
        BlockState state = this.level.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(this.level, pos, this.player, canHarvest, this.level.getFluidState(pos));
        if (removed)
            state.getBlock().destroy(this.level, pos, state);
        return removed;
    }

    public int onBlockBreakEvent(Level level, GameType gameType, ServerPlayer entityPlayer, BlockPos pos) {
        boolean preCancelEvent = false;
        ItemStack itemstack = entityPlayer.getMainHandItem();
        if (!itemstack.isEmpty() && !itemstack.getItem().canAttackBlock(level.getBlockState(pos), level, pos, entityPlayer))
            preCancelEvent = true;

        if (gameType.isBlockPlacingRestricted()) {
            if (gameType == GameType.SPECTATOR)
                preCancelEvent = true;

            if (!entityPlayer.mayBuild() && itemstack.isEmpty() || !itemstack
                    .hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(DefaultedRegistry.BLOCK_REGISTRY), new BlockInWorld(level, pos, false)))
                preCancelEvent = true;
        }

        // Tell client the block is gone immediately then process events
        if (level.getBlockEntity(pos) == null)
            send(new ClientboundBlockUpdatePacket(pos, level.getFluidState(pos).createLegacyBlock()));

        // Post the block break event
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, entityPlayer);
        event.setCanceled(preCancelEvent);
        MinecraftForge.EVENT_BUS.post(event);

        // Handle if the event is canceled
        if (event.isCanceled()) {
            // Let the client know the block still exists
            send(new ClientboundBlockUpdatePacket(level, pos));

            // Update any tile entity data for this block
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                Packet<?> pkt = blockEntity.getUpdatePacket();
                if (pkt != null)
                    send(pkt);
            }
        }
        return event.isCanceled() ? -1 : event.getExpToDrop();
    }

    public InteractionResult useItem(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (this.getGameMode() == GameType.SPECTATOR)
            return InteractionResult.PASS;
        if (player.getCooldowns().isOnCooldown(stack.getItem()))
            return InteractionResult.PASS;

        InteractionResult cancelResult = ForgeHooks.onItemRightClick(player, hand);
        if (cancelResult != null)
            return cancelResult;

        int i = stack.getCount();
        int j = stack.getDamageValue();
        InteractionResultHolder<ItemStack> result = stack.use(level, player, hand);
        ItemStack itemstack = result.getObject();
        if (itemstack == stack && itemstack.getCount() == i && itemstack.getUseDuration() <= 0 && itemstack.getDamageValue() == j)
            return result.getResult();

        if (result.getResult() == InteractionResult.FAIL && itemstack.getUseDuration() > 0 && !player.isUsingItem())
            return result.getResult();

        if (stack != itemstack)
            player.setItemInHand(hand, itemstack);

        if (this.isCreative()) {
            itemstack.setCount(i);
            if (itemstack.isDamageableItem() && itemstack.getDamageValue() != j)
                itemstack.setDamageValue(j);
        }

        if (itemstack.isEmpty())
            player.setItemInHand(hand, ItemStack.EMPTY);

        if (!player.isUsingItem())
            player.inventoryMenu.sendAllDataToRemote();

        return result.getResult();
    }

//    public InteractionResult useItemOn(ServerPlayer player, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
//        BlockPos blockpos = hit.getBlockPos();
//        BlockState blockstate = level.getBlockState(blockpos);
//        if (!blockstate.getBlock().isEnabled(level.enabledFeatures()))
//            return InteractionResult.FAIL;
//
//        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, blockpos, hit);
//        if (event.isCanceled())
//            return event.getCancellationResult();
//
//        if (this.getGameMode() == GameType.SPECTATOR) {
//            MenuProvider menuprovider = blockstate.getMenuProvider(level, blockpos);
//            if (menuprovider != null) {
//                player.openMenu(menuprovider);
//                return InteractionResult.SUCCESS;
//            }
//            return InteractionResult.PASS;
//        }
//
//        UseOnContext useoncontext = new UseOnContext(level, player, hand, stack, hit);
//        if (event.getUseItem() != Event.Result.DENY) {
//            InteractionResult result = stack.onItemUseFirst(useoncontext);
//            if (result != InteractionResult.PASS)
//                return result;
//        }
//
//        boolean flag = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
//        boolean flag1 = (player.isSecondaryUseActive() && flag) && !(player.getMainHandItem().doesSneakBypassUse(level, blockpos, player) && player.getOffhandItem()
//                .doesSneakBypassUse(level, blockpos, player));
//        ItemStack itemstack = stack.copy();
//        if (event.getUseBlock() == Event.Result.ALLOW || (event.getUseBlock() != Event.Result.DENY && !flag1)) {
//            InteractionResult interactionresult = blockstate.use(level, player, hand, hit);
//            if (interactionresult.consumesAction()) {
//                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockpos, itemstack);
//                return interactionresult;
//            }
//        }
//
//        if (event.getUseItem() == Event.Result.ALLOW || (!stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack.getItem()))) {
//            if (event.getUseItem() == Event.Result.DENY)
//                return InteractionResult.PASS;
//            InteractionResult interactionresult1;
//            if (this.isCreative()) {
//                int i = stack.getCount();
//                interactionresult1 = stack.useOn(useoncontext);
//                stack.setCount(i);
//            } else {
//                interactionresult1 = stack.useOn(useoncontext);
//            }
//
//            if (interactionresult1.consumesAction())
//                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockpos, itemstack);
//
//            return interactionresult1;
//        }
//        return InteractionResult.PASS;
//    }

    @FunctionalInterface
    interface EntityInteraction {
        InteractionResult run(ServerPlayer player, Entity entity, InteractionHand hand);
    }
}
