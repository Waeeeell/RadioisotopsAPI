package radioisotops.api.com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public void init() {
        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar la carpeta de subidas");
        }
    }

    public String save(MultipartFile file, Long userId) {
        try {
            // Validar que sea una imagen
            if (!file.getContentType().startsWith("image/")) {
                throw new RuntimeException("El archivo debe ser una imagen");
            }

            // Generar nombre único: user_1_1712684000.jpg
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = "user_" + userId + "_" + System.currentTimeMillis() + extension;

            Files.copy(file.getInputStream(), this.root.resolve(fileName));
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }
    }
}