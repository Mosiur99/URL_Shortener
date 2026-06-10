package com.example.urlshortener.dto;

public class CreateShortUrlResponse {

    private String code;
    private String originalUrl;
    private String shortUrl;

    public CreateShortUrlResponse() {
    }

    public CreateShortUrlResponse(String code, String originalUrl, String shortUrl) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
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
}
