package com.github.megatronking.stringfog.lib;

import java.io.UnsupportedEncodingException;

public final class Base64Fog {

    public static final String DEFAULT_KEY = "Megatron";

    public static String decode(String data) {
        return decode(data, DEFAULT_KEY);
    }

    public static String encode(String data) {
        return encode(data, DEFAULT_KEY);
    }

    public static String encode(String data, String key) {
        if (key == null || key.length() == 0) {
            key = DEFAULT_KEY;
        }
        String newData;
        try {
            newData = new String(Base64.encode(xor(data.getBytes("UTF-8"), key), Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            newData = new String(Base64.encode(xor(data.getBytes(), key), Base64.NO_WRAP));
        }
        return newData;
    }

    public static String decode(String data, String key) {
        if (key == null || key.length() == 0) {
            key = DEFAULT_KEY;
        }
        String newData;
        try {
            newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key));
        }
        return newData;
    }

    private static byte[] xor(byte[] data, String key) {
        int len = data.length;
        int lenKey = key.length();
        int i = 0;
        int j = 0;
        while (i < len) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] ^ key.charAt(j));
            i++;
            j++;
        }
        return data;
    }

}
