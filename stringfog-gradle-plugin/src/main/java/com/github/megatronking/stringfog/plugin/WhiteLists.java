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

package com.github.megatronking.stringfog.plugin;

import com.github.megatronking.stringfog.plugin.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The white list contains some ignored levels. We defined some popular
 * library domains and classes which must be ignored when executing string fog.
 *
 * @author Megatron King
 * @since 2017/3/7 19:34
 */

public final class WhiteLists {

    public static final int FLAG_PACKAGE = 0;
    public static final int FLAG_CLASS = 1;

    private static final List<String> PACKAGE_WHITE_LIST = new ArrayList<>();
    private static final List<String> CLASS_WHITE_LIST = new ArrayList<>();

    static {
        // default packages in white list.
        addWhiteList("com.github.megatronking.stringfog.lib", FLAG_PACKAGE);
        addWhiteList("android.support", FLAG_PACKAGE);
        addWhiteList("com.google", FLAG_PACKAGE);
        addWhiteList("com.facebook", FLAG_PACKAGE);
        addWhiteList("com.baidu", FLAG_PACKAGE);
        addWhiteList("com.alipay", FLAG_PACKAGE);
        addWhiteList("com.alibaba", FLAG_PACKAGE);
        addWhiteList("com.tencent", FLAG_PACKAGE);
        addWhiteList("com.qq", FLAG_PACKAGE);
        addWhiteList("de.greenrobot", FLAG_PACKAGE);
        addWhiteList("rx", FLAG_PACKAGE);
        addWhiteList("com.squareup", FLAG_PACKAGE);

        // default classes short name in white list.
        addWhiteList("BuildConfig", FLAG_CLASS);
        addWhiteList("R", FLAG_CLASS);
        addWhiteList("R2", FLAG_CLASS);
        addWhiteList("StringFog", FLAG_CLASS);
    }

    private WhiteLists() {
    }

    public static void addWhiteList(String name, int flag) {
        switch (flag) {
            case FLAG_PACKAGE:
                PACKAGE_WHITE_LIST.add(name);
                break;
            case FLAG_CLASS:
                CLASS_WHITE_LIST.add(name);
                break;
        }
    }

    public static boolean inWhiteList(String name, int flag) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        boolean inWhiteList = false;
        switch (flag) {
            case FLAG_PACKAGE:
                inWhiteList = checkPackage(trueClassName(name));
                break;
            case FLAG_CLASS:
                inWhiteList = checkClass(shortClassName(name));
                break;
        }
        return inWhiteList;
    }

    private static boolean checkPackage(String name) {
        for (String packageName : PACKAGE_WHITE_LIST) {
            if (name.startsWith(packageName + ".")) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkClass(String name) {
        for (String className : CLASS_WHITE_LIST) {
            if (name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private static String trueClassName(String className) {
        return className.replace(File.separatorChar, '.');
    }

    private static String shortClassName(String className) {
        String[] spiltArrays = className.split(File.separator);
        return spiltArrays[spiltArrays.length - 1];
    }

}
