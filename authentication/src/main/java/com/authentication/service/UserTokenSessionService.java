package com.authentication.service;

import com.authentication.model.UserTokenSession;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Rohit.Kumar
 */
public interface UserTokenSessionService {

    /**
     * Check whether there is mapping between oauth token, username and session-id.
     * And the token is not yet expired.
     * @param userTokenSession
     * @return ValidMappingResponse if valid mapping else throw {@link UsernameNotFoundException}
     */
    ValidMappingResponse isValidUserTokenSessionMapping(UserTokenSession userTokenSession)  throws UsernameNotFoundException;

    /**
     *
     * @param userTokenSession
     * @return token session record from data base.
     */
    UserTokenSession saveUserTokenSessionMapping(UserTokenSession userTokenSession);


    /**
     * Class to store isValidUserTokenSessionMapping() response.
     */
    class ValidMappingResponse {

        private boolean valid;
        private UserTokenSession userTokenSession;

        public ValidMappingResponse() {
        }

        public ValidMappingResponse(boolean valid, UserTokenSession userTokenSession ) {
            this.valid = valid;
            this.userTokenSession = userTokenSession;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public UserTokenSession getUserTokenSession() {
            return userTokenSession;
        }

        public void setUserTokenSession(UserTokenSession userTokenSession) {
            this.userTokenSession = userTokenSession;
        }
    }

}
