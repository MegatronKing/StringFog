package com.github.megatronking.stringfog.plugin.utils;

import java.security.MessageDigest;

public class MD5 {

    private MD5() {

    }

    /**
     * get md5 string for input buffer
     *
     * @param buffer data to be calculated
     * @return md5 result in string format
     */
    public static String getMessageDigest(byte[] buffer) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0: md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }


}
