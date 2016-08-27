package org.hello.utils;

/**
 * Created by ilshat on 27.08.16.
 */
public class StringUtils {

    private StringUtils() {}

    public static int getIdFromURL(String url) {
        String[] pieces = url.split(StringConstants.SLASH);
        return Integer.parseInt(pieces[pieces.length - 1]);
    }
}
