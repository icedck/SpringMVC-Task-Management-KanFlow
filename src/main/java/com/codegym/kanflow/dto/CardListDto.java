package com.codegym.kanflow.dto;

// Lớp này dùng để truyền dữ liệu CardList dưới dạng JSON
public class CardListDto {
    private Long id;
    private String title;
    private int position;

    // Constructors
    public CardListDto() {
    }

    public CardListDto(Long id, String title, int position) {
        this.id = id;
        this.title = title;
        this.position = position;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}