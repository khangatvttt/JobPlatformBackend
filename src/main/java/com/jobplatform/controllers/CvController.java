package com.jobplatform.controllers;

import com.jobplatform.models.Cv;
import com.jobplatform.models.dto.CvDto;
import com.jobplatform.services.CvService;
import com.jobplatform.services.FirebaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.CharBuffer;
import java.util.List;

@RestController
@RequestMapping("/cvs")
public class CvController {
    private final CvService cvService;
    private final FirebaseService firebaseService;

    public CvController(CvService cvService, FirebaseService firebaseService) {
        this.cvService = cvService;
        this.firebaseService = firebaseService;
    }

    @PostMapping("")
    public ResponseEntity<Cv> createCv(@RequestBody @Valid Cv cv) {
        Cv createdCv = cvService.createCv(cv);
        return new ResponseEntity<>(createdCv, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<Cv>> getCvs(@RequestParam(required = false) String userId) {
        List<Cv> listCv = cvService.getCvs(userId);
        return new ResponseEntity<>(listCv, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Cv> updateCv(@PathVariable Long id, @RequestBody @Valid CvDto updateCv) {
        Cv updatedCv = cvService.updateCv(id, updateCv);
        return new ResponseEntity<>(updatedCv, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Cv> deleteCv(@PathVariable Long id) {
        cvService.deleteCv(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/upload")
    public String uploadCv(@RequestParam("file") MultipartFile multipartFile) {
        try {
            return firebaseService.upload(multipartFile);
        }
        catch (Error error){
            return "Fail";
        }
    }
}
