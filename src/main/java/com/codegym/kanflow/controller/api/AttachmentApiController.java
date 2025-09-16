package com.codegym.kanflow.controller.api;

import com.amazonaws.services.s3.AmazonS3;
import com.codegym.kanflow.dto.AttachmentDto;
import com.codegym.kanflow.dto.AttachmentUpdateMessage;
import com.codegym.kanflow.model.Attachment;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.service.IAttachmentService;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.ICardService;
import com.codegym.kanflow.service.IWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentApiController {

    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private ICardService cardService;
    @Autowired
    private IBoardService boardService;
    @Autowired
    private IWebSocketService webSocketService;
    @Autowired
    private AmazonS3 s3client;
    @Value("${aws.s3.bucketName}")
    private String bucketName;


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("cardId") Long cardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Card card = cardService.findByIdWithDetails(cardId);
        if (card == null) return new ResponseEntity<>("Card not found", HttpStatus.NOT_FOUND);
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
        }

        Attachment attachment = attachmentService.storeFile(file, cardId);

        AttachmentDto dto = new AttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());

        String fileUrl = s3client.getUrl(bucketName, attachment.getStoredFileName()).toString();
        dto.setUrl(fileUrl);

        // Get all attachments for the card to send via WebSocket
        Card updatedCard = cardService.findByIdWithDetails(cardId);
        List<AttachmentDto> allAttachments = new ArrayList<>();
        for (Attachment att : updatedCard.getAttachments()) {
            AttachmentDto attDto = new AttachmentDto();
            attDto.setId(att.getId());
            attDto.setFileName(att.getFileName());
            attDto.setUrl(s3client.getUrl(bucketName, att.getStoredFileName()).toString());
            allAttachments.add(attDto);
        }

        // Send WebSocket notification
        AttachmentUpdateMessage wsMessage = new AttachmentUpdateMessage(
            updatedCard.getCardList().getBoard().getId(), 
            username, 
            cardId, 
            allAttachments, 
            "upload", 
            attachment.getFileName()
        );
        webSocketService.sendToBoard(updatedCard.getCardList().getBoard().getId(), wsMessage);

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long attachmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Get attachment details before deletion
        Attachment attachment = attachmentService.findById(attachmentId);
        if (attachment == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Card card = cardService.findByIdWithDetails(attachment.getCard().getId());
        if (!boardService.hasAccess(card.getCardList().getBoard().getId(), username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        String fileName = attachment.getFileName();
        Long cardId = card.getId();
        
        attachmentService.deleteFile(attachmentId);
        
        // Get remaining attachments for the card
        card = cardService.findByIdWithDetails(cardId);
        List<AttachmentDto> remainingAttachments = new ArrayList<>();
        for (Attachment att : card.getAttachments()) {
            AttachmentDto attDto = new AttachmentDto();
            attDto.setId(att.getId());
            attDto.setFileName(att.getFileName());
            attDto.setUrl(s3client.getUrl(bucketName, att.getStoredFileName()).toString());
            remainingAttachments.add(attDto);
        }

        // Send WebSocket notification
        AttachmentUpdateMessage wsMessage = new AttachmentUpdateMessage(
            card.getCardList().getBoard().getId(), 
            username, 
            cardId, 
            remainingAttachments, 
            "delete", 
            fileName
        );
        webSocketService.sendToBoard(card.getCardList().getBoard().getId(), wsMessage);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}