package com.codegym.kanflow.dto;

public class CardDeleteMessage extends WebSocketMessage {
    private Long cardId;
    private String cardTitle;
    private Long listId;

    public CardDeleteMessage() {
        super("CARD_DELETE", null, null);
    }

    public CardDeleteMessage(Long boardId, String username, Long cardId, String cardTitle, Long listId) {
        super("CARD_DELETE", boardId, username);
        this.cardId = cardId;
        this.cardTitle = cardTitle;
        this.listId = listId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }
}

