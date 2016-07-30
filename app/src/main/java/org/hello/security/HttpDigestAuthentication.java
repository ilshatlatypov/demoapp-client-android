package org.hello.security;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpMethod;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.hello.utils.StringConstants.COLON;

/**
 * Created by ilshat on 30.07.16.
 */
public class HttpDigestAuthentication extends HttpAuthentication {

    private static final String NONCE_COUNT = "00000001";
    private static final String CLIENT_NONCE = "";

    private final String username;
    private final String password;

    private final String realm;
    private final String qop;
    private final String nonce;

    private final HttpMethod method;
    private final String url;

    public HttpDigestAuthentication(String username, String password, Map<String, String> authParams, HttpMethod method, String url) {
        this.username = username;
        this.password = password;
        this.realm = authParams.get("realm");
        this.qop = authParams.get("qop");
        this.nonce = authParams.get("nonce");
        this.method = method;
        this.url = url;
    }

    @Override
    public String getHeaderValue() {
        String ha1 = calcHA1(username, realm, password);
        String ha2 = calcHA2(method.toString(), url);
        String response = calcResponse(ha1, nonce, NONCE_COUNT, CLIENT_NONCE, qop, ha2);
        String authHeader = String.format("Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", qop=%s, nc=%s, cnonce=\"%s\", response=\"%s\", opaque=\"\"", username, realm, nonce, url, qop, NONCE_COUNT, CLIENT_NONCE, response);
        return authHeader;
    }

    private String calcHA1(String username, String realm, String password) {
        return md5Hex(username + COLON + realm + COLON + password);
    }

    private String calcHA2(String method, String digestURI) {
        return md5Hex(method + COLON + digestURI);
    }

    private String calcResponse(String ha1, String nonce, String nonceCount, String clientNonce, String qop, String ha2) {
        return md5Hex(ha1 + COLON + nonce + COLON + nonceCount + COLON + clientNonce + COLON + qop + COLON + ha2);
    }

    private String md5Hex(String data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(encode(digest.digest(data.getBytes())));
    }

    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };

    public static char[] encode(byte[] bytes) {
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
