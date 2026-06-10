package com.example.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UrlShortenerApplicationTests {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("app.base-url", () -> "http://localhost:8080");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void homePageRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void validCreationReturns201() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://redis.io/docs/latest/?t=create-test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://redis.io/docs/latest/?t=create-test"))
                .andExpect(jsonPath("$.shortUrl").exists());
    }

    @Test
    void blankUrlIsRejected() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void invalidProtocolIsRejected() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("http")));

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"file:///etc/passwd\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"ftp://example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redirectReturns302WithLocation() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/target\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String body = createResult.getResponse().getContentAsString();
        String code = extractJsonValue(body, "code");

        mockMvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/target"));
    }

    @Test
    void missingCodeReturns404() throws Exception {
        mockMvc.perform(get("/aaaaaaa"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirectWorksWithTrailingSlash() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/trailing-slash\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String code = extractJsonValue(createResult.getResponse().getContentAsString(), "code");

        mockMvc.perform(get("/" + code + "/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/trailing-slash"));
    }

    @Test
    void redirectIncrementsAnalytics() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/analytics-test\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String code = extractJsonValue(createResult.getResponse().getContentAsString(), "code");
        String utcDate = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);

        mockMvc.perform(get("/" + code))
                .andExpect(status().isFound());

        mockMvc.perform(get("/api/urls/" + code + "/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(1))
                .andExpect(jsonPath("$.dailyClicks['" + utcDate + "']").value(1));
    }

    @Test
    void analyticsEndpointReturnsFullData() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://redis.io/docs/latest/\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String code = extractJsonValue(createResult.getResponse().getContentAsString(), "code");

        mockMvc.perform(get("/api/urls/" + code + "/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.originalUrl").value("https://redis.io/docs/latest/"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/" + code))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.totalClicks").value(0))
                .andExpect(jsonPath("$.dailyClicks").exists());
    }

    @Test
    void duplicateUrlReturnsExistingShortUrlForLegacyEntry() throws Exception {
        String url = "https://example.com/legacy-dedup-test";
        redisTemplate.opsForValue().set("url:legacy1", url);
        redisTemplate.opsForHash().put("metadata:legacy1", "createdAt", "2026-06-10T08:00:00Z");
        redisTemplate.opsForHash().put("analytics:legacy1", "totalClicks", "0");

        MvcResult result = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("legacy1", extractJsonValue(result.getResponse().getContentAsString(), "code"));
        assertEquals("legacy1", redisTemplate.opsForValue().get("lookup:" + sha256Hex(url)));
    }

    @Test
    void duplicateUrlReturnsExistingShortUrl() throws Exception {
        MvcResult first = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/duplicate-test\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String firstCode = extractJsonValue(first.getResponse().getContentAsString(), "code");

        MvcResult second = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/duplicate-test\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String secondCode = extractJsonValue(second.getResponse().getContentAsString(), "code");
        assertEquals(firstCode, secondCode);
    }

    @Test
    void allAnalyticsPageRenders() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/all-page-test\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/analytics"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-analytics"));
    }

    @Test
    void allAnalyticsApiReturnsList() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/all-api-test\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/urls/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void deletionRemovesAllKeys() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/delete-me\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String code = extractJsonValue(createResult.getResponse().getContentAsString(), "code");

        mockMvc.perform(delete("/api/urls/" + code))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + code))
                .andExpect(status().isNotFound());

        assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("url:" + code)));
        assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("metadata:" + code)));
        assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("analytics:" + code)));
        assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("analytics:" + code + ":daily")));
        assertFalse(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("urls:index", code)));
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String extractJsonValue(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            return null;
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
