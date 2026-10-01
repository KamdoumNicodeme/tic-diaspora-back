package com.ticdiaspora.infrastructure.adapter.out.client;

import com.ticdiaspora.application.port.out.PaymentProofStoragePort;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalPaymentProofStorageAdapter implements PaymentProofStoragePort {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".png", ".jpg", ".jpeg", ".webp");
    private final Path storageDirectory;

    public LocalPaymentProofStorageAdapter(@Value("${app.uploads.contribution-proofs-dir:./uploads/contribution-proofs}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredPaymentProof store(String originalFilename, String contentType, InputStream content) {
        try {
            Files.createDirectories(storageDirectory);
            String extension = safeExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;
            Path target = storageDirectory.resolve(storedFilename).normalize();
            if (!target.startsWith(storageDirectory)) {
                throw new BusinessException("INVALID_PROOF_PATH", "Chemin de preuve invalide");
            }
            long size = Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredPaymentProof(storedFilename, "/contributions/proofs/" + storedFilename,
                    contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType, size);
        } catch (IOException exception) {
            throw new BusinessException("PAYMENT_PROOF_UPLOAD_FAILED", "Impossible d'enregistrer la preuve de paiement");
        }
    }

    @Override
    public PaymentProofFile load(String storedFilename) {
        Path file = storageDirectory.resolve(storedFilename).normalize();
        if (!file.startsWith(storageDirectory) || !Files.exists(file)) {
            throw new NotFoundException("Preuve de paiement", storedFilename);
        }
        try {
            String contentType = Files.probeContentType(file);
            return new PaymentProofFile(storedFilename,
                    contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType,
                    Files.readAllBytes(file));
        } catch (IOException exception) {
            throw new BusinessException("PAYMENT_PROOF_READ_FAILED", "Impossible de lire la preuve de paiement");
        }
    }

    private String safeExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
            return ".bin";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(extension) ? extension : ".bin";
    }
}
