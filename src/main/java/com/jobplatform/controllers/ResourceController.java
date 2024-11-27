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

    @GetMapping("/uploadedCv")
    public ResponseEntity<List<CvFile>> getCvFiles(@RequestParam Long userId) {
        List<CvFile> files = cvFileService.getAllCvFileByUser(userId);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @GetMapping("/uploadedCv/{id}")
    public ResponseEntity<CvFile> getCVFile(@PathVariable Long id) {
        CvFile file = cvFileService.getCvFile(id);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @DeleteMapping("/uploadedCv/{id}")
    public ResponseEntity<String> deleteCvFile(@PathVariable Long id) {
        firebaseService.deleteCvFile(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}