package com.codegym.kanflow.dto;

import java.util.List;
import java.util.Set;

public class CardUpdateMessage extends WebSocketMessage {
    private Long cardId;
    private String title;
    private String description;
    private List<UserDto> assignees;
    private Set<LabelDto> labels;

    public CardUpdateMessage() {
        super("CARD_UPDATE", null, null);
    }

    public CardUpdateMessage(Long boardId, String username, Long cardId, String title, String description, 
                           List<UserDto> assignees, Set<LabelDto> labels) {
        super("CARD_UPDATE", boardId, username);
        this.cardId = cardId;
        this.title = title;
        this.description = description;
        this.assignees = assignees;
        this.labels = labels;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UserDto> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<UserDto> assignees) {
        this.assignees = assignees;
    }

    public Set<LabelDto> getLabels() {
        return labels;
    }

    public void setLabels(Set<LabelDto> labels) {
        this.labels = labels;
    }
}

