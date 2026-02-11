package com.service.hotelbookingback.services.impl;

import com.service.hotelbookingback.configs.FileStorageProperties;
import com.service.hotelbookingback.enums.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Path roomImagesLocation;
    private final Path userAvatarsLocation;
    private final Path tempLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        this.roomImagesLocation = this.fileStorageLocation.resolve("rooms");
        this.userAvatarsLocation = this.fileStorageLocation.resolve("avatars");
        this.tempLocation = this.fileStorageLocation.resolve("temp");

        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.roomImagesLocation);
            Files.createDirectories(this.userAvatarsLocation);
            Files.createDirectories(this.tempLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directories", ex);
        }
    }

    // Store room image
    public String storeRoomImage(MultipartFile file) {
        return storeFile(file, roomImagesLocation, "room");
    }

    // Store user avatar
    public String storeUserAvatar(MultipartFile file) {
        return storeFile(file, userAvatarsLocation, "avatar");
    }

    private String storeFile(MultipartFile file, Path targetLocation, String prefix) {
        // Validate file
        validateFile(file);
        try {
            // Generate unique filename
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String fileName = prefix + "_" + UUID.randomUUID().toString() + fileExtension;

            // Copy file to target location
            Path targetPath = targetLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    // Store multiple room images
    public List<String> storeRoomImages(MultipartFile[] files) {
        List<String> fileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            fileNames.add(storeRoomImage(file));
        }
        return fileNames;
    }

    // Load file as resource
    public Resource loadFileAsResource(String fileName, ImageType imageType) {
        try {
            Path filePath = getFilePath(fileName, imageType);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    // Delete file
    public void deleteFile(String fileName, ImageType imageType) {
        try {
            Path filePath = getFilePath(fileName, imageType);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + fileName, ex);
        }
    }

    // Delete multiple files
    public void deleteFiles(List<String> fileNames, ImageType imageType) {
        for (String fileName : fileNames) {
            deleteFile(fileName, imageType);
        }
    }

    // Get file path
    private Path getFilePath(String fileName, ImageType imageType) {
        switch (imageType) {
            case ROOM_IMAGE:
                return roomImagesLocation.resolve(fileName).normalize();
            case USER_AVATAR:
                return userAvatarsLocation.resolve(fileName).normalize();
            default:
                throw new IllegalArgumentException("Invalid image type: " + imageType);
        }
    }

    // Validate file
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"))) {
            throw new RuntimeException("Only JPG, PNG, GIF, and WebP images are allowed");
        }

        // Clean filename
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Check for path traversal
        if (fileName.contains("..")) {
            throw new RuntimeException("Filename contains invalid path sequence: " + fileName);
        }
    }

    // Get file extension
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

    // Compress image
    public String compressAndStoreImage(MultipartFile file, ImageType imageType, int maxWidth, int maxHeight) {
        try {
            // Convert MultipartFile to BufferedImage
            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            // Calculate new dimensions
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int newWidth = originalWidth;
            int newHeight = originalHeight;

            if (originalWidth > maxWidth) {
                newWidth = maxWidth;
                newHeight = (newWidth * originalHeight) / originalWidth;
            }

            if (newHeight > maxHeight) {
                newHeight = maxHeight;
                newWidth = (newHeight * originalWidth) / originalHeight;
            }

            // Create compressed image
            BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            compressedImage.createGraphics().drawImage(
                    originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
                    0, 0, null
            );

            // Generate filename
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String fileName = (imageType == ImageType.ROOM_IMAGE ? "room_" : "avatar_")
                    + UUID.randomUUID().toString() + fileExtension;

            // Determine target directory
            Path targetLocation = imageType == ImageType.ROOM_IMAGE ? roomImagesLocation : userAvatarsLocation;

            // Save compressed image
            Path targetPath = targetLocation.resolve(fileName);
            ImageIO.write(compressedImage, getImageFormat(fileExtension), targetPath.toFile());

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not compress and store image", ex);
        }
    }

    private String getImageFormat(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "jpeg";
            case ".png":
                return "png";
            case ".gif":
                return "gif";
            case ".webp":
                return "webp";
            default:
                return "jpeg";
        }
    }
}