package me.srrapero720.waterframes.backport.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class SrvUseItemOnPacket extends net.minecraft.network.protocol.game.ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
   private final BlockHitResult blockHit;
   private final InteractionHand hand;
   private final int sequence;

   public SrvUseItemOnPacket(InteractionHand p_238005_, BlockHitResult p_238006_, int p_238007_) {
      super(p_238005_, p_238006_);
      this.hand = p_238005_;
      this.blockHit = p_238006_;
      this.sequence = p_238007_;
   }

   public SrvUseItemOnPacket(FriendlyByteBuf p_179796_) {
      super(p_179796_);
      this.hand = p_179796_.readEnum(InteractionHand.class);
      this.blockHit = p_179796_.readBlockHitResult();
      this.sequence = p_179796_.readVarInt();
   }

   public void write(FriendlyByteBuf p_134705_) {
      p_134705_.writeEnum(this.hand);
      p_134705_.writeBlockHitResult(this.blockHit);
      p_134705_.writeVarInt(this.sequence);
   }

   @Override
   public void handle(ServerGamePacketListener p_134702_) {
      p_134702_.handleUseItemOn(this);
   }

   @Override
   public InteractionHand getHand() {
      return this.hand;
   }

   @Override
   public BlockHitResult getHitResult() {
      return this.blockHit;
   }

   public int getSequence() {
      return this.sequence;
   }
}