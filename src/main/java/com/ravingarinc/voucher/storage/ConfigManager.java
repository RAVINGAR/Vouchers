package com.ravingarinc.voucher.storage;

import com.ravingarinc.api.I;
import com.ravingarinc.api.module.Module;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.*;
import com.ravingarinc.voucher.item.CrucibleItemType;
import com.ravingarinc.voucher.item.ItemType;
import com.ravingarinc.voucher.item.MMOItemType;
import com.ravingarinc.voucher.item.VanillaItemType;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class ConfigManager extends Module {
    private final ConfigFile configFile;

    private final ConfigFile voucherFile;
    private final EquipmentSlot[] armourSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public ConfigManager(final RavinPlugin plugin) {
        super(ConfigManager.class, plugin);
        this.configFile = new ConfigFile(plugin, "config.yml");
        this.voucherFile = new ConfigFile(plugin, "vouchers.yml");
    }

    @Override
    public void load() {
        final VoucherTracker tracker = plugin.getModule(VoucherTracker.class);
        final HolderManager manager = plugin.getModule(HolderManager.class);

        final FileConfiguration config = configFile.getConfig();
        final FileConfiguration vouchersConfig = voucherFile.getConfig();
        wrap("material-vouchers", vouchersConfig::getMapList).ifPresent(v -> v.forEach(map -> {
            try {
                final ItemType type = getItemType(map.get("item").toString());
                if(type != null) {
                    final ItemVoucher voucher;
                    final Material material = type.getMaterial();
                    final String tier = map.get("tier").toString();
                    final String craftTime = map.get("craft-time").toString();
                    final int energyCost = Integer.parseInt(map.get("energy-cost").toString());
                    final int moneyCost = Integer.parseInt(map.get("money-cost").toString());
                    if(material.isBlock()) {
                        voucher = new BlockVoucher(type, tier, craftTime, energyCost, moneyCost, manager);
                    } else {
                        if(Arrays.stream(armourSlots).anyMatch(slot -> material.getEquipmentSlot() == slot)) {
                            voucher = new ArmourVoucher(type, tier, craftTime, energyCost, moneyCost, manager);
                        } else {
                            voucher = new ItemVoucher(type, tier, craftTime, energyCost, moneyCost, manager);
                        }
                    }
                    ((List<?>)map.get("item-costs")).forEach(val -> {
                        final Map<?, ?> innerMap = (Map<?, ?>)val;
                        final ItemType costType = getItemType(innerMap.get("id").toString());
                        final int quantity = Integer.parseInt(innerMap.get("quantity").toString());
                        voucher.addItemCost(costType, quantity);
                    });
                    tracker.addVoucher(voucher);
                }
            } catch (final IllegalArgumentException e) {
                I.log(Level.WARNING, e.getMessage(), e);
            }
        }));

        consumeSection(vouchersConfig, "farm-vouchers", (vouchers) -> vouchers.getKeys(false).forEach(key -> {
            final ConfigurationSection section = vouchers.getConfigurationSection(key);
            if (section == null) {
                I.log(Level.WARNING, "Could not load farm-voucher section for key '" + key + "' as section was empty!");
            } else {
                try {
                    String tier = section.getString("tier");
                    if(tier == null) {
                        I.log(Level.WARNING, "Tier for farming voucher '" + key + "' could not be found! Please make sure to include the tier. Using a default of 'basic' for now...");
                        tier = "basic";
                    }
                    final ItemType item = getItemType(section.getString("item-material"));
                    final ItemType block = getItemType(section.getString("block-material"));
                    final ItemType seed = getItemType(section.getString("seed-material"));
                    final ItemType food = getItemType(section.getString("food-material"));
                    final FarmVoucher voucher = new FarmVoucher(key, tier, item, block, seed, food, manager);
                    tracker.addVoucher(voucher);
                } catch (final IllegalArgumentException e) {
                    I.log(Level.WARNING, e.getMessage());
                }
            }

        }));

        consumeSection(config, "messages", (child) -> {
            wrap("voucher-item-name-format", child::getString).ifPresent(v -> VoucherSettings.voucherNameFormat = ChatColor.translateAlternateColorCodes('&', v));
            wrap("voucher-item-name-format-locked", child::getString).ifPresent(v -> VoucherSettings.voucherNameFormatLocked = ChatColor.translateAlternateColorCodes('&', v));
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
            wrap("prevent-crafting", child::getBoolean).ifPresent(v -> VoucherSettings.preventItemCrafting = v);
            wrap("prevent-damage", child::getBoolean).ifPresent(v -> VoucherSettings.preventItemDamage = v);
            wrap("prevent-interaction", child::getBoolean).ifPresent(v -> VoucherSettings.preventItemInteraction = v);
            wrap("prevent-equip", child::getBoolean).ifPresent(v -> VoucherSettings.preventItemEquipping = v);

            wrap("prevent-block-placement", child::getBoolean).ifPresent(v -> VoucherSettings.preventBlockPlacement = v);
            wrap("prevent-block-mining", child::getBoolean).ifPresent(v -> VoucherSettings.preventBlockMining = v);
            wrap("block-food-material-craft", child::getBoolean).ifPresent(v -> VoucherSettings.blockFoodMaterialCraft = v);
            wrap("block-food-material-consume", child::getBoolean).ifPresent(v -> VoucherSettings.blockFoodMaterialConsume = v);
            wrap("gui-border-material1", child::getString).ifPresent(v -> VoucherSettings.border1 = Material.matchMaterial(v));
            wrap("gui-border-material2", child::getString).ifPresent(v -> VoucherSettings.border2 = Material.matchMaterial(v));

            if (VoucherSettings.border1 == null) {
                I.log(Level.WARNING, "Could not match material for gui-border-material1");
                VoucherSettings.border1 = Material.BLACK_STAINED_GLASS_PANE;
            }
            if (VoucherSettings.border2 == null) {
                I.log(Level.WARNING, "Could not match material for gui-border-material2");
                VoucherSettings.border2 = Material.GRAY_STAINED_GLASS_PANE;
            }
        });
    }

    @Nullable
    private ItemType getItemType(@Nullable String line) {
        if(line == null || line.isEmpty()) {
            return null;
        }
        String[] split = line.toLowerCase().split(":");
        if(split[0].equals("mmoitem") || split[0].equals("mmoitems")) {
            if(!Bukkit.getServer().getPluginManager().isPluginEnabled("MMOItems")) {
                I.log(Level.WARNING, "Could not load MMOItems item type from '" + line + "' as MMOItems is not enabled!");
                return null;
            }
            if(split.length == 3) {
                return new MMOItemType(split[1].toUpperCase(), split[2].toUpperCase());
            } else {
                I.log(Level.WARNING, "Could not parse '" + line + "' as MMOItem as this requires both a type and identifier to specified!");
                return null;
            }
        } else if(split[0].equals("crucible")) {
            if(!Bukkit.getServer().getPluginManager().isPluginEnabled("MythicCrucible")) {
                I.log(Level.WARNING, "Could not load MythicCrucible item type from '" + line + "' as MythicCrucible is not enabled!");
                return null;
            }
            return new CrucibleItemType(split[1]);
        } else {
            int i;
            if(split[0].equals("vanilla")) {
                i = 1;
            } else if(split.length == 1) {
                i = 0;
            } else {
                I.log(Level.WARNING, "Could not determine item type from '" + line + "'! (have you specified a tier?)");
                return null;
            }
            final var material = Material.matchMaterial(split[i]);
            if(material == null) {
                I.log(Level.WARNING, "Could not find vanilla material called '" + split[i] + "' to determine ItemType!");
                return null;
            } else {
                return new VanillaItemType(material);
            }
        }
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
