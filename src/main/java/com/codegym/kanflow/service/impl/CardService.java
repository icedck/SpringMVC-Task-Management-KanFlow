package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.repository.CardListRepository;
import com.codegym.kanflow.repository.CardRepository;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class CardService implements ICardService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardListRepository cardListRepository;

    @Override
    public Card save(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public Card findById(Long id) {
        return cardRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        cardRepository.deleteById(id);
    }

    @Override
    @Transactional // Rất quan trọng vì chúng ta cập nhật nhiều đối tượng
    public void updatePositions(List<Long> cardIds) {
        for (int i = 0; i < cardIds.size(); i++) {
            Long cardId = cardIds.get(i);
            int newPosition = i;

            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));

            card.setPosition(newPosition);

            // **SỬA LỖI:** Thêm lời gọi save() tường minh
            cardRepository.save(card);
        }
    }

    @Override
    @Transactional
    public void move(Long cardId, Long targetListId, int newPosition) {
        // --- BƯỚC 1: LẤY CÁC ĐỐI TƯỢNG CẦN THIẾT TỪ CSDL ---
        Card cardToMove = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));

        CardList targetList = cardListRepository.findById(targetListId)
                .orElseThrow(() -> new EntityNotFoundException("Target List not found with id: " + targetListId));

        // **SỬA LỖI:** DÒNG BỊ THIẾU ĐÃ ĐƯỢC THÊM LẠI
        CardList sourceList = cardToMove.getCardList();

        // Khai báo list chứa các card của list nguồn
        List<Card> sourceCards = null;

        // --- BƯỚC 2: XỬ LÝ LIST NGUỒN (NẾU CÓ) ---
        // Nếu card được kéo ra khỏi một list (sourceList khác null)
        if (sourceList != null) {
            // Lấy tất cả các card từ list nguồn
            sourceCards = cardRepository.findByCardListOrderByPositionAsc(sourceList);
            // Loại bỏ card đang được di chuyển ra khỏi danh sách
            sourceCards.remove(cardToMove);
            // Cập nhật lại vị trí cho các card còn lại trong list nguồn
            reorderPositions(sourceCards);
        }

        // --- BƯỚC 3: XỬ LÝ LIST ĐÍCH ---
        // Lấy tất cả các card trong list đích
        List<Card> targetCards = cardRepository.findByCardListOrderByPositionAsc(targetList);

        // Chèn card được di chuyển vào vị trí mới trong danh sách của list đích
        if (newPosition < 0) newPosition = 0;
        if (newPosition > targetCards.size()) newPosition = targetCards.size();
        targetCards.add(newPosition, cardToMove);

        // Cập nhật lại vị trí cho tất cả các card trong list đích
        reorderPositions(targetCards);

        // --- BƯỚC 4: CẬP NHẬT QUAN HỆ VÀ LƯU VÀO CSDL ---
        // Cập nhật lại quan hệ của card được di chuyển
        cardToMove.setCardList(targetList);

        // Lưu tất cả các thay đổi của list đích (bao gồm cả card được chuyển đến)
        cardRepository.saveAll(targetCards);

        // Chỉ lưu lại list nguồn nếu nó tồn tại và khác với list đích
        if (sourceList != null && !sourceList.getId().equals(targetList.getId())) {
            cardRepository.saveAll(sourceCards);
        }
    }

    /**
     * Một hàm helper để cập nhật lại thuộc tính 'position' của một danh sách các card
     * dựa trên thứ tự của chúng trong danh sách đó.
     */
    private void reorderPositions(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }
    }
}