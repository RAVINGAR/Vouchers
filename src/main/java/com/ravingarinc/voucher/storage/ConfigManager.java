package com.ravingarinc.voucher.storage;

import com.ravingarinc.api.I;
import com.ravingarinc.api.module.Module;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.ArmourVoucher;
import com.ravingarinc.voucher.api.BlockVoucher;
import com.ravingarinc.voucher.api.FarmVoucher;
import com.ravingarinc.voucher.api.ItemVoucher;
import com.ravingarinc.voucher.api.Voucher;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class ConfigManager extends Module {
    private final ConfigFile configFile;
    private final EquipmentSlot[] armourSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public ConfigManager(final RavinPlugin plugin) {
        super(ConfigManager.class, plugin);
        this.configFile = new ConfigFile(plugin, "config.yml");
    }

    @Override
    protected void load() {
        final VoucherTracker tracker = plugin.getModule(VoucherTracker.class);
        final HolderManager manager = plugin.getModule(HolderManager.class);

        final FileConfiguration config = configFile.getConfig();
        consumeSection(config, "defaults", (child) -> {
            wrap("material-vouchers", child::getStringList).ifPresent(v -> {
                v.forEach(string -> {
                    try {
                        final Material material = matchMaterial(string, "material-vouchers");
                        final Voucher voucher;
                        if (material.isBlock()) {
                            voucher = new BlockVoucher(material, manager);
                        } else {
                            if (Arrays.stream(armourSlots).anyMatch(slot -> material.getEquipmentSlot() == slot)) {
                                voucher = new ArmourVoucher(material, manager);
                            } else {
                                voucher = new ItemVoucher(material, manager);
                            }
                        }
                        tracker.addVoucher(voucher);
                    } catch (final IllegalArgumentException e) {
                        I.log(Level.WARNING, e.getMessage());
                    }
                });
            });

            consumeSection(child, "farm-vouchers", (vouchers) -> vouchers.getKeys(false).forEach(key -> {
                final ConfigurationSection section = vouchers.getConfigurationSection(key);
                if (section == null) {
                    I.log(Level.WARNING, "Could not load farm-voucher section for key '" + key + "' as section was empty!");
                } else {
                    try {
                        final Material item = matchMaterial(section.getString("item-material"), key + ".item-material");
                        final Material block = matchMaterial(section.getString("block-material"), key + ".block-material");
                        final Material seed = matchMaterial(section.getString("seed-material"), key + ".seed-material");
                        final Material food = matchMaterial(section.getString("food-material"), key + ".food-material");
                        final FarmVoucher voucher = new FarmVoucher(key, item, block, seed, food, manager);
                        tracker.addVoucher(voucher);
                    } catch (final IllegalArgumentException e) {
                        I.log(Level.WARNING, e.getMessage());
                    }
                }

            }));
        });

        consumeSection(config, "messages", (child) -> {
            wrap("voucher-item-name-format", child::getString).ifPresent(v -> VoucherSettings.voucherNameFormat = ChatColor.translateAlternateColorCodes('&', v));
            wrap("voucher-item-lore-format", child::getStringList).ifPresent(v -> {
                final String[] lore = new String[v.size()];
                for (int i = 0; i < lore.length; i++) {
                    lore[i] = ChatColor.translateAlternateColorCodes('&', v.get(i));
                }
                VoucherSettings.voucherLoreFormat = lore;
            });
            wrap("unlocked-message", child::getString).ifPresent(v -> VoucherSettings.unlockedMessage = ChatColor.translateAlternateColorCodes('&', v));
            wrap("locked-message", child::getString).ifPresent(v -> VoucherSettings.lockedMessage = ChatColor.translateAlternateColorCodes('&', v));
            wrap("deny-message", child::getString).ifPresent(v -> VoucherSettings.denyMessage = ChatColor.translateAlternateColorCodes('&', v));
        });

        consumeSection(config, "miscellaneous", (child) -> {
            wrap("prevent-block-placement", child::getBoolean).ifPresent(v -> VoucherSettings.preventBlockPlacement = v);
            wrap("prevent-block-mining", child::getBoolean).ifPresent(v -> VoucherSettings.preventBlockMining = v);
            wrap("block-food-material-craft", child::getBoolean).ifPresent(v -> VoucherSettings.blockFoodMaterialCraft = v);
            wrap("block-food-material-consume", child::getBoolean).ifPresent(v -> VoucherSettings.blockFoodMaterialConsume = v);
        });
    }

    /**
     * Validates if a configuration section exists at the path from parent. If it does exist then it is consumed
     *
     * @param parent   The parent section
     * @param path     The path to child section
     * @param consumer The consumer
     */
    private void consumeSection(final ConfigurationSection parent, final String path, final Consumer<ConfigurationSection> consumer) {
        final ConfigurationSection section = parent.getConfigurationSection(path);
        if (section == null) {
            I.log(Level.WARNING, parent.getCurrentPath() + " is missing a '%s' section!", path);
        }
        consumer.accept(section);
    }

    private <V> Optional<V> wrap(final String option, final Function<String, V> wrapper) {
        final V value = wrapper.apply(option);
        if (value == null) {
            I.log(Level.WARNING,
                    "Could not find configuration option '%s', please check your config! " +
                            "Using default value for now...", option);
        }
        return Optional.ofNullable(value);
    }

    @NotNull
    public Material matchMaterial(final String key, final String option) {
        if (key == null) {
            throw new IllegalArgumentException("Could not find required key for configuration option '" + option + "'!");
        }
        final Material material = Material.matchMaterial(key);
        if (material == null) {
            throw new IllegalArgumentException("Could not identify material called '" + key + "' in configuration option '" + option + '!');
        }
        return material;
    }

    @Override
    public void cancel() {
        this.configFile.reloadConfig();
    }
}
