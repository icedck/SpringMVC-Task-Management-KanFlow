package com.codegym.kanflow.dto;

public class CardDto {
    private Long id;
    private String title;
    private String description;
    private int position;

    public CardDto() {
    }

    public CardDto(Long id, String title, String description, int position) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}