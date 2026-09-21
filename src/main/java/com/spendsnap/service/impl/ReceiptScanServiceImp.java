package com.spendsnap.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsnap.dto.ReceiptScanResponse;
import com.spendsnap.entity.Category;
import com.spendsnap.service.ReceiptScanService;
import com.spendsnap.service.categoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReceiptScanServiceImp implements ReceiptScanService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptScanServiceImp.class);

    // Free-tier Gemini model — good enough for reading receipts/bills and cheap on rate limits.
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private categoryService categoryService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long MAX_FILE_SIZE_BYTES = 8L * 1024 * 1024; // 8MB

    private static final String PROMPT = """
            You are reading a photo of a shopping bill / receipt / invoice for a personal expense tracker app.
            Look at the image carefully and return ONLY a JSON object (no markdown, no explanation) with these fields:
            - "amount": the FINAL total amount paid, as a plain number (no currency symbol, no commas). Use null if you cannot find it.
            - "date": the date on the receipt in strict "yyyy-MM-dd" format. If no date is visible, use null.
            - "merchant": the store / shop / restaurant / service name, short (2-4 words). Use null if unreadable.
            - "description": a short (max 8 words) human-friendly summary of what was purchased, e.g. "Groceries at Big Bazaar" or "Dinner - 2 people".
            - "category": your single best guess for the expense category as a short common word, e.g. one of: Food, Groceries, Transport, Shopping, Entertainment, Bills, Health, Travel, Education, Other. Pick the closest match, don't invent unusual categories.

            Respond with raw JSON only, matching exactly this shape:
            {"amount": 0, "date": "yyyy-MM-dd", "merchant": "", "description": "", "category": ""}
            """;

    @Override
    public ReceiptScanResponse scanReceipt(MultipartFile receiptImage, Long userId) {

        if (receiptImage == null || receiptImage.isEmpty()) {
            return ReceiptScanResponse.failure("Please choose a receipt image first.");
        }

        String contentType = receiptImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ReceiptScanResponse.failure("Only image files (jpg, png, webp) are supported for receipt scanning.");
        }

        if (receiptImage.getSize() > MAX_FILE_SIZE_BYTES) {
            return ReceiptScanResponse.failure("Image is too large. Please upload a photo under 8MB.");
        }

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return ReceiptScanResponse.failure("AI scanning isn't configured yet. Set GEMINI_API_KEY on the server.");
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(receiptImage.getBytes());

            Map<String, Object> requestBody = buildGeminiRequestBody(base64Image, contentType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String urlWithKey = UriComponentsBuilder.fromHttpUrl(geminiApiUrl)
                    .queryParam("key", geminiApiKey)
                    .toUriString();

            Map<?, ?> rawResponse = restTemplate.postForObject(urlWithKey, request, Map.class);

            String extractedJsonText = extractModelText(rawResponse);
            if (extractedJsonText == null) {
                return ReceiptScanResponse.failure("Couldn't read a response from the AI. Please try again.");
            }

            return parseExtractedFields(extractedJsonText);

        } catch (Exception e) {
            log.error("Receipt scan failed", e);
            return ReceiptScanResponse.failure("Something went wrong while scanning the receipt. Please enter the details manually.");
        }
    }

    private Map<String, Object> buildGeminiRequestBody(String base64Image, String mimeType) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", PROMPT);

        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("inline_data", inlineData);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart, imagePart));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("response_mime_type", "application/json");
        generationConfig.put("temperature", 0.1);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);

        return body;
    }

    @SuppressWarnings("unchecked")
    private String extractModelText(Map<?, ?> rawResponse) {
        if (rawResponse == null) return null;

        Object candidatesObj = rawResponse.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) {
            return null;
        }

        Object firstCandidate = candidates.get(0);
        if (!(firstCandidate instanceof Map<?, ?> candidateMap)) return null;

        Object contentObj = candidateMap.get("content");
        if (!(contentObj instanceof Map<?, ?> contentMap)) return null;

        Object partsObj = contentMap.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) return null;

        Object firstPart = parts.get(0);
        if (!(firstPart instanceof Map<?, ?> partMap)) return null;

        Object text = partMap.get("text");
        return text != null ? text.toString() : null;
    }

    private ReceiptScanResponse parseExtractedFields(String jsonText) throws Exception {
        JsonNode node = objectMapper.readTree(jsonText);

        ReceiptScanResponse response = new ReceiptScanResponse();
        response.setSuccess(true);

        if (node.hasNonNull("amount")) {
            response.setAmount(node.get("amount").asDouble());
        }

        if (node.hasNonNull("date")) {
            String rawDate = node.get("date").asText();
            try {
                LocalDate parsedDate = LocalDate.parse(rawDate.trim());
                // Never let a scanned receipt fill in a future date
                if (parsedDate.isAfter(LocalDate.now())) {
                    parsedDate = LocalDate.now();
                }
                response.setDate(parsedDate.toString());
            } catch (Exception ignored) {
            }
        }

        String merchant = node.hasNonNull("merchant") ? node.get("merchant").asText() : null;
        String description = node.hasNonNull("description") ? node.get("description").asText() : null;

        String finalDescription = (description != null && !description.isBlank())
                ? description
                : merchant;
        response.setDescription(finalDescription);

        String suggestedCategory = node.hasNonNull("category") ? node.get("category").asText() : null;
        response.setSuggestedCategoryName(suggestedCategory);

        if (suggestedCategory != null && !suggestedCategory.isBlank()) {
            matchExistingCategory(suggestedCategory, response);
        }

        if (response.getAmount() == null) {
            response.setMessage("Couldn't detect the amount clearly — please check it before saving.");
        } else {
            response.setMessage("Receipt scanned! Please review the details below before saving.");
        }

        return response;
    }

    private void matchExistingCategory(String suggestedCategory, ReceiptScanResponse response) {
        List<Category> categories = categoryService.allCategory();
        String target = suggestedCategory.trim().toLowerCase();

        for (Category category : categories) {
            String name = category.getName() == null ? "" : category.getName().trim().toLowerCase();
            if (name.isEmpty()) continue;

            if (name.equals(target) || name.contains(target) || target.contains(name)) {
                response.setMatchedCategoryId(category.getId());
                response.setMatchedCategoryName(category.getName());
                return;
            }
        }
    }
}
