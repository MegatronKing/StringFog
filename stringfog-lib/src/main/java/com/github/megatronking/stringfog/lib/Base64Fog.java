/*
 * Copyright (C) 2017, Megatron King
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.megatronking.stringfog.lib;

import java.io.UnsupportedEncodingException;

public final class Base64Fog {

    public static final String DEFAULT_KEY = "StringFog";

    public static String decode(String data) {
        return decode(data, DEFAULT_KEY);
    }

    public static String encode(String data) {
        return encode(data, DEFAULT_KEY);
    }

    public static String encode(String data, String key) {
        String newData;
        try {
            newData = new String(Base64.encode(xor(data.getBytes("UTF-8"), key), Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            newData = new String(Base64.encode(xor(data.getBytes(), key), Base64.NO_WRAP));
        }
        return newData;
    }

    public static String decode(String data, String key) {
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
