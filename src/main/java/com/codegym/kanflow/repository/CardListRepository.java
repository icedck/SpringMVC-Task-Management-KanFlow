package com.codegym.kanflow.repository;

import com.codegym.kanflow.model.CardList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardListRepository extends JpaRepository<CardList, Long> {
    @Query("SELECT cl FROM CardList cl JOIN FETCH cl.board WHERE cl.id = :id")
    Optional<CardList> findByIdWithBoard(@Param("id") Long id);
}