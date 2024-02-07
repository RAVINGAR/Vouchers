package com.ravingarinc.voucher.api;

public class Util {

    public static String fullyCapitalise(final String word) {
        final StringBuilder builder = new StringBuilder();
        final String[] split = word.toLowerCase().split("[_ -]");
        int i = 0;
        for (final String s : split) {
            builder.append(s.toUpperCase().charAt(0));
            builder.append(s.substring(1));
            if (++i < split.length) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}
