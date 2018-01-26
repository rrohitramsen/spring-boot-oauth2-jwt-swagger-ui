package com.authentication.service;

import com.authentication.api.OAuthHelper;
import com.authentication.model.UserTokenSession;
import com.authentication.repository.UserTokenSessionRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.mockito.Matchers.any;

/**
 * @implNote This class provide Junits for {@link UserTokenSessionServiceImpl} class.
 * @author Rohit.Kumar
 *
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
public class UserTokenSessionServiceTest {

    private UserTokenSessionRepository userTokenSessionRepository;

    @InjectMocks
    private UserTokenSessionServiceImpl userTokenSessionService;

    @Autowired
    private OAuthHelper oAuthHelper;

    @Value("${config.oauth2.tokenTimeout}")
    private String tokenExpiryTime;
    private UserTokenSession userTokenSession;

    @Before
    public void setup() throws IOException {

        MockitoAnnotations.initMocks(this);
        userTokenSessionRepository = Mockito.mock(UserTokenSessionRepository.class);
        Field field = ReflectionUtils.findField(UserTokenSessionServiceImpl.class, "userTokenSessionRepository");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, userTokenSessionService, userTokenSessionRepository);

        /**
         * testIsValidUserTokenSessionMapping and testSaveUserTokenSessionMapping Set-up
         */

        String username = "user";
        String password = "password";
        HttpServletRequest httpServletRequest = oAuthHelper.buildMockHttpServletRequest(username, password);
        userTokenSession = new UserTokenSession(username, httpServletRequest.getHeader("Authorization"), "JSESSION : Test-123", Long.valueOf(tokenExpiryTime));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testIsValidUserTokenSessionMapping() {

        UserTokenSession mockUserTokenSession = Mockito.spy(userTokenSession);
        Mockito.when(userTokenSessionRepository.findOneByUsername(userTokenSession.getUsername())).thenReturn(mockUserTokenSession);
        Mockito.when(mockUserTokenSession.getCreatedTime()).thenReturn( LocalDateTime.now().plusDays(2));

        userTokenSessionService.isValidUserTokenSessionMapping(userTokenSession);

    }

    @Test
    public void testSaveUserTokenSessionMapping() {

        Mockito.when(userTokenSessionRepository.save(any(UserTokenSession.class))).thenReturn(userTokenSession);
        Assert.assertEquals(userTokenSessionService.saveUserTokenSessionMapping(userTokenSession), userTokenSession);
    }

}
