package me.srrapero720.waterframes.backport.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.jetbrains.annotations.NotNull;

// TODO: MOVE THIS INTO WATERCoRE
public class SrvPlayerActionPacket extends net.minecraft.network.protocol.game.ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
   private final BlockPos pos;
   private final Direction direction;
   private final SrvPlayerActionPacket.Action action;
   private final int sequence;

   public SrvPlayerActionPacket(SrvPlayerActionPacket.Action p_237983_, BlockPos p_237984_, Direction p_237985_, int p_237986_) {
      super(p_237983_, p_237984_, p_237985_);
      this.action = p_237983_;
      this.pos = p_237984_.immutable();
      this.direction = p_237985_;
      this.sequence = p_237986_;
   }

   public SrvPlayerActionPacket(SrvPlayerActionPacket.Action p_134272_, BlockPos p_134273_, Direction p_134274_) {
      this(p_134272_, p_134273_, p_134274_, 0);
   }

   public SrvPlayerActionPacket(FriendlyByteBuf p_179711_) {
      super(p_179711_);
      this.action = p_179711_.readEnum(SrvPlayerActionPacket.Action.class);
      this.pos = p_179711_.readBlockPos();
      this.direction = Direction.from3DDataValue(p_179711_.readUnsignedByte());
      this.sequence = p_179711_.readVarInt();
   }

   @Override
   public void write(@NotNull FriendlyByteBuf p_134283_) {
      p_134283_.writeEnum(this.action);
      p_134283_.writeBlockPos(this.pos);
      p_134283_.writeByte(this.direction.get3DDataValue());
      p_134283_.writeVarInt(this.sequence);
   }

   @Override
   public void handle(ServerGamePacketListener p_134280_) {
      p_134280_.handlePlayerAction(this);
   }

   @Override
   public BlockPos getPos() {
      return this.pos;
   }

   @Override
   public Direction getDirection() {
      return this.direction;
   }

   @Override
   public Action getAction() {
      return this.action;
   }

   public int getSequence() {
      return this.sequence;
   }


}