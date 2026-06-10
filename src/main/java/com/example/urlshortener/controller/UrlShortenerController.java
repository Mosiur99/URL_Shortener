package com.example.urlshortener.controller;

import com.example.urlshortener.dto.CreateShortUrlRequest;
import com.example.urlshortener.dto.CreateShortUrlResult;
import com.example.urlshortener.dto.CreateShortUrlResponse;
import com.example.urlshortener.dto.UrlAnalyticsResponse;

import java.util.List;
import com.example.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping
    public ResponseEntity<CreateShortUrlResponse> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request) {
        CreateShortUrlResult result = urlShortenerService.createShortUrl(request.getUrl());
        HttpStatus status = result.isNewlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.getResponse());
    }

    @GetMapping("/analytics")
    public List<UrlAnalyticsResponse> getAllAnalytics() {
        return urlShortenerService.getAllAnalytics();
    }

    @GetMapping("/{code}/analytics")
    public UrlAnalyticsResponse getAnalytics(@PathVariable String code) {
        return urlShortenerService.getAnalytics(code);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String code) {
        urlShortenerService.deleteShortUrl(code);
        return ResponseEntity.noContent().build();
    }
}
