package com.example.urlshortener;

import com.example.urlshortener.util.BaseUrlResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseUrlResolverTest {

    @Test
    void usesRequestHostWhenConfiguredBaseUrlIsPlaceholder() {
        BaseUrlResolver resolver = new BaseUrlResolver("https://your-actual-app.onrender.com");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "url-shortener.onrender.com");

        assertEquals("https://url-shortener.onrender.com", resolver.resolve(request));
    }

    @Test
    void usesConfiguredBaseUrlWhenNoForwardedHeaders() {
        BaseUrlResolver resolver = new BaseUrlResolver("http://localhost:8080");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(80);

        assertEquals("http://localhost:8080", resolver.resolve(request));
    }

    @Test
    void usesHostHeaderWhenForwardedProtoPresent() {
        BaseUrlResolver resolver = new BaseUrlResolver("https://your-actual-app.onrender.com");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "microurl.onrender.com");
        request.addHeader("X-Forwarded-Proto", "https");

        assertEquals("https://microurl.onrender.com", resolver.resolve(request));
    }
}
