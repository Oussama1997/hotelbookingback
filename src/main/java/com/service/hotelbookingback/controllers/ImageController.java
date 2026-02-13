package com.service.hotelbookingback.controllers;

import com.service.hotelbookingback.enums.ImageType;
import com.service.hotelbookingback.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private FileStorageService fileStorageService;

    // Serve room images
    @GetMapping("/rooms/{fileName:.+}")
    public ResponseEntity<Resource> getRoomImage(@PathVariable String fileName) {
        return serveImage(fileName, ImageType.ROOM_IMAGE);
    }

    // Serve user avatars
    @GetMapping("/avatars/{fileName:.+}")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable String fileName) {
        return serveImage(fileName, ImageType.USER_AVATAR);
    }

    private ResponseEntity<Resource> serveImage(String fileName, ImageType imageType) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName, imageType);

            String contentType = determineContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException ex) {
            // Return default image if file not found
            return getDefaultImage(imageType);
        }
    }

    private ResponseEntity<Resource> getDefaultImage(ImageType imageType) {
        String defaultImage = imageType == ImageType.ROOM_IMAGE ?
                "default-room.jpg" : "default-avatar.png";

        try {
            Resource resource = fileStorageService.loadFileAsResource(defaultImage, imageType);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}