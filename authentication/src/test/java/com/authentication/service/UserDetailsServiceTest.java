package com.authentication.service;

import com.authentication.api.OAuthHelper;
import com.authentication.model.User;
import com.authentication.repository.UserRepository;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @implNote This class provide Junits for {@link UserDetailsServiceImpl} class.
 * @author Rohit.Kumar
 *
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
public class UserDetailsServiceTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UserRepository userRepository;

    @Autowired
    private OAuthHelper oAuthHelper;

    @Value("${config.oauth2.tokenTimeout}")
    private String tokenExpiryTime;

    private User user;

    @Before
    public void setup() throws IOException {

        MockitoAnnotations.initMocks(this);
        userRepository = Mockito.mock(UserRepository.class);

        Field field = ReflectionUtils.findField(UserDetailsServiceImpl.class, "userRepository");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, userDetailsService, userRepository);

        user = Mockito.mock(User.class);
    }

    @Test
    public void testLoadUserByUsername() {

        String username = "dumy_user";
        Mockito.when(userRepository.findOneByUsername(username)).thenReturn(user);

        Assert.assertEquals(userDetailsService.loadUserByUsername(username), user);
    }
}
