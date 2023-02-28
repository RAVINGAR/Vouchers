package com.ravingarinc.voucher.storage.sql;

public class Schema {

    public static final String DATABASE = "vouchers";

    public static class Voucher {

        public static final String VOUCHERS = "vouchers";
        public static final String UUID = "uuid";
        public static final String KEYS = "keys";
        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + VOUCHERS + " (" +
                Voucher.UUID + " TEXT PRIMARY KEY," +
                Voucher.KEYS + " TEXT NOT NULL) WITHOUT ROWID";
        public static final String SELECT = "SELECT " + KEYS +
                " FROM " + VOUCHERS +
                " WHERE " + UUID + " = ?";

        public static final String INSERT = "INSERT INTO " + VOUCHERS + "(" +
                UUID + "," +
                KEYS + ") VALUES(?,?)";

        public static final String UPDATE = "UPDATE " + VOUCHERS +
                " SET " + KEYS + " = ?" +
                " WHERE " + UUID + " = ?";
    }
}
