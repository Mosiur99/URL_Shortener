package com.example.urlshortener.dto;

public class CreateShortUrlResult {

    private final CreateShortUrlResponse response;
    private final boolean newlyCreated;

    public CreateShortUrlResult(CreateShortUrlResponse response, boolean newlyCreated) {
        this.response = response;
        this.newlyCreated = newlyCreated;
    }

    public CreateShortUrlResponse getResponse() {
        return response;
    }

    public boolean isNewlyCreated() {
        return newlyCreated;
    }
}
