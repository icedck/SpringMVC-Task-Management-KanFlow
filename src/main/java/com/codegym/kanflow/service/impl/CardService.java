package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.CardList;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.CardListRepository;
import com.codegym.kanflow.repository.CardRepository;
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

    @Override
    public Card save(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public Card findById(Long id) {
        return cardRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true) // Quan trọng!
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
    public void move(Card cardToMoveParam, CardList targetListParam, int newPosition) {
        // --- BƯỚC 1: TẢI LẠI CÁC ĐỐI TƯỢNG BÊN TRONG TRANSACTION HIỆN TẠI ---
        // Điều này đảm bảo chúng ta đang làm việc với các đối tượng "managed"
        // và tất cả các collection con có thể được tải lên một cách an toàn.
        Card cardToMove = cardRepository.findByIdWithDetails(cardToMoveParam.getId())
                .orElseThrow(() -> new EntityNotFoundException("Card to move not found"));

        CardList targetList = cardListRepository.findByIdWithBoard(targetListParam.getId())
                .orElseThrow(() -> new EntityNotFoundException("Target list not found"));

        CardList sourceList = cardToMove.getCardList();

        // --- BƯỚC 2: XỬ LÝ LOGIC DI CHUYỂN ---
        // Lấy danh sách card của list nguồn (nếu có)
        if (sourceList != null) {
            // Dòng này bây giờ an toàn vì đang trong transaction
            sourceList.getCards().remove(cardToMove);
            reorderPositions(sourceList.getCards());
        }

        // Lấy danh sách card của list đích
        // Dòng này bây giờ an toàn vì đang trong transaction
        List<Card> targetCards = targetList.getCards();
        if (newPosition < 0) newPosition = 0;
        if (newPosition > targetCards.size()) newPosition = targetCards.size();

        targetCards.add(newPosition, cardToMove);
        reorderPositions(targetCards);

        // --- BƯỚC 3: CẬP NHẬT QUAN HỆ ---
        cardToMove.setCardList(targetList);

        // Transaction sẽ tự động commit các thay đổi khi phương thức kết thúc.
        // Không cần gọi save() tường minh.
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


    @Override
    @Transactional // THÊM VÀO ĐÂY
    public void assignMember(Long cardId, Long userId) {
        // findByIdWithDetails đã được fetch đầy đủ
        Card card = cardRepository.findByIdWithDetails(cardId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (card != null && user != null && !card.getAssignees().contains(user)) {
            card.getAssignees().add(user);
            // Không cần save() vì @Transactional sẽ xử lý
        }
    }

    @Override
    @Transactional // THÊM VÀO ĐÂY
    public void unassignMember(Long cardId, Long userId) {
        Card card = cardRepository.findByIdWithDetails(cardId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (card != null && user != null) {
            // Dùng removeIf cho an toàn hơn khi thao tác với collection được quản lý
            card.getAssignees().removeIf(assignee -> assignee.getId().equals(userId));
            // Không cần save() vì @Transactional sẽ xử lý
        }
    }

    @Override
    public List<Card> findAllByAssignee(User user) {
        return cardRepository.findAllByAssignee(user);
    }

}