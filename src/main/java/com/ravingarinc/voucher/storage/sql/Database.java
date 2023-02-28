package com.ravingarinc.voucher.storage.sql;

import com.ravingarinc.api.I;
import com.ravingarinc.api.module.Module;
import com.ravingarinc.api.module.ModuleLoadException;
import com.ravingarinc.api.module.RavinPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class Database extends Module {
    protected final String name;
    protected final String path;
    protected final String url;
    protected final BukkitScheduler scheduler;

    @SafeVarargs
    public Database(final String name, final Class<? extends Database> identifier, final RavinPlugin plugin, final Class<? extends Module>... dependsOn) {
        super(identifier, plugin, dependsOn);
        this.name = name + ".db";
        this.scheduler = plugin.getServer().getScheduler();
        this.path = plugin.getDataFolder() + "/" + this.name;
        this.url = "jdbc:sqlite:" + this.path;
    }

    @Override
    public void load() throws ModuleLoadException {
        final File file = new File(path);
        try {
            if (!file.exists()) {
                try (Connection connection = DriverManager.getConnection(url)) {
                    I.log(Level.INFO, "Created new database called '%s' with driver '%s'!", name, connection.getMetaData().getDriverName());
                }
            }
        } catch (final SQLException exception) {
            throw new ModuleLoadException(this, ModuleLoadException.Reason.SQL, exception);
        }
    }

    /**
     * Prepare a statement for the database and consume it. This can be used for insert/update of data
     *
     * @param request  The request
     * @param consumer The consumer to apply to the statement
     */
    protected void prepareStatement(final String request, final Consumer<PreparedStatement> consumer) {
        if (isLoaded()) {
            try (Connection connection = DriverManager.getConnection(url);
                 PreparedStatement statement = connection.prepareStatement(request)) {
                consumer.accept(statement);
                statement.executeUpdate();
            } catch (final SQLException exception) {
                I.log(Level.SEVERE, "Encountered issue inserting data into database!", exception);
            }
        }
    }

    /**
     * Execute a query on the database.
     *
     * @param execution The query
     */
    protected void execute(final String execution) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.execute(execution);
        }
    }

    @Nullable
    protected <T> T query(final String query, final Consumer<PreparedStatement> consumer, final Function<ResultSet, T> cursor) {
        if (isLoaded()) {
            try (Connection connection = DriverManager.getConnection(url);
                 PreparedStatement statement = connection.prepareStatement(query)) {
                consumer.accept(statement);
                try (ResultSet result = statement.executeQuery()) {
                    return cursor.apply(result);
                }
            } catch (final SQLException e) {
                I.log(Level.SEVERE, "Encountered issue querying database!", e);
            }
        }
        return null;
    }

    protected <T> Optional<T> query(final String query, final Function<ResultSet, T> cursor) {
        if (isLoaded()) {
            try (Connection connection = DriverManager.getConnection(url);
                 PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet result = statement.executeQuery()) {
                return Optional.ofNullable(cursor.apply(result));
            } catch (final SQLException e) {
                I.log(Level.SEVERE, "Encountered issue querying database!", e);
            }
        }
        return Optional.empty();
    }
}
