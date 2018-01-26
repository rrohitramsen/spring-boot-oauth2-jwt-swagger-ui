package com.authentication.api;

import com.authentication.api.AuthenticationAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for creating OAuth access token, OAuth Authentication, {@link MockHttpServletRequest} objects.
 */
@Component
public class OAuthHelper {


    @Value("${config.oauth2.clientID}")
    private String clientID;

    @Value("${config.oauth2.clientSecret}")
    private String clientSecret;

    @Value("${config.oauth2.resource.id}")
    private String resourceId;

    @Autowired
    private AuthorizationServerTokenServices tokenservice;

    /**
     * Create {@link RequestPostProcessor} for {@link AuthenticationAPI} testing.
     * @param username
     * @param password
     * @return
     */
    public RequestPostProcessor addBearerToken(final String username, String password) {

        RequestPostProcessor requestPostProcessor = new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {

                OAuth2Request oauth2Request = createOAuth2Request(username, password);
                Authentication userauth = new TestingAuthenticationToken(username, password);
                OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userauth);
                OAuth2AccessToken oAuth2AccessToken = tokenservice.createAccessToken(oauth2auth);

                request.addHeader("Authorization", "Bearer " + oAuth2AccessToken.getValue());
                request.setSession(new MockHttpSession());
                return request;
            }
        };
        return requestPostProcessor;
    }

    /**
     * Build {@link MockHttpServletRequest} for the given user with {@link OAuth2AccessToken}.
     * @param username
     * @param password
     * @return HttpServletRequest
     */
    public HttpServletRequest buildMockHttpServletRequest(final String username, String password) {

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();//Mockito.mock(MockHttpServletRequest.class);
        OAuth2Request oauth2Request = createOAuth2Request(username, password);
        Authentication userauth = new TestingAuthenticationToken(username, password);
        OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, userauth);
        OAuth2AccessToken oAuth2AccessToken = tokenservice.createAccessToken(oauth2auth);

        httpServletRequest.addHeader("Authorization", "Bearer " + oAuth2AccessToken.getValue());
        httpServletRequest.setSession(new MockHttpSession());
        return httpServletRequest;
    }

    /**
     * Create {@link OAuth2Request}
     * @param username
     * @param password
     * @return OAuth2Request
     */
    public OAuth2Request createOAuth2Request(String username, String password) {

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("username", username);
        requestParams.put("password", password);
        requestParams.put("grant_type", "password");
        requestParams.put("client_id", clientID);
        requestParams.put("client_secret", clientSecret);

        Set<String> scope = new HashSet<>();
        scope.add("read");
        scope.add("write");

        Set<String> resourceIds = new HashSet<>();
        resourceIds.add(resourceId);

        OAuth2Request oauth2Request = new OAuth2Request(requestParams, clientID, null, true, scope, resourceIds, null, null, null);

        return oauth2Request;
    }


}