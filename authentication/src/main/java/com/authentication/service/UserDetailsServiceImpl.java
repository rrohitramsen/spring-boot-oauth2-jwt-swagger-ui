package com.authentication.service;

import com.authentication.model.User;
import com.authentication.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Rohit.Kumar
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final Logger LOGGER = Logger.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

	    User userFromDataBase = userRepository.findOneByUsername(username);
        if (userFromDataBase == null) {
			LOGGER.info("User " + username + " was not found in the database");
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }
        return userFromDataBase;

	}
}