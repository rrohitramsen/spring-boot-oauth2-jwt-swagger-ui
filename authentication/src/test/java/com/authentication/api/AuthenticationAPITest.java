package com.authentication.api;

import com.authentication.model.UserTokenSession;
import com.authentication.service.UserDetailsServiceImpl;
import com.authentication.service.UserTokenSessionService;
import com.authentication.service.UserTokenSessionServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.Matchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @implNote This class provide Junits for {@link AuthenticationAPI} class.
 * @author Rohit.Kumar
 *
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
public class AuthenticationAPITest {

    @InjectMocks
    private AuthenticationAPI authenticationAPI;

    private MockMvc mockMvc;
    private UserDetailsService userDetailsService;
    private UserTokenSessionServiceImpl userTokenSessionService;

    @Value("${config.oauth2.tokenTimeout}")
    private String tokenExpiryTime;


    @Autowired
    private OAuthHelper oAuthHelper;

    private RequestPostProcessor requestPostProcessor;
    private  UserTokenSession userTokenSession;
    private HttpHeaders httpHeaders;
    private OAuth2Authentication oAuth2Authentication;

    @Before
    public void setup() throws IOException {

        MockitoAnnotations.initMocks(this);
        userDetailsService = Mockito.mock(UserDetailsServiceImpl.class);
        userTokenSessionService = Mockito.mock(UserTokenSessionServiceImpl.class);
        Field field = ReflectionUtils.findField(AuthenticationAPI.class, "userDetailsService");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, authenticationAPI, userDetailsService);

        field = ReflectionUtils.findField(AuthenticationAPI.class, "userTokenSessionService");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, authenticationAPI, userTokenSessionService);

        this.mockMvc = MockMvcBuilders.standaloneSetup(authenticationAPI).build();


        /**
         * testLogin and testValidateToken Set-up
         */

        String username = "user";
        String password = "password";

        requestPostProcessor = oAuthHelper.addBearerToken(username, password);
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);
        OAuth2Request oAuth2Request = oAuthHelper.createOAuth2Request(username, password);
        oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
        HttpServletRequest httpServletRequest = oAuthHelper.buildMockHttpServletRequest(username, password);
        OAuth2AuthenticationDetails oAuth2AuthenticationDetails = new OAuth2AuthenticationDetails(httpServletRequest);
        oAuth2Authentication.setDetails(oAuth2AuthenticationDetails);

        userTokenSession = new UserTokenSession(username, httpServletRequest.getHeader("Authorization"), "JSESSION : Test-123", Long.valueOf(tokenExpiryTime));

        httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", httpServletRequest.getHeader("Authorization"));
        httpHeaders.add("cookie", "JSESSION : Test-123");
    }



    @Test
    public void testValidateToken() throws Exception {

        UserTokenSessionService.ValidMappingResponse expectedValidMappingResponse = new UserTokenSessionService.ValidMappingResponse(true, userTokenSession);
        Mockito.when(userTokenSessionService.isValidUserTokenSessionMapping(any())).thenReturn(expectedValidMappingResponse);

        this.mockMvc.perform(post("/oauth/validateToken").with(requestPostProcessor).principal(oAuth2Authentication).headers(httpHeaders))
                .andExpect(status().is(200))
                .equals(userTokenSession);
    }

    @Test
    public void testLogin() throws Exception {

        Mockito.when(userTokenSessionService.saveUserTokenSessionMapping(any())).thenReturn(userTokenSession);

        this.mockMvc.perform(post("/oauth/login").with(requestPostProcessor).principal(oAuth2Authentication).headers(httpHeaders))
                    .andExpect(status().is(200))
                    .equals(userTokenSession);
    }


}
