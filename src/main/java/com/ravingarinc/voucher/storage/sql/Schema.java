package com.ravingarinc.voucher.storage.sql;

public class Schema {

    public static final String DATABASE = "vouchers";

    public static class Voucher {

        public static final String VOUCHERS = "vouchers";
        public static final String UUID = "uuid";
        public static final String KEYS = "keys";
        public static final String createTable = "CREATE TABLE IF NOT EXISTS " + VOUCHERS + " (" +
                Voucher.UUID + " TEXT PRIMARY KEY," +
                Voucher.KEYS + " TEXT NOT NULL) WITHOUT ROWID";
        public static final String select = "SELECT " + KEYS +
                " FROM " + VOUCHERS +
                " WHERE " + UUID + " = ?";

        public static final String insert = "INSERT INTO " + VOUCHERS + "(" +
                UUID + "," +
                KEYS + ") VALUES(?,?)";

        public static final String update = "UPDATE " + VOUCHERS +
                " SET " + KEYS + " = ?" +
                " WHERE " + UUID + " = ?";
    }
}
