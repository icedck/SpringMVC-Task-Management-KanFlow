package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Attachment;
import org.springframework.web.multipart.MultipartFile;

public interface IAttachmentService {
    Attachment storeFile(MultipartFile file, Long cardId);
    void deleteFile(Long attachmentId);
    Attachment findById(Long attachmentId);
}