package com.codegym.kanflow.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "card_lists")
public class CardList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int position; // Dùng để sắp xếp thứ tự các list

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "cardList", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<Card> cards;

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }
}