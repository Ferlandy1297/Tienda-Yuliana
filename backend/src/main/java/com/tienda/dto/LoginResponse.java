package com.tienda.dto;

public class LoginResponse {
    private String token;
    private String rol;
    private String username;

    public LoginResponse() {}
    public LoginResponse(String token, String rol, String username) {
        this.token = token; this.rol = rol; this.username = username;
    }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

