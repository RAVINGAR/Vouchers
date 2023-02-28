package com.ravingarinc.voucher.storage.sql;

import com.ravingarinc.api.I;
import com.ravingarinc.api.module.ModuleLoadException;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.api.Voucher;
import com.ravingarinc.voucher.player.Holder;
import com.ravingarinc.voucher.tracker.VoucherTracker;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;

public class VoucherDatabase extends Database {
    private VoucherTracker tracker;

    public VoucherDatabase(final RavinPlugin plugin) {
        super(Schema.DATABASE, VoucherDatabase.class, plugin, VoucherTracker.class);
    }

    @Override
    public void load() throws ModuleLoadException {
        super.load();
        try {
            execute(Schema.Voucher.CREATE_TABLE);
        } catch (final SQLException exception) {
            I.log(Level.WARNING, "Encountered issue creating tables for database!", exception);
        }

        tracker = plugin.getModule(VoucherTracker.class);
    }


    public Optional<Holder> loadHolder(final Player player) {
        return Optional.ofNullable(query(Schema.Voucher.SELECT, (statement) -> {
            try {
                statement.setString(1, player.getUniqueId().toString());
            } catch (final SQLException exception) {
                I.log(Level.WARNING, "Encountered issue loading holder!", exception);
            }
        }, (result) -> {
            try {
                final Holder holder = new Holder(player);
                if (result.next()) {
                    final String keys = result.getString(1);
                    if (!keys.isEmpty()) {
                        for (final String key : keys.split(",")) {
                            final Voucher voucher = tracker.getVoucher(key);
                            if (voucher != null) {
                                holder.unlock(voucher);
                            }
                        }
                    }
                } else {
                    prepareStatement(Schema.Voucher.INSERT, (statement) -> {
                        try {
                            statement.setString(1, player.getUniqueId().toString());
                            statement.setString(2, "");
                        } catch (final SQLException exception) {
                            I.log(Level.WARNING, "Encountered issue inserting player into database!");
                        }
                    });
                }
                return holder;
            } catch (final SQLException exception) {
                I.log(Level.WARNING, "Encountered issue loading holder!");
            }
            return null;
        }));
    }

    public void saveHolder(final Holder holder) {
        prepareStatement(Schema.Voucher.UPDATE, (statement) -> {
            try {
                final Iterator<String> iterator = holder.getVoucherKeys().iterator();
                final StringBuilder builder = new StringBuilder();
                while (iterator.hasNext()) {
                    builder.append(iterator.next());
                    if (iterator.hasNext()) {
                        builder.append(",");
                    }
                }
                statement.setString(1, builder.toString());
                statement.setString(2, holder.getPlayer().getUniqueId().toString());
            } catch (final SQLException exception) {
                I.log(Level.WARNING, "Encountered issue saving holder!", exception);
            }
        });
    }

    @Override
    public void cancel() {
        // do nothing
    }
}
