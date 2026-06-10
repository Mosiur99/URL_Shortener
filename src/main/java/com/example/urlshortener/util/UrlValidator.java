package com.example.urlshortener.util;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class UrlValidator {

    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is required and must not be blank");
        }

        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL is not syntactically valid");
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("URL must use http or https protocol");
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only http and https protocols are allowed");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("URL is not syntactically valid");
        }
    }
}
