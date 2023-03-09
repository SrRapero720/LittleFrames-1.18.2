package me.srrapero720.waterframes.backport.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;

public class SrvboundUseItemPacket extends net.minecraft.network.protocol.game.ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
   private final InteractionHand hand;
   private final int sequence;

   public SrvboundUseItemPacket(InteractionHand p_238011_, int p_238012_) {
      super(p_238011_);
      this.hand = p_238011_;
      this.sequence = p_238012_;
   }

   public SrvboundUseItemPacket(FriendlyByteBuf p_179798_) {
      super(p_179798_);
      this.hand = p_179798_.readEnum(InteractionHand.class);
      this.sequence = p_179798_.readVarInt();
   }

   public void write(FriendlyByteBuf p_134719_) {
      p_134719_.writeEnum(this.hand);
      p_134719_.writeVarInt(this.sequence);
   }

   public void handle(ServerGamePacketListener p_134716_) {
      p_134716_.handleUseItem(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public int getSequence() {
      return this.sequence;
   }
}