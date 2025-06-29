package com.codegym.kanflow.model;

import javax.persistence.*;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob // Dùng cho các chuỗi dài như mô tả
    private String description;

    private int position; // Dùng để sắp xếp thứ tự các card

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_list_id")
    private CardList cardList;

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public CardList getCardList() { return cardList; }
    public void setCardList(CardList cardList) { this.cardList = cardList; }
}