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
    public Card findByIdWithDetails(Long id) {
        return cardRepository.findByIdWithDetails(id).orElse(null);
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

        CardList sourceList = cardToMove.getCardList();

        // --- BƯỚC 2: XỬ LÝ DI CHUYỂN GIỮA CÁC LIST KHÁC NHAU ---
        if (sourceList != null && !sourceList.getId().equals(targetList.getId())) {
            // Lấy các card từ list nguồn, loại bỏ card đang di chuyển và sắp xếp lại
            List<Card> sourceCards = cardRepository.findByCardListOrderByPositionAsc(sourceList);
            sourceCards.remove(cardToMove);
            reorderPositions(sourceCards);
            cardRepository.saveAll(sourceCards);
        }

        // --- BƯỚC 3: CẬP NHẬT LIST ĐÍCH ---
        // Lấy tất cả các card trong list đích
        List<Card> targetCards = cardRepository.findByCardListOrderByPositionAsc(targetList);

        // Nếu card đã có trong list đích (trường hợp sắp xếp trong cùng 1 list), hãy loại bỏ nó trước
        targetCards.remove(cardToMove);

        // Chèn card vào vị trí mới
        if (newPosition < 0) newPosition = 0;
        if (newPosition > targetCards.size()) newPosition = targetCards.size();
        targetCards.add(newPosition, cardToMove);

        // Sắp xếp lại vị trí cho toàn bộ list đích
        reorderPositions(targetCards);

        // --- BƯỚC 4: CẬP NHẬT QUAN HỆ VÀ LƯU ---
        cardToMove.setCardList(targetList);

        // Lưu lại toàn bộ list đích đã được sắp xếp lại
        cardRepository.saveAll(targetCards);
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