package com.ravingarinc.voucher.item;

import com.ravingarinc.api.I;
import com.ravingarinc.voucher.api.Lazy;
import com.ravingarinc.voucher.api.Util;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import net.Indyuce.mmoitems.stat.type.NameData;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class MMOItemType implements ItemType {
    private final String type;
    private final String identifier;

    private final String tier;

    private final Lazy<String> displayName = new Lazy<>(() -> {
        final var type = MMOItems.plugin.getTypes().get(getType());
        if(type == null) {
            I.log(Level.WARNING, "Could not find type called '" + getType() + "' in MMOItems!");
            return Util.fullyCapitalise(getId());
        }
        final var item = MMOItems.plugin.getMMOItem(type, getId());
        if(item == null) {
            I.log(Level.WARNING, "Could not find MMOItems item type with identifier '" + getId() + "'!");
            return Util.fullyCapitalise(getId());
        }
        final var display = (NameData)item.getData(ItemStats.NAME);
        if(display == null) {
            return Util.fullyCapitalise(getId());
        }
        return display.getMainName();
    });

    private final Lazy<Material> material = new Lazy<>(() -> {
        final var type = MMOItems.plugin.getTypes().get(getType());
        if(type == null) {
            I.log(Level.WARNING, "Could not find type called '" + getType() + "' in MMOItems!");
            return Material.STONE;
        }
        final var item = MMOItems.plugin.getMMOItem(type, getId());
        if(item == null) {
            I.log(Level.WARNING, "Could not find MMOItems item type with identifier '" + getId() + "'!");
            return Material.STONE;
        }
        final var material = (MaterialData)item.getData(ItemStats.MATERIAL);
        if(material == null) {
            I.log(Level.WARNING, "Could not find material for MMOItem! This is unexpected!");
            return Material.STONE;
        }
        return material.getMaterial();
    });

    public MMOItemType(String type, String identifier, String tier) {
        this.type = type;
        this.identifier = identifier;
        this.tier = tier;
    }
    @Override
    public boolean isSameAs(ItemStack item) {
        if(item == null) {
            return false;
        }
        if(item.getType() == Material.AIR) {
            return false;
        }
        final var nbtItem = NBTItem.get(item);
        if(!nbtItem.hasType()) {
            return false;
        }
        return nbtItem.getType().equals(type) && nbtItem.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(identifier);
    }

    @Override
    public boolean isSameAs(BlockData block) {
        // Todo maybe if custom blocks are considered here?
        return false;
    }

    @Override
    public Material getMaterial() {
        return material.get();
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getTier() {
        return tier;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getDisplayName() {
        return displayName.get();
    }

    @Override
    public String getKey() {
        return "mmoitems_" + getId();
    }
}
