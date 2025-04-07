package com.jobplatform.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class NormalizerUtils {

    private static final Map<String, String> LEVEL_KEYWORDS = new LinkedHashMap<>();
    private static final Map<String, String> INDUSTRY_KEYWORDS = new LinkedHashMap<>();

    static {
        // Level normalization
        LEVEL_KEYWORDS.put("intern", "intern");
        LEVEL_KEYWORDS.put("thực tập", "intern");
        LEVEL_KEYWORDS.put("fresher", "fresher");
        LEVEL_KEYWORDS.put("entry", "junior");
        LEVEL_KEYWORDS.put("junior", "junior");
        LEVEL_KEYWORDS.put("mid", "mid");
        LEVEL_KEYWORDS.put("middle", "mid");
        LEVEL_KEYWORDS.put("senior", "senior");
        LEVEL_KEYWORDS.put("expert", "senior");
        LEVEL_KEYWORDS.put("lead", "senior");
        LEVEL_KEYWORDS.put("manager", "manager");
        LEVEL_KEYWORDS.put("head", "manager");
        LEVEL_KEYWORDS.put("boss", "manager");

        // Industry normalization
        INDUSTRY_KEYWORDS.put("kỹ thuật", "engineering");
        INDUSTRY_KEYWORDS.put("engineering", "engineering");

        INDUSTRY_KEYWORDS.put("bất động sản", "real_estate");
        INDUSTRY_KEYWORDS.put("real estate", "real_estate");
        INDUSTRY_KEYWORDS.put("property", "real_estate");

        INDUSTRY_KEYWORDS.put("công nghệ thông tin", "information_technology");
        INDUSTRY_KEYWORDS.put("it", "information_technology");
        INDUSTRY_KEYWORDS.put("technology", "information_technology");
        INDUSTRY_KEYWORDS.put("software", "information_technology");
        INDUSTRY_KEYWORDS.put("web", "information_technology");
        INDUSTRY_KEYWORDS.put("developer", "information_technology");
        INDUSTRY_KEYWORDS.put("dev", "information_technology");

        INDUSTRY_KEYWORDS.put("giáo dục", "education");
        INDUSTRY_KEYWORDS.put("giáo viên", "education");
        INDUSTRY_KEYWORDS.put("education", "education");
        INDUSTRY_KEYWORDS.put("teaching", "education");

        INDUSTRY_KEYWORDS.put("kế toán", "accounting");
        INDUSTRY_KEYWORDS.put("accounting", "accounting");
        INDUSTRY_KEYWORDS.put("finance", "accounting");

        INDUSTRY_KEYWORDS.put("marketing", "marketing");
        INDUSTRY_KEYWORDS.put("truyền thông", "marketing");
        INDUSTRY_KEYWORDS.put("quảng cáo", "marketing");
        INDUSTRY_KEYWORDS.put("advertising", "marketing");
        INDUSTRY_KEYWORDS.put("communication", "marketing");

        INDUSTRY_KEYWORDS.put("thời trang", "fashion");
        INDUSTRY_KEYWORDS.put("thiết kế", "fashion");
        INDUSTRY_KEYWORDS.put("fashion", "fashion");

        INDUSTRY_KEYWORDS.put("y tế", "healthcare");
        INDUSTRY_KEYWORDS.put("health", "healthcare");
        INDUSTRY_KEYWORDS.put("medical", "healthcare");
        INDUSTRY_KEYWORDS.put("medicine", "healthcare");

    }

    public static boolean normalizeLevel(String cvPosition, String jobLevel) {
        String normalCv = normalize(cvPosition, LEVEL_KEYWORDS);
        String normalJob = normalize(jobLevel, LEVEL_KEYWORDS);
        return normalJob != null && normalJob.equals(normalCv);
    }

    public static boolean normalizeIndustry(String cvPosition, String jobIndustry) {
        String normalCv = normalize(cvPosition, INDUSTRY_KEYWORDS);
        String normalJob = normalize(jobIndustry, INDUSTRY_KEYWORDS);
        return normalCv != null && normalCv.equals(normalJob);
    }

    private static String normalize(String input, Map<String, String> keywordMap) {
        if (input == null) {
            return null;
        }
        String lower = input.toLowerCase();

        for (Map.Entry<String, String> entry : keywordMap.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}

