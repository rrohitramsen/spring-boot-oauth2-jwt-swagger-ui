package com.authentication.api;

import com.authentication.model.UserTokenSession;
import com.authentication.service.UserTokenSessionService;
import com.authentication.service.UserTokenSessionServiceImpl;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Objects;

/**
 * @author Rohit.Kumar
 * @version 0.01
 */
@RestController
@RequestMapping("/oauth")
@Api(value="Authentication API", description="Authenticate user using authorization token.")
public class AuthenticationAPI {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationAPI.class);

    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService userDetailsService;

    @Value("${config.oauth2.tokenTimeout}")
    private long tokenExpiryTime;

    @Autowired
    private UserTokenSessionServiceImpl userTokenSessionService;


    @ApiOperation(value = "Authenticated User Login", response = UserTokenSession.class)
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = UserTokenSession.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    public ResponseEntity<UserTokenSession> login(@RequestHeader HttpHeaders httpHeaders, Principal principal, HttpServletRequest httpServletRequest) {

        String username = principal.getName();
        UserTokenSession userTokenSession = buildUserTokenSession(principal, httpHeaders);
        userTokenSession = userTokenSessionService.saveUserTokenSessionMapping(userTokenSession);

        LOGGER.info("User "+username+" successfully logged in. User, Token and Session mapping stored."+userTokenSession);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Message", "Success");
        responseHeaders.add("Set-Cookie", userTokenSession.getSessionId());

        return new ResponseEntity(userTokenSession, responseHeaders, HttpStatus.OK);
    }

    @ApiOperation(value = "Validate the given token", response = UserTokenSession.class)
    @RequestMapping(value = "/validateToken", method = RequestMethod.POST,
             produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = UserTokenSession.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")})
    public ResponseEntity<UserTokenSession> validateToken(@RequestHeader HttpHeaders httpHeaders,  Principal principal, HttpServletRequest httpServletRequest) {


        String username = principal.getName();
        UserTokenSession userTokenSession = buildUserTokenSession(principal, httpHeaders);

        ResponseEntity<UserTokenSession> responseEntity;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Set-Cookie", userTokenSession.getSessionId());

        UserTokenSessionService.ValidMappingResponse validMappingResponse = userTokenSessionService.isValidUserTokenSessionMapping(userTokenSession);
        if (validMappingResponse.isValid()) {

            LOGGER.info("User " + username + " has valid token."+validMappingResponse.getUserTokenSession());
            responseHeaders.add("Message", "Valid Token");
            responseEntity = new ResponseEntity<UserTokenSession>(validMappingResponse.getUserTokenSession(), responseHeaders, HttpStatus.OK);

        } else {

            LOGGER.info("User " + username + " has invalid token.");
            responseHeaders.add("Message", "Invalid Token");
            responseEntity = new ResponseEntity<UserTokenSession>(userTokenSession, responseHeaders, HttpStatus.UNAUTHORIZED);
        }

        return responseEntity;
    }

    /**
     * Build Token session using {@link Principal} and {@link HttpHeaders}
     * @param principal
     * @param httpHeaders
     * @return TokenSession
     */
    private UserTokenSession buildUserTokenSession(Principal principal, HttpHeaders httpHeaders) {

        OAuth2AuthenticationDetails oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails) ((OAuth2Authentication) principal).getDetails();
        String tokenValue = oAuth2AuthenticationDetails.getTokenValue();
        String username = principal.getName();
        String [] sessionId = new String[1];

        if (Objects.nonNull(httpHeaders.get("cookie"))) {
            sessionId = httpHeaders.get("cookie").get(0).split(";");
        }else {
            LOGGER.info("User " + username + " cookie not found. JSessionId not set.");

            /**
             * Swagger2 does not support cookie for that putting default sessssion id.
             */
            sessionId[0] = "JSEESION-ID";
        }

        UserTokenSession userTokenSession = new UserTokenSession(username, tokenValue, sessionId[0], tokenExpiryTime);
        return userTokenSession;
    }


}
