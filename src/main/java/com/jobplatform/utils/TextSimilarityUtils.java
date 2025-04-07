package com.jobplatform.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class TextSimilarityUtils {
    private static final Cache<String, List<String>> tokenCache =
                            CacheBuilder.newBuilder()
                            .maximumSize(10_000)
                            .build();

    // Cosine Similarity
    public static double cosineSimilarity(String text1, String text2) {
        if (text1 == null) text1 = "";
        if (text2 == null) text2 = "";

        List<String> tokens1 = tokenize(text1);
        List<String> tokens2 = tokenize(text2);

        Map<String, Integer> freq1 = termFrequency(tokens1);
        Map<String, Integer> freq2 = termFrequency(tokens2);

        Set<String> allTerms = new HashSet<>(freq1.keySet());
        allTerms.addAll(freq2.keySet());

        double[] vec1 = new double[allTerms.size()];
        double[] vec2 = new double[allTerms.size()];

        int i = 0;
        for (String term : allTerms) {
            vec1[i] = freq1.getOrDefault(term, 0);
            vec2[i] = freq2.getOrDefault(term, 0);
            i++;
        }

        return cosine(vec1, vec2);
    }

    // Jaccard similarity
    public static double jaccardSimilarity(String text1, String text2) {
        if (text1 == null) text1 = "";
        if (text2 == null) text2 = "";

        Set<String> set1 = new HashSet<>(tokenize(text1));
        Set<String> set2 = new HashSet<>(tokenize(text2));

        if (set1.isEmpty() || set2.isEmpty()) return 0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    // Tokenizer with cache
    private static List<String> tokenize(String text) {
        return tokenCache.getIfPresent(text) != null
                ? tokenCache.getIfPresent(text)
                : tokenCache.asMap().computeIfAbsent(text, t ->
                Arrays.stream(t.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList())
        );
    }

    private static Map<String, Integer> termFrequency(List<String> tokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String token : tokens) {
            freq.put(token, freq.getOrDefault(token, 0) + 1);
        }
        return freq;
    }

    private static double cosine(double[] vec1, double[] vec2) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
            normA += vec1[i] * vec1[i];
            normB += vec2[i] * vec2[i];
        }
        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
