package com.jobplatform.services;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.jobplatform.models.CvFile;
import com.jobplatform.models.UserAccount;
import com.jobplatform.repositories.CvFileRepository;
import com.jobplatform.repositories.UserRepository;
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
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FirebaseService {

    private final UserRepository userRepository;
    private final CvFileRepository cvFileRepository;

    private static final List<String> allowedTypes = Arrays.asList(
            "application/pdf", // PDF format
            "application/msword", // Microsoft Word (.doc)
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // Microsoft Word (.docx)
    );

    private static final List<String> allowedImageTypes = Arrays.asList(
            "image/jpeg",  // JPEG format
            "image/png",   // PNG format
            "image/gif",   // GIF format
            "image/bmp",   // BMP format
            "image/webp"   // WEBP format
    );
    @Value("${firebase.bucket}")
    private String firebaseBucket;

    public FirebaseService(UserRepository userRepository, CvFileRepository cvFileRepository) {
        this.userRepository = userRepository;
        this.cvFileRepository = cvFileRepository;
    }

    private String uploadFileToFirebase(MultipartFile file, String fileName) throws IOException {
        BlobId blobId = BlobId.of(firebaseBucket, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

        InputStream credentialsStream = FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");
        Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        storage.create(blobInfo, file.getInputStream());

        String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media";
        return String.format(DOWNLOAD_URL, firebaseBucket, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    @SneakyThrows
    public String uploadImageToFirebase(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));
        fileName = "images/" + fileName;

        String fileType = multipartFile.getContentType();
        if (!allowedImageTypes.contains(fileType)) {
            throw new HttpMediaTypeNotSupportedException("File type is not accept. Only allow (.jpeg, .png, .gif, .bmp, .webp)");
        }

        return uploadFileToFirebase(multipartFile, fileName);
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @SneakyThrows
    public CvFile uploadCv(MultipartFile multipartFile, Long userId) {
        String fileType = multipartFile.getContentType();
        if (!allowedTypes.contains(fileType)) {
            throw new HttpMediaTypeNotSupportedException("File type is not accept. Only allow (.pdf, .doc, .docx)");
        }
        CvFile cvFile = new CvFile();
        String fileName = multipartFile.getOriginalFilename();
        cvFile.setCvName(fileName);
        cvFile.setUploadedAt(LocalDateTime.now());
        UserAccount currentUser = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getRole() == UserAccount.Role.ROLE_JOB_SEEKER) {
            cvFile.setUser(currentUser);
        } else {
            UserAccount user = userRepository.findById(userId).orElseThrow(
                    () -> new NoSuchElementException("User with id " + userId + "is not found"));
            cvFile.setUser(user);
        }
        fileName = "cvs/" + UUID.randomUUID().toString().concat(this.getExtension(fileName));

        String urlFile = uploadFileToFirebase(multipartFile, fileName);
        cvFile.setCvUrl(urlFile);
        return cvFileRepository.save(cvFile);
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

    public void deleteCvFile(Long cvFileId) {
        CvFile cvFile = cvFileRepository.findById(cvFileId).orElseThrow(() -> new NoSuchElementException("Cv with id " + cvFileId + "is not found"));
        checkPermission(cvFile.getUser().getId());
        deleteFile(cvFile.getCvUrl());

        cvFileRepository.delete(cvFile);

    }


    @SneakyThrows
    private boolean deleteFile(String url) {
        String[] parts = url.split("/o/");
        String fileName = parts[1].split("\\?")[0];
        fileName = fileName.replace("%2F", "/");
        BlobId blobId = BlobId.of(firebaseBucket, fileName);
        InputStream credentialsStream = FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");
        Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        return storage.delete(blobId);
    }


    @SneakyThrows
    private void checkPermission(Long resoureOwnerId){
        UserAccount currentUser = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getRole()!= UserAccount.Role.ROLE_ADMIN && !Objects.equals(currentUser.getId(), resoureOwnerId)){
            throw new NoPermissionException();
        }
    }


}

