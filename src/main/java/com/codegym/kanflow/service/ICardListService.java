package com.codegym.kanflow.service;

import com.codegym.kanflow.model.CardList;

import java.util.List;

public interface ICardListService {
    CardList save(CardList cardList);

    CardList findById(Long id);

    CardList findByIdWithBoard(Long id);

    void deleteById(Long id);

    void updatePositions(List<Long> listIds);
}