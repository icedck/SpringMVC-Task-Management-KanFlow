package com.codegym.kanflow.dto;

public class CursorPositionMessage extends WebSocketMessage {
    private int x;
    private int y;
    private String element; // "card", "list", "board"

    public CursorPositionMessage() {
        super("CURSOR_POSITION", null, null);
    }

    public CursorPositionMessage(Long boardId, String username, int x, int y, String element) {
        super("CURSOR_POSITION", boardId, username);
        this.x = x;
        this.y = y;
        this.element = element;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }
}

