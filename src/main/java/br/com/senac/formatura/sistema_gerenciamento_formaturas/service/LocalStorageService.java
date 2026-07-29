package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocalStorageService implements StorageService {
    private final Path rootPath;

    public LocalStorageService(@Value("${app.storage.local-path:${user.home}/.formaly-business/uploads}") String localPath) {
        this.rootPath = Path.of(localPath).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(MultipartFile file, String namespace, Collection<String> allowedExtensions, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo obrigatorio.");
        }
        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo maior que o limite permitido.");
        }

        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        Set<String> allowed = allowedExtensions.stream()
            .map(item -> item.toLowerCase(Locale.ROOT).replace(".", "").trim())
            .filter(item -> !item.isBlank())
            .collect(Collectors.toSet());
        if (!allowed.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extensao nao permitida para este documento.");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        String safeNamespace = sanitizePathPart(namespace);
        Path targetDir = rootPath.resolve(safeNamespace).normalize();
        Path target = targetDir.resolve(storedName).normalize();
        if (!target.startsWith(rootPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho de armazenamento invalido.");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel armazenar o arquivo.");
        }

        return new StoredFile(
            originalName,
            storedName,
            rootPath.relativize(target).toString().replace('\\', '/'),
            file.getSize(),
            file.getContentType()
        );
    }

    private String sanitizeOriginalName(String value) {
        String name = value == null ? "arquivo" : Path.of(value).getFileName().toString();
        return name.replaceAll("[\\r\\n]", "").trim();
    }

    private String sanitizePathPart(String value) {
        String normalized = Normalizer.normalize(value == null ? "geral" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9._-]", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");
        return normalized.isBlank() ? "geral" : normalized;
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
