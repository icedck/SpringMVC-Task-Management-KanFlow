package com.codegym.kanflow.dto;

public class UserDto {
    private Long id;
    private String username;
    private String email;

    // Constructor mặc định (cần cho một số thư viện)
    public UserDto() {
    }

    // Constructor đầy đủ
    public UserDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}