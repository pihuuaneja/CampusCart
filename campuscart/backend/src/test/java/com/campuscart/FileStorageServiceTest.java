package com.campuscart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.campuscart.service.FileStorageService;

class FileStorageServiceTest {
    @Test
    void recognisesImagesByContent() {
        assertEquals("jpg", FileStorageService.detectExtension(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00 }));
        assertEquals("png", FileStorageService.detectExtension(
                new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A }));
        assertEquals("webp", FileStorageService.detectExtension("RIFF1234WEBPVP8 ".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void rejectsNonImages() {
        assertNull(FileStorageService.detectExtension("<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)));
        assertNull(FileStorageService.detectExtension(new byte[0]));
    }
}
