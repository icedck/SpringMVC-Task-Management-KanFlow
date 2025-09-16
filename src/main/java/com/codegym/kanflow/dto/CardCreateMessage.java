package com.codegym.kanflow.dto;

public class CardCreateMessage extends WebSocketMessage {
    private Long cardId;
    private String title;
    private Long listId;
    private int position;

    public CardCreateMessage() {
        super("CARD_CREATE", null, null);
    }

    public CardCreateMessage(Long boardId, String username, Long cardId, String title, Long listId, int position) {
        super("CARD_CREATE", boardId, username);
        this.cardId = cardId;
        this.title = title;
        this.listId = listId;
        this.position = position;
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

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

