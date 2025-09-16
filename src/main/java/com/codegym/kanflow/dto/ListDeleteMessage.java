package com.codegym.kanflow.dto;

public class ListDeleteMessage extends WebSocketMessage {
    private Long listId;
    private String title;

    public ListDeleteMessage() {
        super("LIST_DELETE", null, null);
    }

    public ListDeleteMessage(Long boardId, String username, Long listId, String title) {
        super("LIST_DELETE", boardId, username);
        this.listId = listId;
        this.title = title;
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
}

