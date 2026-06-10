<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Analytics — ${analytics.code} | MicroURL</title>
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
            <a href="/" class="nav-link">Home</a>
            <a href="/analytics" class="nav-link">All Links</a>
        </div>
    </nav>

    <main class="page-main">
        <a href="/analytics" class="back-link">
            <svg viewBox="0 0 24 24"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
            Back to all links
        </a>

        <div class="page-header">
            <h1>Link Analytics</h1>
            <p>Performance overview for <strong style="color:#a5b4fc;">${analytics.code}</strong></p>
        </div>

        <div class="stats-grid">
            <div class="stat-card highlight">
                <div class="stat-label">Total Clicks</div>
                <div class="stat-value">${analytics.totalClicks}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Short Code</div>
                <div class="stat-value small">${analytics.code}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Created</div>
                <div class="stat-value small">${analytics.createdAt}</div>
            </div>
        </div>

        <div class="detail-card">
            <div class="detail-row">
                <span class="detail-label">Short URL</span>
                <span class="detail-value">
                    <a href="${analytics.shortUrl}" target="_blank" rel="noopener">${analytics.shortUrl}</a>
                </span>
            </div>
            <div class="detail-row">
                <span class="detail-label">Original URL</span>
                <span class="detail-value">
                    <a href="${analytics.originalUrl}" target="_blank" rel="noopener">${analytics.originalUrl}</a>
                </span>
            </div>
        </div>

        <div class="section-title">Daily Clicks (UTC)</div>

        <c:choose>
            <c:when test="${empty analytics.dailyClicks}">
                <div class="empty-state">
                    <svg viewBox="0 0 24 24"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
                    <p>No clicks recorded yet.<br>Share your link to start tracking.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="analytics-table">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Clicks</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="entry" items="${analytics.dailyClicks}">
                            <tr>
                                <td>${entry.key}</td>
                                <td><span class="click-count">${entry.value}</span></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <footer class="footer">
        Built with <span>Spring Boot</span> &amp; <span>Redis</span>
    </footer>
</div>
</body>
</html>
