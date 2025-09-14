    package com.codegym.kanflow.service.impl;

    import com.amazonaws.services.s3.AmazonS3;
    import com.amazonaws.services.s3.model.CannedAccessControlList;
    import com.amazonaws.services.s3.model.ObjectMetadata;
    import com.amazonaws.services.s3.model.PutObjectRequest;
    import com.codegym.kanflow.model.Attachment;
    import com.codegym.kanflow.model.Card;
    import com.codegym.kanflow.repository.AttachmentRepository;
    import com.codegym.kanflow.repository.CardRepository;
    import com.codegym.kanflow.service.IAttachmentService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.util.StringUtils;
    import org.springframework.web.multipart.MultipartFile;

    import javax.persistence.EntityNotFoundException;
    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.util.UUID;

    @Service
    public class AttachmentService implements IAttachmentService {

        @Autowired
        private AmazonS3 s3client;

        @Value("${aws.s3.bucketName}")
        private String bucketName;

        @Autowired
        private AttachmentRepository attachmentRepository;

        @Autowired
        private CardRepository cardRepository;

        @Override
        public Attachment storeFile(MultipartFile file, Long cardId) {
            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> new EntityNotFoundException("Card not found with id " + cardId));

            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            try {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } catch (Exception e) {
                e.printStackTrace();
            }
            String storedFileName = UUID.randomUUID().toString() + fileExtension;

            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());

                // Tạo request và upload file lên S3, set quyền PublicRead
                PutObjectRequest request = new PutObjectRequest(bucketName, storedFileName, file.getInputStream(), metadata);

                s3client.putObject(request);

                Attachment attachment = new Attachment();
                attachment.setFileName(originalFileName);
                attachment.setStoredFileName(storedFileName);
                attachment.setFileType(file.getContentType());
                attachment.setUploadDate(LocalDateTime.now());
                attachment.setCard(card);

                return attachmentRepository.save(attachment);
            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
            }
        }

        @Override
        public void deleteFile(Long attachmentId) {
            Attachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id " + attachmentId));

            // Xóa file trên S3
            s3client.deleteObject(bucketName, attachment.getStoredFileName());

            // Xóa record trong database
            attachmentRepository.delete(attachment);
        }
    }