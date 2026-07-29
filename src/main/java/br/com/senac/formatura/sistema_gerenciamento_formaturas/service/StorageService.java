package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import java.util.Collection;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file, String namespace, Collection<String> allowedExtensions, long maxBytes);

    record StoredFile(
        String originalName,
        String storedName,
        String reference,
        long size,
        String mimeType
    ) {}
}
