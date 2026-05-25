package dev.fix85;

import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.enums.VaultState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public final class VaultAutoOpener {
    private static int cooldown = 0;

    private VaultAutoOpener() {}

    public static void onClientTick(MinecraftClient client) {
        if (cooldown > 0) cooldown--;

        if (!Config.get().enabled) return;
        ClientPlayerEntity player = client.player;
        World world = client.world;
        if (player == null || world == null) return;

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof VaultBlock)) return;

        VaultState vs = state.get(VaultBlock.VAULT_STATE);
        if (vs != VaultState.ACTIVE) return;

        boolean ominous = state.get(VaultBlock.OMINOUS);
        if (ominous && !Config.get().openOminous) return;
        if (!ominous && !Config.get().openNormal) return;

        String itemId = getDisplayItemId(world, pos);
        if (itemId != null && !itemId.isEmpty()) {
            net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(itemId);
            if (id != null && Registries.ITEM.containsId(id)) {
                Text itemName = Registries.ITEM.get(id).getName();

                boolean isWindBurstBook = false;
                if ("minecraft:enchanted_book".equals(itemId)) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof VaultBlockEntity vault) {
                        try {
                            NbtCompound nbt = vault.createNbtWithIdentifyingData(world.getRegistryManager());
                            Optional<NbtCompound> sharedOpt = nbt.getCompound("shared_data");
                            if (sharedOpt.isPresent()) {
                                Optional<NbtCompound> itemOpt = sharedOpt.get().getCompound("display_item");
                                if (itemOpt.isPresent() && hasWindBurst(itemOpt.get())) {
                                    isWindBurstBook = true;
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }

                Text message;
                if (isWindBurstBook) {
                    message = Text.translatable("autovault.hud.spinning_item_wind_burst", itemName);
                } else {
                    message = Text.translatable("autovault.hud.spinning_item", itemName);
                }
                player.sendMessage(message, true);
            }
        }

        if (cooldown > 0) return;

        Hand keyHand = findKeyHand(player);
        if (keyHand == null) return;

        ItemStack stack = player.getStackInHand(keyHand);
        if (keyMatchesVault(stack, ominous)) {
            if (!Config.get().useFilter || displayItemPassesFilter(world, pos)) {
                if (client.interactionManager != null) {
                    ActionResult actionResult = client.interactionManager.interactBlock(player, keyHand, blockHit);
                    if (actionResult.isAccepted()) {
                        player.swingHand(keyHand); 
                        cooldown = 8; 
                    }
                }
            }
        }
    }

    private static Hand findKeyHand(PlayerEntity player) {
        if (isAnyTrialKey(player.getMainHandStack())) return Hand.MAIN_HAND;
        if (isAnyTrialKey(player.getOffHandStack()))  return Hand.OFF_HAND;
        return null;
    }

    private static boolean isAnyTrialKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.isOf(Items.TRIAL_KEY) || stack.isOf(Items.OMINOUS_TRIAL_KEY);
    }

    private static boolean keyMatchesVault(ItemStack stack, boolean ominousVault) {
        if (stack == null || stack.isEmpty()) return false;
        if (ominousVault) return stack.isOf(Items.OMINOUS_TRIAL_KEY);
        return stack.isOf(Items.TRIAL_KEY);
    }

    private static String getDisplayItemId(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof VaultBlockEntity vault)) return null;

        DynamicRegistryManager drm = world.getRegistryManager();
        NbtCompound nbt;
        try {
            nbt = vault.createNbtWithIdentifyingData(drm);
        } catch (Throwable t) {
            return null;
        }

        Optional<NbtCompound> sharedOpt = nbt.getCompound("shared_data");
        if (sharedOpt.isEmpty()) return null;
        NbtCompound shared = sharedOpt.get();

        Optional<NbtCompound> itemOpt = shared.getCompound("display_item");
        if (itemOpt.isEmpty()) return null;
        NbtCompound item = itemOpt.get();

        return item.getString("id").orElse("");
    }

    private static boolean displayItemPassesFilter(World world, BlockPos pos) {
        String itemId = getDisplayItemId(world, pos);
        if (itemId == null || itemId.isEmpty()) return false;

        net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            return false;
        }

        if (!Config.get().filter.contains(itemId)) return false;

        if ("minecraft:enchanted_book".equals(itemId) && Config.get().requireWindBurstOnBook) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof VaultBlockEntity vault) {
                DynamicRegistryManager drm = world.getRegistryManager();
                try {
                    NbtCompound nbt = vault.createNbtWithIdentifyingData(drm);
                    Optional<NbtCompound> sharedOpt = nbt.getCompound("shared_data");
                    if (sharedOpt.isPresent()) {
                        Optional<NbtCompound> itemOpt = sharedOpt.get().getCompound("display_item");
                        if (itemOpt.isPresent()) {
                            return hasWindBurst(itemOpt.get());
                        }
                    }
                } catch (Throwable ignored) {}
            }
            return false;
        }

        return true;
    }

    private static boolean hasWindBurst(NbtCompound item) {
        Optional<NbtCompound> compsOpt = item.getCompound("components");
        if (compsOpt.isEmpty()) return false;
        NbtCompound comps = compsOpt.get();

        Optional<NbtCompound> storedOpt = comps.getCompound("minecraft:stored_enchantments");
        if (storedOpt.isEmpty()) {
            return false;
        }
        NbtCompound stored = storedOpt.get();

        Optional<NbtCompound> levelsOpt = stored.getCompound("levels");
        NbtCompound levels = levelsOpt.orElse(stored);
        return levels.contains("minecraft:wind_burst");
    }
}
