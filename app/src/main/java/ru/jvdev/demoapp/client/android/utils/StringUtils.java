package ru.jvdev.demoapp.client.android.utils;

/**
 * Created by ilshat on 27.08.16.
 */
public class StringUtils {

    private StringUtils() {}

    public static int getIdFromURL(String url) {
        String[] pieces = url.split(StringConstants.SLASH);
        return Integer.parseInt(pieces[pieces.length - 1]);
    }

    public static boolean containsOnlyLatinLetters(String s) {
        return s.matches("[A-Za-z]+");
    }
}
