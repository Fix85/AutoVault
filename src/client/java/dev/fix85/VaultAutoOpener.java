package dev.fix85;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public final class VaultAutoOpener {
    private static int cooldown = 0;

    private VaultAutoOpener() {}

    public static void onClientTick(Minecraft client) {
        if (cooldown > 0) cooldown--;

        if (!Config.get().enabled) return;
        LocalPlayer player = client.player;
        Level world = client.level;
        if (player == null || world == null) return;

        HitResult hitResult = client.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof VaultBlock)) return;

        VaultState vs = state.getValue(VaultBlock.STATE);
        if (vs != VaultState.ACTIVE) return;

        boolean ominous = state.getValue(VaultBlock.OMINOUS);
        if (ominous && !Config.get().openOminous) return;
        if (!ominous && !Config.get().openNormal) return;

        ItemStack displayStack = getDisplayItem(world, pos);
        if (displayStack != null && !displayStack.isEmpty()) {
            String itemId = BuiltInRegistries.ITEM.getKey(displayStack.getItem()).toString();
            Component itemName = displayStack.getHoverName();

            boolean isWindBurstBook = "minecraft:enchanted_book".equals(itemId)
                    && hasWindBurst(displayStack);

            Component message = isWindBurstBook
                    ? Component.translatable("autovault.hud.spinning_item_wind_burst", itemName)
                    : Component.translatable("autovault.hud.spinning_item", itemName);
            player.sendOverlayMessage(message);
        }

        if (cooldown > 0) return;

        InteractionHand keyHand = findKeyHand(player);
        if (keyHand == null) return;

        ItemStack stack = player.getItemInHand(keyHand);
        if (keyMatchesVault(stack, ominous)) {
            if (!Config.get().useFilter || displayItemPassesFilter(world, pos)) {
                if (client.gameMode != null) {
                    InteractionResult actionResult = client.gameMode.useItemOn(player, keyHand, blockHit);
                    if (actionResult.consumesAction()) {
                        player.swing(keyHand);
                        cooldown = 8;
                    }
                }
            }
        }
    }

    private static InteractionHand findKeyHand(Player player) {
        if (isAnyTrialKey(player.getMainHandItem())) return InteractionHand.MAIN_HAND;
        if (isAnyTrialKey(player.getOffhandItem())) return InteractionHand.OFF_HAND;
        return null;
    }

    private static boolean isAnyTrialKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.getItem() == Items.TRIAL_KEY || stack.getItem() == Items.OMINOUS_TRIAL_KEY;
    }

    private static boolean keyMatchesVault(ItemStack stack, boolean ominousVault) {
        if (stack == null || stack.isEmpty()) return false;
        if (ominousVault) return stack.getItem() == Items.OMINOUS_TRIAL_KEY;
        return stack.getItem() == Items.TRIAL_KEY;
    }

    private static ItemStack getDisplayItem(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof VaultBlockEntity vault)) return null;
        VaultSharedData shared = vault.getSharedData();
        if (shared == null || !shared.hasDisplayItem()) return null;
        return shared.getDisplayItem();
    }

    private static boolean displayItemPassesFilter(Level world, BlockPos pos) {
        ItemStack displayStack = getDisplayItem(world, pos);
        if (displayStack == null || displayStack.isEmpty()) return false;

        Identifier id = BuiltInRegistries.ITEM.getKey(displayStack.getItem());
        if (id == null) return false;
        String itemId = id.toString();

        if (!Config.get().filter.contains(itemId)) return false;

        if ("minecraft:enchanted_book".equals(itemId) && Config.get().requireWindBurstOnBook) {
            return hasWindBurst(displayStack);
        }

        return true;
    }

    private static boolean hasWindBurst(ItemStack stack) {
        ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null) return false;
        for (var entry : enchantments.entrySet()) {
            var holder = entry.getKey();
            if (holder.is(Enchantments.WIND_BURST)) {
                return true;
            }
        }
        return false;
    }
}
