package com.jobplatform.services;

import com.jobplatform.models.Cv;
import com.jobplatform.models.CvFile;
import com.jobplatform.repositories.CvFileRepository;
import com.jobplatform.repositories.CvRepository;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class GeminiAIService {

    private final RestTemplate restTemplate;
    @Value("${spring.gemini-api-key}")
    private String apiKey;
    private final CvRepository cvRepository;
    private final CvFileRepository cvFileRepository;

    public GeminiAIService(CvRepository cvRepository, CvFileRepository cvFileRepository) {
        this.cvFileRepository = cvFileRepository;
        this.restTemplate = new RestTemplate();
        this.cvRepository = cvRepository;
    }

    public Map<String, Object> analyzeCv(Long cvId) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;
        Cv cv = cvRepository.findById(cvId).orElseThrow(()-> new NoSuchElementException("CV is not found"));
        String fommattedCv = formatCv(cv);
        // Prepare request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", "You are a professional HR assistant. Always respond in Vietnamese. Analyze and evaluate this CV in Vietnamese:\n" + fommattedCv)
                })
        });

        // Convert payload to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        // Make the POST request
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            // Deserialize JSON string into a Map
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call API", e);
        }
    }

    public Map<String, Object> analyzeCvFile(Long cvId) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;
        CvFile cvFile = cvFileRepository.findById(cvId).orElseThrow(()-> new NoSuchElementException("CV file not found"));
        String cvContent = readCvFile(cvFile.getCvUrl());
        // Prepare request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", "You are a professional HR assistant. Always respond in Vietnamese. Analyze and evaluate this CV in Vietnamese:\n" + cvContent)
                })
        });

        // Convert payload to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        // Make the POST request
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            // Deserialize JSON string into a Map
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call API", e);
        }
    }

    @SneakyThrows
    public String readCvFile(String pdfUrl) {
        URL url = new URL(pdfUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream inputStream = connection.getInputStream();
        PDDocument document = PDDocument.load(inputStream);

        PDFTextStripper stripper = new PDFTextStripper();
        String pdfContent = stripper.getText(document);

        return pdfContent;
    }

    private String formatCv(Cv cv) {
        StringBuilder sb = new StringBuilder();
        sb.append("Job Position: ").append(cv.getJobPosition()).append("\n");
        sb.append("Full Name: ").append(cv.getFullName()).append("\n");
        sb.append("Phone: ").append(cv.getPhone()).append("\n");
        sb.append("Email: ").append(cv.getEmail()).append("\n");
        sb.append("Address: ").append(cv.getAddress() != null ? cv.getAddress() : "N/A").append("\n");
        sb.append("Education: ").append(cv.getEducation() != null ? cv.getEducation() : "N/A").append("\n");
        sb.append("Work Experience: ").append(cv.getWorkExperience() != null ? cv.getWorkExperience() : "N/A").append("\n");
        sb.append("Skills: ").append(cv.getSkills() != null ? cv.getSkills() : "N/A").append("\n");
        sb.append("Certifications: ").append(cv.getCertifications() != null ? cv.getCertifications() : "N/A").append("\n");
        sb.append("Languages: ").append(cv.getLanguageSkill() != null ? cv.getLanguageSkill() : "N/A").append("\n");
        sb.append("Hobbies: ").append(cv.getHobby() != null ? cv.getHobby() : "N/A").append("\n");
        sb.append("Portfolio: ").append(cv.getPortfolio() != null ? cv.getPortfolio() : "N/A").append("\n");
        sb.append("Status: ").append(cv.getStatus() != null ? (cv.getStatus() ? "Active" : "Inactive") : "N/A").append("\n");
        return sb.toString();
    }
}

