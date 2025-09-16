package com.codegym.kanflow.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CardUpdateMessage.class, name = "CARD_UPDATE"),
    @JsonSubTypes.Type(value = CardMoveMessage.class, name = "CARD_MOVE"),
    @JsonSubTypes.Type(value = CardCreateMessage.class, name = "CARD_CREATE"),
    @JsonSubTypes.Type(value = CardDeleteMessage.class, name = "CARD_DELETE"),
    @JsonSubTypes.Type(value = ListUpdateMessage.class, name = "LIST_UPDATE"),
    @JsonSubTypes.Type(value = ListCreateMessage.class, name = "LIST_CREATE"),
    @JsonSubTypes.Type(value = ListDeleteMessage.class, name = "LIST_DELETE"),
    @JsonSubTypes.Type(value = ListMoveMessage.class, name = "LIST_MOVE"),
    @JsonSubTypes.Type(value = MemberJoinMessage.class, name = "MEMBER_JOIN"),
    @JsonSubTypes.Type(value = MemberLeaveMessage.class, name = "MEMBER_LEAVE"),
    @JsonSubTypes.Type(value = AttachmentUpdateMessage.class, name = "ATTACHMENT_UPDATE"),
    @JsonSubTypes.Type(value = TypingMessage.class, name = "TYPING"),
    @JsonSubTypes.Type(value = CursorPositionMessage.class, name = "CURSOR_POSITION"),
})
public abstract class WebSocketMessage {
    private String type;
    private Long boardId;
    private String username;
    private Long timestamp;

    public WebSocketMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public WebSocketMessage(String type, Long boardId, String username) {
        this();
        this.type = type;
        this.boardId = boardId;
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

