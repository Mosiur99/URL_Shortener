<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MicroURL — URL Shortener</title>
    <link rel="icon" href="/images/microurl-logo.png" type="image/png">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/styles.css">
</head>
<body>
<div class="page">
    <nav class="navbar">
        <a href="/" class="brand">
            <img src="/images/microurl-logo.png" alt="MicroURL" class="brand-logo">
        </a>
        <div class="nav-links">
            <a href="/" class="nav-link active">Home</a>
            <a href="/analytics" class="nav-link">All Links</a>
        </div>
    </nav>

    <main class="page-main">
        <section class="hero">
            <img src="/images/microurl-logo.png" alt="MicroURL" class="hero-logo">
            <div class="hero-eyebrow">URL Shortener</div>
            <h1>Short links.<br>Real-time analytics.</h1>
            <p>Transform long URLs into compact, shareable links — with click tracking backed by Redis.</p>
            <div class="features">
                <span class="feature-pill">
                    <svg viewBox="0 0 24 24"><polyline points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                    Instant redirects
                </span>
                <span class="feature-pill">
                    <svg viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
                    Click analytics
                </span>
                <span class="feature-pill">
                    <svg viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    Secure &amp; fast
                </span>
            </div>
        </section>

        <div class="card">
            <div class="card-title">Shorten a URL</div>
            <form id="url-form">
                <div class="form-group">
                    <label class="form-label" for="url-input">Paste your long URL</label>
                    <div class="input-row">
                        <div class="input-wrap">
                            <span class="input-icon">
                                <svg viewBox="0 0 24 24"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
                            </span>
                            <input type="url" id="url-input" name="url"
                                   placeholder="https://example.com/your-very-long-url" required>
                        </div>
                        <button type="submit" class="btn btn-primary" id="submit-btn">
                            <span id="btn-text">Shorten</span>
                        </button>
                    </div>
                </div>
            </form>

            <div id="error-message" class="message error hidden"></div>

            <div id="result" class="result hidden">
                <div class="result-header">
                    <span class="result-check">
                        <svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                    </span>
                    <h2>Link created successfully</h2>
                </div>

                <div class="result-field">
                    <div class="result-label">Short URL</div>
                    <div class="copy-row">
                        <a id="result-short-url" href="#" target="_blank" rel="noopener"></a>
                        <button type="button" class="btn btn-secondary btn-icon" id="copy-short-btn" title="Copy link">
                            <svg viewBox="0 0 24 24"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                        </button>
                    </div>
                </div>

                <div class="result-field">
                    <div class="result-label">Short code</div>
                    <div class="copy-row">
                        <span id="result-code"></span>
                        <button type="button" class="btn btn-secondary btn-icon" id="copy-code-btn" title="Copy code">
                            <svg viewBox="0 0 24 24"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                        </button>
                    </div>
                </div>

                <div class="result-field">
                    <div class="result-label">Original URL</div>
                    <div class="result-value" id="result-original-url"></div>
                </div>

                <div class="result-actions">
                    <a id="analytics-link" href="#" class="btn btn-secondary">
                        <svg viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
                        View analytics
                    </a>
                    <button type="button" class="btn btn-ghost" id="new-link-btn">Shorten another</button>
                </div>
            </div>
        </div>
    </main>

    <footer class="footer">
        Built with <span>Spring Boot</span> &amp; <span>Redis</span>
    </footer>
</div>
<script src="/js/app.js"></script>
</body>
</html>
