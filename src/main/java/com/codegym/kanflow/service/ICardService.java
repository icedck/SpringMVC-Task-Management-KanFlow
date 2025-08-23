package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.User;

import java.util.List;

public interface ICardService {
    Card save(Card card);

    Card findById(Long id);

    Card findByIdWithDetails(Long id);

    void deleteById(Long id);

    void move(Card cardToMove, CardList targetList, int newPosition);

    void assignMember(Long cardId, Long userId);

    void unassignMember(Long cardId, Long userId);

    List<Card> findAllByAssignee(User user);

    void assignLabel(Long cardId, Long labelId);

    void unassignLabel(Long cardId, Long labelId);
}