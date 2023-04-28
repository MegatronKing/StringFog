package com.github.megatronking.stringfog.plugin.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetUtils {

    private SetUtils() {
    }

    @SafeVarargs
    public static <T> Set<T> fromArray(T ... array) {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, array);
        return  set;
    }


}
