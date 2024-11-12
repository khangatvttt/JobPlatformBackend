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

import javax.naming.NoPermissionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        String contentType;
        if (getExtension(fileName).equals("pdf")){
            contentType = "application/pdf";
        }
        else {
            contentType = "application/msword";
        }
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

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

    public List<String> getFilesByUserId(Long userId) {
        List<String> fileNames = new ArrayList<>();
        InputStream credentialsStream = FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");

        try {
            Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            Bucket bucket = storage.get(firebaseBucket);

            // List all files with the folder path as a prefix
            for (Blob blob : bucket.list(Storage.BlobListOption.prefix(userId.toString())).iterateAll()) {
                // Add each file name to the list
                fileNames.add(blob.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNames;
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

    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }

}
