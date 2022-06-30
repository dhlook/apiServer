package com.demo.apiserver.security;

public class JwtAuthResponse{

    private final String token;

    public JwtAuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
  
}
