package com.demo.apiserver.controller;

import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.apiserver.mapper.UserMapper;
import com.demo.apiserver.security.JwtAuthRequest;
import com.demo.apiserver.security.JwtAuthResponse;
import com.demo.apiserver.security.JwtUtil;

@RestController
@RequestMapping("/api")
public class AuthController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;
    
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthRequest request
            , @RequestParam(value = "rememberMe", defaultValue = "false", required = false) boolean rememberMe
            , HttpServletResponse response) throws AuthenticationException {
		
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        
        try {
        	// 아래 코드에서 loadUserByUsername 메소드가 실행됨.
            Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.createToken(authentication, rememberMe);
            response.addHeader("Authorization", jwt);
            
            
            return ResponseEntity.ok(new JwtAuthResponse(jwt));
        } catch (Exception ae) {
            logger.trace("Authentication exception trace: {}", ae);
            return new ResponseEntity<>(Collections.singletonMap("AuthenticationException", ae.getLocalizedMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

}
