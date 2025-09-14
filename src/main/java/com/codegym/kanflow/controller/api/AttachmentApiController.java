package com.codegym.kanflow.controller.api;

import com.amazonaws.services.s3.AmazonS3;
import com.codegym.kanflow.dto.AttachmentDto;
import com.codegym.kanflow.model.Attachment;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.service.IAttachmentService;
import com.codegym.kanflow.service.IBoardService;
import com.codegym.kanflow.service.ICardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long attachmentId) {
        attachmentService.deleteFile(attachmentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}