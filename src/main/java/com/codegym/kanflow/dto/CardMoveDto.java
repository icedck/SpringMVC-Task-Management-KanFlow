package com.codegym.kanflow.dto;

public class CardMoveDto {
    private Long targetListId;
    private int newPosition;

    public Long getTargetListId() {
        return targetListId;
    }

    public void setTargetListId(Long targetListId) {
        this.targetListId = targetListId;
    }

    public int getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int newPosition) {
        this.newPosition = newPosition;
    }
}
