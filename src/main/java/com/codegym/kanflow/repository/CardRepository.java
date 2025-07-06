package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CardRepository extends CrudRepository<Card, Long> {
    /**
     * Tìm tất cả các Card thuộc về một CardList và sắp xếp chúng theo vị trí tăng dần.
     */
    List<Card> findByCardListOrderByPositionAsc(CardList cardList); // <-- THÊM DÒNG NÀY
}
