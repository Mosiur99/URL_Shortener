package com.example.urlshortener.controller;

import com.example.urlshortener.dto.UrlAnalyticsResponse;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.util.BaseUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;
    private final BaseUrlResolver baseUrlResolver;

    public HomeController(UrlShortenerService urlShortenerService,
                          BaseUrlResolver baseUrlResolver) {
        this.urlShortenerService = urlShortenerService;
        this.baseUrlResolver = baseUrlResolver;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/analytics")
    public String allAnalytics(Model model, HttpServletRequest httpRequest) {
        String publicBaseUrl = baseUrlResolver.resolve(httpRequest);
        List<UrlAnalyticsResponse> links = urlShortenerService.getAllAnalytics(publicBaseUrl);
        model.addAttribute("links", links);
        model.addAttribute("totalLinks", links.size());
        long totalClicks = links.stream().mapToLong(UrlAnalyticsResponse::getTotalClicks).sum();
        model.addAttribute("totalClicks", totalClicks);
        return "all-analytics";
    }

    @GetMapping("/analytics/{code}")
    public String analytics(@PathVariable String code, Model model, HttpServletRequest httpRequest) {
        UrlAnalyticsResponse analytics = urlShortenerService.getAnalytics(
                code, baseUrlResolver.resolve(httpRequest));
        model.addAttribute("analytics", analytics);
        return "analytics";
    }
}
