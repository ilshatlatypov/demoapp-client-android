package ru.jvdev.demoapp.client.android.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.jvdev.demoapp.client.android.utils.StringConstants;

/**
 * Created by ilshat on 03.08.16.
 */
public class DigestAuthUtils {

    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };

    private DigestAuthUtils() {}

    public static String calcHA1(String username, String realm, String password) {
        return md5Hex(username + StringConstants.COLON + realm + StringConstants.COLON + password);
    }

    public static String calcHA2(String method, String digestURI) {
        return md5Hex(method + StringConstants.COLON + digestURI);
    }

    public static String calcResponse(String ha1, String nonce, String nonceCount, String clientNonce, String qop, String ha2) {
        return md5Hex(ha1 + StringConstants.COLON + nonce + StringConstants.COLON + nonceCount + StringConstants.COLON + clientNonce + StringConstants.COLON + qop + StringConstants.COLON + ha2);
    }

    private static String md5Hex(String data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(hexEncode(digest.digest(data.getBytes())));
    }

    public static char[] hexEncode(byte[] bytes) {
        final int nBytes = bytes.length;
        char[] result = new char[2 * nBytes];

        int j = 0;
        for (int i = 0; i < nBytes; i++) {
            // Char for top 4 bits
            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
            // Bottom 4
            result[j++] = HEX[(0x0F & bytes[i])];
        }

        return result;
    }
}
