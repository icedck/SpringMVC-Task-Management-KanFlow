package com.codegym.kanflow.dto;

public class TypingMessage extends WebSocketMessage {
    private Long cardId;
    private String field; // "title" or "description"
    private boolean isTyping;

    public TypingMessage() {
        super("TYPING", null, null);
    }

    public TypingMessage(Long boardId, String username, Long cardId, String field, boolean isTyping) {
        super("TYPING", boardId, username);
        this.cardId = cardId;
        this.field = field;
        this.isTyping = isTyping;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }
}

