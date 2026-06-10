package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateShortUrlRequest {

    @NotBlank(message = "url is required and must not be blank")
    private String url;

    public CreateShortUrlRequest() {
    }

    public CreateShortUrlRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
