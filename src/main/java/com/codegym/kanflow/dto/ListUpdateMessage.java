package com.codegym.kanflow.dto;

public class ListUpdateMessage extends WebSocketMessage {
    private Long listId;
    private String title;
    private int position;

    public ListUpdateMessage() {
        super("LIST_UPDATE", null, null);
    }

    public ListUpdateMessage(Long boardId, String username, Long listId, String title, int position) {
        super("LIST_UPDATE", boardId, username);
        this.listId = listId;
        this.title = title;
        this.position = position;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

