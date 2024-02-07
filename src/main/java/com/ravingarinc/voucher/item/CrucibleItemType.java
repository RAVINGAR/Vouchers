package com.ravingarinc.voucher.item;

import com.ravingarinc.api.I;
import com.ravingarinc.voucher.api.Lazy;
import com.ravingarinc.voucher.api.Util;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class CrucibleItemType implements ItemType {
    private final String identifier;
    private final String tier;

    private final Lazy<String> displayName = new Lazy<>(() -> {
        final var item = MythicBukkit.inst().getItemManager().getItem(getId());
        if(item.isPresent()) {
            return item.get().getDisplayName();
        } else {
            return Util.fullyCapitalise(getId());
        }
    });

    private final Lazy<Material> material = new Lazy<>(() -> {
        final var item = MythicBukkit.inst().getItemManager().getItem(getId());
        if(item.isPresent()) {
            return (item.get().getMaterial());
        }
        I.log(Level.WARNING, "Could not find MythicCrucible item with id '" + getId() + "'!");
        return Material.STONE;
    });
    public CrucibleItemType(String identifier, String tier) {
        this.identifier = identifier;
        this.tier = tier;
    }
    @Override
    public boolean isSameAs(ItemStack item) {
        if(item == null) {
            return false;
        }
        final var identifier = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
        if(identifier == null) {
            return false;
        }
        return identifier.equalsIgnoreCase(this.identifier);
    }

    @Override
    public Material getMaterial() {
        return material.get();
    }

    @Override
    public boolean isSameAs(BlockData block) {
        // Todo maybe handle this later?
        return false;
    }

    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getDisplayName() {
        return displayName.get();
    }

    @Override
    public String getTier() {
        return tier;
    }

    @Override
    public String getKey() {
        return "crucible_" + getId();
    }
}
