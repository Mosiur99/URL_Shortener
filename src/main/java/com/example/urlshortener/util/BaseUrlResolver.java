package com.example.urlshortener.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BaseUrlResolver {

    private final String configuredBaseUrl;

    public BaseUrlResolver(@Value("${app.base-url}") String configuredBaseUrl) {
        this.configuredBaseUrl = configuredBaseUrl;
    }

    public String resolve(HttpServletRequest request) {
        String configured = normalize(configuredBaseUrl);

        if (request != null && shouldUseRequestHeaders(request, configured)) {
            String fromRequest = fromRequestHeaders(request);
            if (StringUtils.hasText(fromRequest)) {
                return normalize(fromRequest);
            }
        }

        return configured;
    }

    private boolean shouldUseRequestHeaders(HttpServletRequest request, String configured) {
        if (hasForwardedHeaders(request)) {
            return true;
        }
        return isPlaceholder(configured);
    }

    private boolean hasForwardedHeaders(HttpServletRequest request) {
        return StringUtils.hasText(request.getHeader("X-Forwarded-Host"))
                || StringUtils.hasText(request.getHeader("X-Forwarded-Proto"));
    }

    private boolean isPlaceholder(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return true;
        }
        String lower = baseUrl.toLowerCase();
        return lower.contains("your-actual-app");
    }

    private String fromRequestHeaders(HttpServletRequest request) {
        String host = firstHeader(request, "X-Forwarded-Host", "Host");
        if (!StringUtils.hasText(host) && StringUtils.hasText(request.getServerName())) {
            host = request.getServerName();
            int port = request.getServerPort();
            if (port > 0 && port != 80 && port != 443) {
                host = host + ":" + port;
            }
        }
        if (!StringUtils.hasText(host)) {
            return null;
        }
        host = host.split(",")[0].trim();

        String proto = firstHeader(request, "X-Forwarded-Proto");
        if (!StringUtils.hasText(proto)) {
            proto = request.getScheme();
        }
        if (!StringUtils.hasText(proto)) {
            proto = "https";
        }
        proto = proto.split(",")[0].trim();

        return proto + "://" + host;
    }

    private String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String normalize(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "http://localhost:8081";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
