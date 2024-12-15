package com.jobplatform.controllers;

import com.jobplatform.models.Cv;
import com.jobplatform.models.dto.CvDto;
import com.jobplatform.services.CvService;
import com.jobplatform.services.GeminiAIService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cvs")
public class CvController {
    private final CvService cvService;
    private final GeminiAIService geminiAIService;

    public CvController(CvService cvService, GeminiAIService geminiAIService) {
        this.cvService = cvService;
        this.geminiAIService = geminiAIService;
    }

    @PostMapping("")
    public ResponseEntity<Cv> createCv(@RequestBody @Valid Cv cv) {
        Cv createdCv = cvService.createCv(cv);
        return new ResponseEntity<>(createdCv, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<Cv>> getCvs(@RequestParam(required = false) Long userId,
                                           @RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        Page<Cv> listPageCv = cvService.getCvs(userId, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(listPageCv.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(listPageCv.getTotalElements()));
        return new ResponseEntity<>(listPageCv.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cv> getCv(@PathVariable Long id) {
        Cv cv = cvService.getCv(id);
        return new ResponseEntity<>(cv, HttpStatus.OK);
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

    @GetMapping("/{id}/evaluation")
    public ResponseEntity<Map<String, Object>> evaluateCv(@PathVariable Long id) {
        return new ResponseEntity<>(geminiAIService.analyzeCv(id),HttpStatus.OK);
    }
}
