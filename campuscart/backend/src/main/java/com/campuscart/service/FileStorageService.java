package com.campuscart.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.campuscart.exception.ApiException;

/** Saves uploaded photos to disk. The file type is verified from the file's own bytes, not its name. */
@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload folder " + root, e);
        }
    }

    public Path root() { return root; }

    /** Returns the public path of the saved photo, for example /uploads/3f2c....jpg */
    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw ApiException.badRequest("Choose a photo to upload");
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw ApiException.badRequest("Could not read that file");
        }
        String ext = detectExtension(bytes);
        if (ext == null) throw ApiException.badRequest("Only JPG, PNG or WebP photos are allowed");
        String name = UUID.randomUUID() + "." + ext;
        try {
            Files.write(root.resolve(name), bytes);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the photo");
        }
        return "/uploads/" + name;
    }

    public static String detectExtension(byte[] b) {
        if (b == null) return null;
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        return null;
    }
}
