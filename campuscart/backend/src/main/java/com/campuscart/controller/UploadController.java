package com.campuscart.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.campuscart.service.FileStorageService;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final FileStorageService storage;

    public UploadController(FileStorageService storage) { this.storage = storage; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        return Map.of("url", storage.saveImage(file));
    }
}
