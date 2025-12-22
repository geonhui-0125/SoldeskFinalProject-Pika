package com.numlock.pika.service.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ChatMediaService {

    @Value("${file.chat-upload-path}")
    private String uploadDir;

    public String store(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originFilename = multipartFile.getOriginalFilename();
        String fileExtension = "";
        if (originFilename != null && originFilename.contains(".")) {
            fileExtension = originFilename.substring(originFilename.lastIndexOf("."));
        }
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        Path destinationFile = uploadPath.resolve(storedFileName);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to store file: " + storedFileName, e);
        }

        return "/chat-media/" + storedFileName;
    }
}
