package com.jobplatform.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.models.UserAccount;
import com.jobplatform.repositories.UserRepository;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/momo-payment")
public class MomoPaymentController {

    @Value("${spring.momo.access-key}")
    private String ACCESS_KEY;
    @Value("${spring.momo.secret-key}")
    private String SECRET_KEY;
    private static final String PARTNER_CODE = "MOMO";
    private static final String ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create";

    private final UserRepository userRepository;

    public MomoPaymentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("")
    public ResponseEntity<Object> processMomoPayment(@RequestBody Map<String, String> requestPayload) {
        try {
            // Extract data from request body
            String amount = requestPayload.get("amount");
            String account = requestPayload.get("account");

            // Generate unique orderId and requestId
            String orderId = PARTNER_CODE + System.currentTimeMillis();

            // Payment information
            String orderInfo = "Buy more recruitment news for account "+account;
            String redirectUrl = "http://localhost:5173/momo-payment/verify";
            String ipnUrl = "http://localhost:5173/momo-payment/verify";
            String requestType = "payWithMethod";
            String extraData = account;
            String orderGroupId = "";
            boolean autoCapture = true;
            String lang = "vi";

            // Generate raw signature string
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    ACCESS_KEY, amount, extraData, ipnUrl, orderId, orderInfo, PARTNER_CODE, redirectUrl, orderId, requestType
            );

            // Generate HMAC SHA256 signature
            String signature = hmacSHA256(rawSignature, SECRET_KEY);

            // Build json request payload
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("partnerName", "Job Platform");
            requestBody.put("storeId", "Job Platform");
            requestBody.put("requestId", orderId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("lang", lang);
            requestBody.put("requestType", requestType);
            requestBody.put("autoCapture", autoCapture);
            requestBody.put("extraData", extraData);
            requestBody.put("orderGroupId", orderGroupId);
            requestBody.put("signature", signature);

            // Send request to MoMo
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(ENDPOINT);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    // Deserialize JSON string into a Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> momoResponse = objectMapper.readValue(responseBody, Map.class);
                    if (statusCode == HttpStatus.OK.value()) {

                        return new ResponseEntity<>(momoResponse,HttpStatus.OK);

                    } else {
                        return new ResponseEntity<>(momoResponse,HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> handleMomoCallback(@RequestParam Map<String, String> params) {
        try {
            // Extract parameters from the callback URL
            String partnerCode = params.get("partnerCode");
            String orderId = params.get("orderId");
            String requestId = params.get("requestId");
            String amount = params.get("amount");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String transId = params.get("transId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");
            String payType = params.get("payType");
            String responseTime = params.get("responseTime");
            String extraData = params.get("extraData");
            String receivedSignature = params.get("signature");

            // Build rawSignature
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                    ACCESS_KEY, amount, extraData, message, orderId, orderInfo, orderType, partnerCode, payType, requestId, responseTime, resultCode, transId
            );

            // Generate signature
            String generatedSignature = hmacSHA256(rawSignature, SECRET_KEY);

            // Validate signature
            if (generatedSignature.equals(receivedSignature)) {
                //Success
                if ("0".equals(resultCode)) {
                    UserAccount userAccount = userRepository.findByEmail(extraData).orElseThrow(()-> new NoSuchElementException("User not found"));
                    Integer numberOfJobs = Integer.parseInt(amount) /100000;
                    Integer currentNumberOfJobs = userAccount.getAvailableJobPosts()!=null?userAccount.getAvailableJobPosts():0;
                    userAccount.setAvailableJobPosts(currentNumberOfJobs + numberOfJobs);
                    userRepository.save(userAccount);
                    return new ResponseEntity<>("Payment verified successfully",HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Payment fail. Result code: "+resultCode,HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>("Invalid signature",HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error:"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }
}

