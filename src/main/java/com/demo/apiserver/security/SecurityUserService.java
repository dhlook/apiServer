package com.demo.apiserver.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.demo.apiserver.mapper.UserMapper;

@Service
public class SecurityUserService implements UserDetailsService {

    static final Logger logger = LoggerFactory.getLogger(SecurityUserService.class);

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private HttpServletRequest request;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        UserDetails user = null;
        String pwd = "";
        String role = "";

        try {
            Map<String, String> userMap = new HashMap<String, String>();
            userMap = userMapper.getUser(userId);
			
			if(null != userMap) {
				if(null != userMap.get("PASSWORD")) {
					pwd = (String) userMap.get("PASSWORD");
				}
				
				if(null != userMap.get("ROLE")) {
					role = (String) userMap.get("ROLE");
				}
			} else {
				throw new InternalAuthenticationServiceException(role);
			}

            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            user = new User(userId, pwd, true, true, true, true, authorities);
        } catch (Exception e) {
            logger.debug("loadUserByUsername Exception : {}", e.getMessage());
        }

        return user;
    }
}
