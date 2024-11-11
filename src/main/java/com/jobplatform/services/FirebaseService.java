package com.jobplatform.services;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.jobplatform.models.UserAccount;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FirebaseService {

    private static final List<String> allowedTypes = Arrays.asList(
            "application/pdf", // PDF format
            "application/msword", // Microsoft Word (.doc)
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // Microsoft Word (.docx)
    );
    @Value("${firebase.bucket}")
    private String firebaseBucket;

    private String uploadFileToFirebase(InputStream inputStream, String fileName) throws IOException {
        BlobId blobId = BlobId.of(firebaseBucket, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();

        InputStream credentialsStream = FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");
        Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        storage.create(blobInfo, inputStream);

        String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media";
        return String.format(DOWNLOAD_URL, firebaseBucket, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @SneakyThrows
    public String upload(MultipartFile multipartFile) {
        String fileType = multipartFile.getContentType();
        if (!allowedTypes.contains(fileType)) {
            throw new HttpMediaTypeNotSupportedException("File type is not accept. Only allow (.pdf, .doc, .docx)");
        }

        String fileName = multipartFile.getOriginalFilename();
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));
        fileName = userAccount.getId()+"/"+fileName;
        String URL;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            URL = this.uploadFileToFirebase(inputStream, fileName);
        }

        return URL;
    }

    public boolean deleteFile(String url) {
        String[] parts = url.split("[/?]");
        String fileName = parts[parts.length - 2];
        BlobId blobId = BlobId.of(firebaseBucket, fileName);
        InputStream credentialsStream = FirebaseService.class.getClassLoader().getResourceAsStream("firebase-config.json");
        try {
            Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            return storage.delete(blobId);
        } catch (Exception e) {
            return false;
        }
    }

}

