package com.codegym.kanflow.dto;

public class MemberJoinMessage extends WebSocketMessage {
    private Long userId;
    private String email;
    private String message;

    public MemberJoinMessage() {
        super("MEMBER_JOIN", null, null);
    }

    public MemberJoinMessage(Long boardId, String username, Long userId, String email, String message) {
        super("MEMBER_JOIN", boardId, username);
        this.userId = userId;
        this.email = email;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

