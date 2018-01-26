package com.authentication.service;

import com.authentication.model.UserTokenSession;
import com.authentication.repository.UserTokenSessionRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author Rohit.Kumar
 */
@Service
public class UserTokenSessionServiceImpl implements UserTokenSessionService {

    private static final Logger LOGGER = Logger.getLogger(UserTokenSessionServiceImpl.class);

    @Autowired
    private UserTokenSessionRepository userTokenSessionRepository;


    @Override
    public ValidMappingResponse isValidUserTokenSessionMapping(UserTokenSession userTokenSession) throws UsernameNotFoundException {

        String username = userTokenSession.getUsername();
        UserTokenSession userTokenSessionFromDB = userTokenSessionRepository.findOneByUsername(username);

        if (Objects.isNull(userTokenSessionFromDB)) {

            LOGGER.error("User " + username + " mapping with token is not found in the database.");
            throw new UsernameNotFoundException("User " + username + "  mapping with token is not found in the database.");
        }

        /**
         * TODO Time zone of data base and client may be different.
         */
        LocalDateTime currentSystemTime = LocalDateTime.now();
        ZonedDateTime currentZonedDateTime = currentSystemTime.atZone(ZoneId.systemDefault());
        long currentTimeInMillis = currentZonedDateTime.toInstant().toEpochMilli();

        ZonedDateTime dataBaseZonedDateTime = userTokenSessionFromDB.getCreatedTime().atZone(ZoneId.systemDefault());

        /**
         * tokenTimeInMillis = created_time in millis + expiry time (seconds) * 1000.
         */
        long  tokenTimeInMillis = dataBaseZonedDateTime.toInstant().toEpochMilli() + (userTokenSessionFromDB.getExpiryTime() * 1000);

        if ( currentTimeInMillis >= tokenTimeInMillis) {

            LOGGER.info("User " + username + " token has expired. Please generate new token. Deleting the expired token mapping.");
            userTokenSessionRepository.delete(userTokenSessionFromDB);
            throw new UsernameNotFoundException("User " + username + " token has expired. Please generate new token.");

        }else if(!userTokenSession.equals(userTokenSessionFromDB)) {

            if (!userTokenSessionFromDB.getToken().equals(userTokenSession.getToken())){
                LOGGER.info("User "+userTokenSession.getUsername()+ " has invalid user and token mapping. Please generate new token.");

            } else {
                LOGGER.info("User "+userTokenSession.getUsername()+ " has invalid user and session-id mapping. Please generate new token.");
            }

            LOGGER.info("So, Deleting the invalid mapping.");
            userTokenSessionRepository.delete(userTokenSessionFromDB);
            throw new UsernameNotFoundException("User " + username + " has invalid user, session-id and token mapping. Please generate new token.");

        }else {

            LOGGER.info("User " + username + " has valid token.");
            ValidMappingResponse validMappingResponse = new ValidMappingResponse(true, userTokenSessionFromDB);
            return validMappingResponse;
        }

    }

    @Override
    public UserTokenSession saveUserTokenSessionMapping(UserTokenSession userTokenSession) {

        UserTokenSession userTokenSessionFromDB = userTokenSessionRepository.findOneByUsername(userTokenSession.getUsername());

        /**
         * 1. If User is making the login call again with the same session-id and token. Then delete the old mapping and return the new inserted mapping.
         * 2. If same user is making login call with the new token or session-id. Then delete the old mapping and return the new inserted mapping
         */
        if (Objects.nonNull(userTokenSessionFromDB)) {

            if (userTokenSessionFromDB.equals(userTokenSession)) {
                LOGGER.info("User "+userTokenSession.getUsername()+ " making login call again with same token and session-id.");

            } else if (!userTokenSessionFromDB.getToken().equals(userTokenSession.getToken())){
                LOGGER.info("User "+userTokenSession.getUsername()+ " making login call with new token");

            } else {
                LOGGER.info("User "+userTokenSession.getUsername()+ " making login call with different session-id");

            }
            LOGGER.info("So, Deleting older mapping from tbl_user_token_session."+userTokenSessionFromDB);
            userTokenSessionRepository.delete(userTokenSessionFromDB);

        }

        return userTokenSessionRepository.save(userTokenSession);
    }



}
