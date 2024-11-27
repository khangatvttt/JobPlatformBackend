package com.jobplatform.controllers;

import com.jobplatform.models.CvFile;
import com.jobplatform.services.CvFileService;
import com.jobplatform.services.FirebaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/")
public class ResourceController {

    private final FirebaseService firebaseService;
    private final CvFileService cvFileService;

    public ResourceController(FirebaseService firebaseService, CvFileService cvFileService) {
        this.firebaseService = firebaseService;
        this.cvFileService = cvFileService;
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String fileLink = firebaseService.uploadImageToFirebase(multipartFile);
        return new ResponseEntity<>(fileLink, HttpStatus.OK);
    }

    @PostMapping("/uploadedCv")
    public ResponseEntity<CvFile> uploadCv(@RequestParam("file") MultipartFile multipartFile,
                                           @RequestParam("userId") Long userId) {
        CvFile cvFile = firebaseService.uploadCv(multipartFile, userId);
        return new ResponseEntity<>(cvFile, HttpStatus.OK);
    }

    @GetMapping("/uploadedCv/{id}")
    public ResponseEntity<List<CvFile>> getCvFiles(@PathVariable Long id) {
        List<CvFile> files = cvFileService.getAllCvFileByUser(id);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @DeleteMapping("/uploadedCv/{fileId}")
    public ResponseEntity<String> deleteCvFile(@PathVariable String fileId) {
        boolean success = firebaseService.deleteFile(fileId);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
