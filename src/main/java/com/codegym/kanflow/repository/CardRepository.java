package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    /**
     * Tìm tất cả các Card thuộc về một CardList và sắp xếp chúng theo vị trí tăng dần.
     */
    List<Card> findByCardListOrderByPositionAsc(CardList cardList); // <-- THÊM DÒNG NÀY

    @Query("SELECT DISTINCT c FROM Card c " +
            "LEFT JOIN FETCH c.cardList cl " +
            "LEFT JOIN FETCH cl.board " +
            "LEFT JOIN FETCH c.assignees " + // <-- THÊM DÒNG NÀY
            "WHERE c.id = :id")
    Optional<Card> findByIdWithDetails(@Param("id") Long id);
}
