package com.ticdiaspora.application.port.out;

import java.io.InputStream;

public interface PaymentProofStoragePort {
    StoredPaymentProof store(String originalFilename, String contentType, InputStream content);

    PaymentProofFile load(String storedFilename);

    record StoredPaymentProof(String storedFilename, String url, String contentType, long size) {
    }

    record PaymentProofFile(String storedFilename, String contentType, byte[] content) {
    }
}
