package com.blogforge;

import java.util.Objects;

public class Util {

    public static Object getOrDefault(Object toCheck, Object defaultVal) {
        if(Objects.nonNull(toCheck)) return toCheck;
        else return defaultVal;
    }

}
