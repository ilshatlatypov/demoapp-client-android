package org.hello.security;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpMethod;

import java.util.Map;

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
        String ha1 = DigestAuthUtils.calcHA1(username, realm, password);
        String ha2 = DigestAuthUtils.calcHA2(method.toString(), url);
        String response = DigestAuthUtils.calcResponse(ha1, nonce, NONCE_COUNT, CLIENT_NONCE, qop, ha2);
        String authHeader = String.format("Digest username=\"%s\", realm=\"%s\", nonce=\"%s\", uri=\"%s\", qop=%s, nc=%s, cnonce=\"%s\", response=\"%s\", opaque=\"\"", username, realm, nonce, url, qop, NONCE_COUNT, CLIENT_NONCE, response);
        return authHeader;
    }
}
