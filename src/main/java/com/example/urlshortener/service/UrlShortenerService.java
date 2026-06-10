package com.example.urlshortener.service;

import com.example.urlshortener.dto.CreateShortUrlResponse;
import com.example.urlshortener.dto.CreateShortUrlResult;
import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.exception.ShortUrlNotFoundException;
import com.example.urlshortener.util.ShortCodeGenerator;
import com.example.urlshortener.util.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UrlShortenerService {

    private static final String URL_KEY_PREFIX = "url:";
    private static final String METADATA_KEY_PREFIX = "metadata:";
    private static final String ANALYTICS_KEY_PREFIX = "analytics:";
    private static final String LOOKUP_KEY_PREFIX = "lookup:";
    private static final String INDEX_KEY = "urls:index";
    private static final String DAILY_KEY_SUFFIX = ":daily";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String TOTAL_CLICKS_FIELD = "totalClicks";

    private final StringRedisTemplate redisTemplate;
    private final UrlValidator urlValidator;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;

    public UrlShortenerService(StringRedisTemplate redisTemplate,
                               UrlValidator urlValidator,
                               ShortCodeGenerator shortCodeGenerator,
                               @Value("${app.base-url}") String baseUrl) {
        this.redisTemplate = redisTemplate;
        this.urlValidator = urlValidator;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = baseUrl;
    }

    public CreateShortUrlResult createShortUrl(String originalUrl) {
        urlValidator.validate(originalUrl);
        String trimmedUrl = originalUrl.trim();

        String existingCode = findExistingCode(trimmedUrl);
        if (existingCode != null) {
            return new CreateShortUrlResult(
                    new CreateShortUrlResponse(existingCode, trimmedUrl, buildShortUrl(existingCode)),
                    false
            );
        }

        String code = generateUniqueCode();
        String createdAt = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        redisTemplate.opsForValue().set(urlKey(code), trimmedUrl);
        redisTemplate.opsForHash().put(metadataKey(code), CREATED_AT_FIELD, createdAt);
        redisTemplate.opsForHash().put(analyticsKey(code), TOTAL_CLICKS_FIELD, "0");
        redisTemplate.opsForValue().set(lookupKey(trimmedUrl), code);
        redisTemplate.opsForSet().add(INDEX_KEY, code);

        return new CreateShortUrlResult(
                new CreateShortUrlResponse(code, trimmedUrl, buildShortUrl(code)),
                true
        );
    }

    public String resolveOriginalUrl(String code) {
        String originalUrl = redisTemplate.opsForValue().get(urlKey(code));
        if (originalUrl == null) {
            throw new ShortUrlNotFoundException(code);
        }
        return originalUrl;
    }

    public void recordClick(String code) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(urlKey(code)))) {
            throw new ShortUrlNotFoundException(code);
        }

        redisTemplate.opsForHash().increment(analyticsKey(code), TOTAL_CLICKS_FIELD, 1);

        String utcDate = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        redisTemplate.opsForHash().increment(dailyKey(code), utcDate, 1);
    }

    public UrlAnalyticsResponse getAnalytics(String code) {
        String originalUrl = redisTemplate.opsForValue().get(urlKey(code));
        if (originalUrl == null) {
            throw new ShortUrlNotFoundException(code);
        }
        return buildAnalyticsResponse(code, originalUrl);
    }

    public List<UrlAnalyticsResponse> getAllAnalytics() {
        Set<String> codes = discoverCodesFromUrlKeys();
        Map<String, UrlAnalyticsResponse> canonicalByUrl = new HashMap<>();

        for (String code : codes) {
            String originalUrl = redisTemplate.opsForValue().get(urlKey(code));
            if (originalUrl == null) {
                continue;
            }

            UrlAnalyticsResponse current = buildAnalyticsResponse(code, originalUrl);
            UrlAnalyticsResponse existing = canonicalByUrl.get(originalUrl);
            if (existing == null || isEarlier(current.getCreatedAt(), existing.getCreatedAt())) {
                canonicalByUrl.put(originalUrl, current);
                backfillLookup(originalUrl, current.getCode());
            }
        }

        List<UrlAnalyticsResponse> results = new ArrayList<>(canonicalByUrl.values());
        results.sort(Comparator.comparing(
                UrlAnalyticsResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return results;
    }

    public void deleteShortUrl(String code) {
        String originalUrl = redisTemplate.opsForValue().get(urlKey(code));
        if (originalUrl == null) {
            throw new ShortUrlNotFoundException(code);
        }

        redisTemplate.delete(List.of(
                urlKey(code),
                metadataKey(code),
                analyticsKey(code),
                dailyKey(code),
                lookupKey(originalUrl)
        ));
        redisTemplate.opsForSet().remove(INDEX_KEY, code);
    }

    private UrlAnalyticsResponse buildAnalyticsResponse(String code, String originalUrl) {
        Map<Object, Object> metadata = redisTemplate.opsForHash().entries(metadataKey(code));
        Map<Object, Object> analytics = redisTemplate.opsForHash().entries(analyticsKey(code));
        Map<Object, Object> daily = redisTemplate.opsForHash().entries(dailyKey(code));

        String createdAt = (String) metadata.get(CREATED_AT_FIELD);
        long totalClicks = parseLong(analytics.get(TOTAL_CLICKS_FIELD));

        Map<String, Long> dailyClicks = new HashMap<>();
        for (Map.Entry<Object, Object> entry : daily.entrySet()) {
            dailyClicks.put(entry.getKey().toString(), parseLong(entry.getValue()));
        }

        return new UrlAnalyticsResponse(
                code,
                originalUrl,
                buildShortUrl(code),
                createdAt,
                totalClicks,
                dailyClicks
        );
    }

    private String findExistingCode(String trimmedUrl) {
        String fromLookup = redisTemplate.opsForValue().get(lookupKey(trimmedUrl));
        if (fromLookup != null && Boolean.TRUE.equals(redisTemplate.hasKey(urlKey(fromLookup)))) {
            return fromLookup;
        }

        String oldestCode = null;
        String oldestCreatedAt = null;

        for (String code : discoverCodes()) {
            String storedUrl = redisTemplate.opsForValue().get(urlKey(code));
            if (!trimmedUrl.equals(storedUrl)) {
                continue;
            }

            String createdAt = (String) redisTemplate.opsForHash().get(metadataKey(code), CREATED_AT_FIELD);
            if (oldestCode == null || isEarlier(createdAt, oldestCreatedAt)) {
                oldestCode = code;
                oldestCreatedAt = createdAt;
            }
        }

        if (oldestCode != null) {
            backfillLookup(trimmedUrl, oldestCode);
        }
        return oldestCode;
    }

    private boolean isEarlier(String candidate, String current) {
        if (current == null) {
            return true;
        }
        if (candidate == null) {
            return false;
        }
        return candidate.compareTo(current) < 0;
    }

    private void backfillLookup(String trimmedUrl, String code) {
        redisTemplate.opsForValue().set(lookupKey(trimmedUrl), code);
        redisTemplate.opsForSet().add(INDEX_KEY, code);
    }

    private Set<String> discoverCodesFromUrlKeys() {
        return discoverCodes();
    }

    private Set<String> discoverCodes() {
        Set<String> codes = new HashSet<>();
        Set<String> fromIndex = redisTemplate.opsForSet().members(INDEX_KEY);
        if (fromIndex != null) {
            fromIndex.stream()
                    .filter(code -> code.matches("[A-Za-z0-9]{7}"))
                    .forEach(codes::add);
        }
        codes.addAll(scanUrlCodes());
        return codes;
    }

    private Set<String> scanUrlCodes() {
        Set<String> codes = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(URL_KEY_PREFIX + "*")
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String code = key.substring(URL_KEY_PREFIX.length());
                if (code.matches("[A-Za-z0-9]{7}")) {
                    codes.add(code);
                }
            }
        }
        return codes;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (Boolean.TRUE.equals(redisTemplate.hasKey(urlKey(code))));
        return code;
    }

    private String buildShortUrl(String code) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBase + "/" + code;
    }

    private String lookupKey(String originalUrl) {
        return LOOKUP_KEY_PREFIX + sha256Hex(originalUrl);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String urlKey(String code) {
        return URL_KEY_PREFIX + code;
    }

    private String metadataKey(String code) {
        return METADATA_KEY_PREFIX + code;
    }

    private String analyticsKey(String code) {
        return ANALYTICS_KEY_PREFIX + code;
    }

    private String dailyKey(String code) {
        return ANALYTICS_KEY_PREFIX + code + DAILY_KEY_SUFFIX;
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }
}
