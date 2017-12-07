package com.liarstudio.mvideosmsfilter;

/**
 * Created by Mihail on 05.12.2017.
 */

public class StringUtils {

    public static String buildString(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string);
        }
        return sb.toString();
    }
}
