package com.codegym.kanflow.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // Quan hệ Nhiều-1: Nhiều Board có thể thuộc về một User (chủ sở hữu).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id") // Tạo cột khóa ngoại 'owner_id' trong bảng 'boards'.
    private User owner;

    // Quan hệ 1-Nhiều: Một Board có thể chứa nhiều CardList.
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<CardList> cardLists;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public List<CardList> getCardLists() { return cardLists; }
    public void setCardLists(List<CardList> cardLists) { this.cardLists = cardLists; }
}