package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.Label;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.CardListRepository;
import com.codegym.kanflow.repository.CardRepository;
import com.codegym.kanflow.repository.LabelRepository;
import com.codegym.kanflow.repository.UserRepository;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CardService implements ICardService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardListRepository cardListRepository;

    @Autowired
    private LabelRepository labelRepository;


    @Override
    public Card save(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public Card findById(Long id) {
        return cardRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Card findByIdWithDetails(Long id) {
        return cardRepository.findByIdWithDetails(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        cardRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void move(Card cardToMoveParam, CardList targetListParam, int newPosition) {
        Card cardToMove = cardRepository.findByIdWithDetails(cardToMoveParam.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card to move not found"));

        CardList targetList = cardListRepository.findByIdWithBoard(targetListParam.getId())
                .orElseThrow(() -> new EntityNotFoundException("Target list not found"));

        CardList sourceList = cardToMove.getCardList();

        if (sourceList != null) {
            sourceList.getCards().remove(cardToMove);
            reorderPositions(sourceList.getCards());
        }

        List<Card> targetCards = targetList.getCards();
        if (newPosition < 0) newPosition = 0;
        if (newPosition > targetCards.size()) newPosition = targetCards.size();

        targetCards.add(newPosition, cardToMove);
        reorderPositions(targetCards);

        cardToMove.setCardList(targetList);
    }

    private void reorderPositions(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }
    }


    @Override
    @Transactional
    public void assignMember(Long cardId, Long userId) {
        Card card = cardRepository.findByIdWithDetails(cardId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (card != null && user != null && !card.getAssignees().contains(user)) {
            card.getAssignees().add(user);
        }
    }

    @Override
    @Transactional
    public void unassignMember(Long cardId, Long userId) {
        Card card = cardRepository.findByIdWithDetails(cardId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (card != null && user != null) {
            card.getAssignees().removeIf(assignee -> assignee.getId().equals(userId));
        }
    }

    @Override
    public List<Card> findAllByAssignee(User user) {
        return cardRepository.findAllByAssignee(user);
    }

    @Override
    @Transactional
    public void assignLabel(Long cardId, Long labelId) {
        Card card = findByIdWithDetails(cardId);
        Label label = labelRepository.findById(labelId).orElse(null);
        if (card != null && label != null) {
            card.getLabels().add(label);
        }
    }

    @Override
    @Transactional
    public void unassignLabel(Long cardId, Long labelId) {
        Card card = findByIdWithDetails(cardId);
        if (card != null) {
            card.getLabels().removeIf(label -> label.getId().equals(labelId));
        }
    }
}