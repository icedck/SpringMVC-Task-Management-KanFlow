package com.codegym.kanflow.dto;

import java.util.List;

public class AttachmentUpdateMessage extends WebSocketMessage {
    private Long cardId;
    private List<AttachmentDto> attachments;
    private String action; // "upload", "delete"
    private String fileName;

    public AttachmentUpdateMessage() {
        super("ATTACHMENT_UPDATE", null, null);
    }

    public AttachmentUpdateMessage(Long boardId, String username, Long cardId, List<AttachmentDto> attachments, String action, String fileName) {
        super("ATTACHMENT_UPDATE", boardId, username);
        this.cardId = cardId;
        this.attachments = attachments;
        this.action = action;
        this.fileName = fileName;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public List<AttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDto> attachments) {
        this.attachments = attachments;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

