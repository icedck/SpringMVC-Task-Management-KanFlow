package com.codegym.kanflow.dto;

public class MemberLeaveMessage extends WebSocketMessage {
    private Long userId;
    private String message;

    public MemberLeaveMessage() {
        super("MEMBER_LEAVE", null, null);
    }

    public MemberLeaveMessage(Long boardId, String username, Long userId, String message) {
        super("MEMBER_LEAVE", boardId, username);
        this.userId = userId;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

