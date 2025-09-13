package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.AttachmentDto;
import com.codegym.kanflow.model.Attachment;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.service.IAttachmentService;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentApiController {

    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private ICardService cardService;
    @Autowired
    private IBoardService boardService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("cardId") Long cardId) {
        // Lấy thông tin user từ JWT authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }

        // Lưu file
        Attachment attachment = attachmentService.storeFile(file, cardId);

        // Tạo DTO để trả về cho frontend
        AttachmentDto dto = new AttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setUrl("/attachments/" + attachment.getStoredFileName());

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long attachmentId) {
        attachmentService.deleteFile(attachmentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}