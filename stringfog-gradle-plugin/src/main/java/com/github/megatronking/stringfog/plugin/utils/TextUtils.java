package com.github.megatronking.stringfog.plugin.utils;

public final class TextUtils {

    private TextUtils() {
    }

    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmptyAfterTrim(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }
}
