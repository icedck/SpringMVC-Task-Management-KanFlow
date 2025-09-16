package com.codegym.kanflow.dto;

public class CardMoveMessage extends WebSocketMessage {
    private Long cardId;
    private Long fromListId;
    private Long toListId;
    private int newPosition;
    private String cardTitle;

    public CardMoveMessage() {
        super("CARD_MOVE", null, null);
    }

    public CardMoveMessage(Long boardId, String username, Long cardId, Long fromListId, 
                          Long toListId, int newPosition, String cardTitle) {
        super("CARD_MOVE", boardId, username);
        this.cardId = cardId;
        this.fromListId = fromListId;
        this.toListId = toListId;
        this.newPosition = newPosition;
        this.cardTitle = cardTitle;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Long getFromListId() {
        return fromListId;
    }

    public void setFromListId(Long fromListId) {
        this.fromListId = fromListId;
    }

    public Long getToListId() {
        return toListId;
    }

    public void setToListId(Long toListId) {
        this.toListId = toListId;
    }

    public int getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int newPosition) {
        this.newPosition = newPosition;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }
}

