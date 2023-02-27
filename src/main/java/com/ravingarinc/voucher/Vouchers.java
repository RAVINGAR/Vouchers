package com.ravingarinc.voucher;

import com.ravingarinc.api.gui.builder.GuiProvider;
import com.ravingarinc.api.module.RavinPlugin;
import com.ravingarinc.voucher.command.VoucherCommand;
import com.ravingarinc.voucher.listener.PlayerListener;
import com.ravingarinc.voucher.player.HolderManager;
import com.ravingarinc.voucher.storage.ConfigManager;
import com.ravingarinc.voucher.storage.sql.VoucherDatabase;
import com.ravingarinc.voucher.tracker.VoucherTracker;

public class Vouchers extends RavinPlugin {
    public static Vouchers instance;


    @Override
    public void onLoad() {
        instance = this;
        super.onLoad();
    }

    @Override
    public void loadModules() {

        addModule(ConfigManager.class);
        addModule(VoucherTracker.class);
        addModule(VoucherDatabase.class);
        addModule(HolderManager.class);

        addModule(PlayerListener.class);
    }

    @Override
    public void reload() {
        GuiProvider.getInstance(this).shutdown();
        super.reload();
    }


    @Override
    public void loadCommands() {
        new VoucherCommand(this).register(this);
    }
}
