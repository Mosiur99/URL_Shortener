package com.example.urlshortener.controller;

import com.example.urlshortener.dto.CreateShortUrlRequest;
import com.example.urlshortener.dto.CreateShortUrlResult;
import com.example.urlshortener.dto.CreateShortUrlResponse;
import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.util.BaseUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/urls")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final BaseUrlResolver baseUrlResolver;

    public UrlShortenerController(UrlShortenerService urlShortenerService,
                                  BaseUrlResolver baseUrlResolver) {
        this.urlShortenerService = urlShortenerService;
        this.baseUrlResolver = baseUrlResolver;
    }

    @PostMapping
    public ResponseEntity<CreateShortUrlResponse> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request,
            HttpServletRequest httpRequest) {
        String publicBaseUrl = baseUrlResolver.resolve(httpRequest);
        CreateShortUrlResult result = urlShortenerService.createShortUrl(request.getUrl(), publicBaseUrl);
        HttpStatus status = result.isNewlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.getResponse());
    }

    @GetMapping("/analytics")
    public List<UrlAnalyticsResponse> getAllAnalytics(HttpServletRequest httpRequest) {
        return urlShortenerService.getAllAnalytics(baseUrlResolver.resolve(httpRequest));
    }

    @GetMapping("/{code}/analytics")
    public UrlAnalyticsResponse getAnalytics(@PathVariable String code, HttpServletRequest httpRequest) {
        return urlShortenerService.getAnalytics(code, baseUrlResolver.resolve(httpRequest));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String code) {
        urlShortenerService.deleteShortUrl(code);
        return ResponseEntity.noContent().build();
    }
}
