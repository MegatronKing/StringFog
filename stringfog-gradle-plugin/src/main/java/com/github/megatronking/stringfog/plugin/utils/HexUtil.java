package com.github.megatronking.stringfog.plugin.utils;


public class HexUtil {
    private static final char[] digits = "0123456789ABCDEF".toCharArray();

    public static final String fromBytes(final byte[] bytes) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            buf.append(digits[(bytes[i] >> 4) & 0x0f]);
            buf.append(digits[bytes[i] & 0x0f]);
        }
        return buf.toString();
    }

    public static final byte[] toByteArray(final String hexString) {
        if ((hexString.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        final int len = hexString.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
