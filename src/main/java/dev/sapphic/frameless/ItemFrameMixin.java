package dev.sapphic.frameless;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
abstract class ItemFrameMixin extends AbstractDecorationEntity {
  ItemFrameMixin() {
    //noinspection ConstantConditions
    super(null, null);
  }

  @Inject(
    method = "damage",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getHeldItemStack()Lnet/minecraft/item/ItemStack;",
      shift = Shift.BEFORE),
    cancellable = true, require = 1, allow = 1)
  private void tryHide(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
    if (!this.isInvisible() && !this.world.isClient) {
      final @Nullable Entity attacker = source.getAttacker();
      if (attacker instanceof PlayerEntity) {
        final ItemStack held = ((LivingEntity) attacker).getMainHandStack();
        if (held.getItem() == Items.GLASS_PANE) {
          if (!((PlayerEntity) attacker).abilities.creativeMode) {
            held.decrement(1);
          }
          this.playSound(SoundEvents.ENTITY_ITEM_FRAME_PLACE, 1.0F, 1.0F);
          this.setInvisible(true);
          cir.setReturnValue(true);
        }
      }
    }
  }

  @Inject(
    method = "dropHeldStack",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;",
      shift = Shift.AFTER),
    require = 1, allow = 1)
  private void dropPane(final CallbackInfo ci) {
    if (this.isInvisible()) {
      this.dropItem(Items.GLASS_PANE);
    }
  }
}
