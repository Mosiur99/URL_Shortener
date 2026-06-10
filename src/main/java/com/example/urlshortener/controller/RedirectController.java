package com.example.urlshortener.controller;

import com.example.urlshortener.exception.ShortUrlNotFoundException;
import com.example.urlshortener.service.UrlShortenerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@Controller
public class RedirectController {

    private static final Set<String> RESERVED_PATHS = Set.of(
            "api", "analytics", "css", "js", "images", "error", "favicon.ico"
    );
    private static final String CODE_PATTERN = "[A-Za-z0-9]{7}";

    private final UrlShortenerService urlShortenerService;

    public RedirectController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping({"/{code}", "/{code}/"})
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String normalizedCode = code.trim();
        if (RESERVED_PATHS.contains(normalizedCode.toLowerCase()) || !normalizedCode.matches(CODE_PATTERN)) {
            throw new ShortUrlNotFoundException(normalizedCode);
        }

        String originalUrl = urlShortenerService.resolveOriginalUrl(normalizedCode);
        urlShortenerService.recordClick(normalizedCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
