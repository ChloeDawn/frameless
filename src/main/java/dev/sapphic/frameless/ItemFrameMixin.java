package dev.sapphic.frameless;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
abstract class ItemFrameMixin extends HangingEntity {
  ItemFrameMixin(final EntityType<? extends HangingEntity> type, final Level level) {
    super(type, level);
  }

  @Inject(
    method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
    at = @At(
      shift = Shift.BEFORE, value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/world/entity/decoration/ItemFrame;getItem()Lnet/minecraft/world/item/ItemStack;"),
    require = 1, allow = 1, cancellable = true)
  private void tryHide(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
    if (!this.isInvisible() && !this.level.isClientSide) {
      final @Nullable Entity attacker = source.getEntity();

      if (attacker instanceof final Player player) {
        final var held = player.getMainHandItem();

        if (held.getItem() == Items.GLASS_PANE) {
          if (!player.getAbilities().instabuild) {
            held.shrink(1);
          }

          this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
          this.setInvisible(true);

          cir.setReturnValue(true);
        }
      }
    }
  }

  @Inject(
    method = "dropItem(Lnet/minecraft/world/entity/Entity;Z)V",
    at = @At(
      shift = Shift.AFTER, value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/world/entity/decoration/ItemFrame;spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"),
    require = 1, allow = 1)
  private void tryDropPane(final CallbackInfo ci) {
    if (this.isInvisible()) {
      this.spawnAtLocation(Items.GLASS_PANE);
    }
  }
}
