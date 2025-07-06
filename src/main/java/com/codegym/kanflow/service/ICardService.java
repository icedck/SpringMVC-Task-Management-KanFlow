package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Card;

import java.util.List;

public interface ICardService {
    Card save(Card card);

    Card findById(Long id);

    void deleteById(Long id);

    void updatePositions(List<Long> cardIds);

    void move(Long cardId, Long targetListId, int newPosition);
}