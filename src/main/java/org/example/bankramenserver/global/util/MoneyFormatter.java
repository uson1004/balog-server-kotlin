package org.example.bankramenserver.global.util;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyFormatter {

    private static final NumberFormat FORMAT =
            NumberFormat.getNumberInstance(Locale.KOREA);

    public static String format(Long amount) {
        if (amount == null) {
            return "0";
        }
        return FORMAT.format(amount);
    }
}