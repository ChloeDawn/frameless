/*
 * Copyright 2021 Chloe Dawn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sapphic.frameless;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
      method = "hurt(" + "Lnet/minecraft/world/damagesource/DamageSource;" + "F" + ")Z",
      at =
          @At(
              shift = Shift.BEFORE,
              value = "INVOKE",
              opcode = Opcodes.INVOKEVIRTUAL,
              target =
                  "Lnet/minecraft/world/entity/decoration/ItemFrame;"
                      + "getItem()"
                      + "Lnet/minecraft/world/item/ItemStack;"),
      require = 1,
      allow = 1,
      cancellable = true)
  private void trySetInvisible(
      final DamageSource source,
      final float amount,
      final CallbackInfoReturnable<Boolean> callback) {
    if (!this.level.isClientSide && !this.isInvisible() && source.getEntity() instanceof Player) {
      final Player player = (Player) source.getEntity();
      final ItemStack stack = player.getMainHandItem();

      if (stack.is(Items.GLASS_PANE)) {
        if (!player.getAbilities().instabuild) {
          stack.shrink(1);
        }

        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
        this.setInvisible(true);

        callback.setReturnValue(true);
      }
    }
  }

  @Inject(
      method = "dropItem(" + "Lnet/minecraft/world/entity/Entity;" + "Z" + ")V",
      at =
          @At(
              shift = Shift.AFTER,
              value = "INVOKE",
              opcode = Opcodes.INVOKEVIRTUAL,
              ordinal = 0,
              target =
                  "Lnet/minecraft/world/entity/decoration/ItemFrame;"
                      + "spawnAtLocation("
                      + "Lnet/minecraft/world/item/ItemStack;"
                      + ")Lnet/minecraft/world/entity/item/ItemEntity;"),
      require = 1)
  private void tryDropPane(final CallbackInfo callback) {
    if (this.isInvisible()) {
      this.spawnAtLocation(Items.GLASS_PANE);
    }
  }
}
