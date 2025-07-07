package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends CrudRepository<Card, Long> {
    /**
     * Tìm tất cả các Card thuộc về một CardList và sắp xếp chúng theo vị trí tăng dần.
     */
    List<Card> findByCardListOrderByPositionAsc(CardList cardList); // <-- THÊM DÒNG NÀY

    @Query("SELECT c FROM Card c JOIN FETCH c.cardList cl JOIN FETCH cl.board WHERE c.id = :id")
    Optional<Card> findByIdWithDetails(@Param("id") Long id);
}
