package com.codegym.kanflow.dto;

import java.util.List;

public class ListMoveMessage extends WebSocketMessage {
    private List<Long> listOrder;
    private String movedListTitle;
    private int newPosition;

    public ListMoveMessage() {
        super("LIST_MOVE", null, null);
    }

    public ListMoveMessage(Long boardId, String username, List<Long> listOrder, String movedListTitle, int newPosition) {
        super("LIST_MOVE", boardId, username);
        this.listOrder = listOrder;
        this.movedListTitle = movedListTitle;
        this.newPosition = newPosition;
    }

    public List<Long> getListOrder() {
        return listOrder;
    }

    public void setListOrder(List<Long> listOrder) {
        this.listOrder = listOrder;
    }

    public String getMovedListTitle() {
        return movedListTitle;
    }

    public void setMovedListTitle(String movedListTitle) {
        this.movedListTitle = movedListTitle;
    }

    public int getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int newPosition) {
        this.newPosition = newPosition;
    }
}

