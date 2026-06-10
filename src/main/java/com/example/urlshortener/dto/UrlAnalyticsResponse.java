package com.example.urlshortener.dto;

import java.util.Map;

public class UrlAnalyticsResponse {

    private String code;
    private String originalUrl;
    private String shortUrl;
    private String createdAt;
    private long totalClicks;
    private Map<String, Long> dailyClicks;

    public UrlAnalyticsResponse() {
    }

    public UrlAnalyticsResponse(String code, String originalUrl, String shortUrl,
                                String createdAt, long totalClicks, Map<String, Long> dailyClicks) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
        this.totalClicks = totalClicks;
        this.dailyClicks = dailyClicks;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public Map<String, Long> getDailyClicks() {
        return dailyClicks;
    }

    public void setDailyClicks(Map<String, Long> dailyClicks) {
        this.dailyClicks = dailyClicks;
    }
}
