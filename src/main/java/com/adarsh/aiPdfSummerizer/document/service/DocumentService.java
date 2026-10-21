package com.adarsh.aiPdfSummerizer.document.service;

import com.adarsh.aiPdfSummerizer.common.exception.BadRequestException;
import com.adarsh.aiPdfSummerizer.common.exception.ResourceNotFoundException;
import com.adarsh.aiPdfSummerizer.document.entity.Document;
import com.adarsh.aiPdfSummerizer.document.repository.DocumentRepository;
import com.adarsh.aiPdfSummerizer.embedding.service.EmbeddingIndexService;
import com.adarsh.aiPdfSummerizer.user.entity.User;
import com.adarsh.aiPdfSummerizer.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final EmbeddingIndexService embeddingIndexService;

    private static final String UPLOAD_DIR = "uploads/";

    public Document uploadAndExtract(MultipartFile file, String uploaderEmail) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new BadRequestException("Only PDF files are supported");
        }

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", uploaderEmail));

        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));

            String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Path.of(UPLOAD_DIR, uniqueName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String extractedText = extractText(targetPath);

            Document document = new Document();
            document.setFileName(file.getOriginalFilename());
            document.setStoredPath(targetPath.toString());
            document.setExtractedText(extractedText);
            document.setUploadedBy(uploader);

            Document savedDocument = documentRepository.save(document);
            embeddingIndexService.indexDocument(savedDocument.getId()); // pass ID, not entity
            
            return savedDocument;

        } catch (IOException e) {
            throw new RuntimeException("Failed to process uploaded file: " + e.getMessage(), e);
        }
    }

    private String extractText(Path filePath) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }

    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    public java.util.List<Document> getUserDocuments(String email) {
        return documentRepository.findByUploadedByEmailOrderByIdDesc(email);
    }
}
