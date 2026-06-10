<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>All Links — MicroURL</title>
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
            <a href="/analytics" class="nav-link active">All Links</a>
        </div>
    </nav>

    <main class="page-main page-main-wide">
        <div class="page-header">
            <h1>All Short Links</h1>
            <p>Overview of every shortened URL and its click performance.</p>
        </div>

        <div class="stats-grid">
            <div class="stat-card highlight">
                <div class="stat-label">Total Links</div>
                <div class="stat-value">${totalLinks}</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Total Clicks</div>
                <div class="stat-value">${totalClicks}</div>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty links}">
                <div class="empty-state">
                    <svg viewBox="0 0 24 24"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
                    <p>No short links yet.<br><a href="/">Create your first link</a></p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="analytics-table links-table">
                        <thead>
                        <tr>
                            <th>Code</th>
                            <th>Short URL</th>
                            <th>Original URL</th>
                            <th>Clicks</th>
                            <th>Created</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="link" items="${links}">
                            <tr>
                                <td><span class="code-badge">${link.code}</span></td>
                                <td class="url-cell">
                                    <a href="${link.shortUrl}" target="_blank" rel="noopener">${link.shortUrl}</a>
                                </td>
                                <td class="url-cell" title="${link.originalUrl}">${link.originalUrl}</td>
                                <td><span class="click-count">${link.totalClicks}</span></td>
                                <td class="date-cell">${link.createdAt}</td>
                                <td>
                                    <a href="/analytics/${link.code}" class="btn btn-secondary btn-sm">Details</a>
                                </td>
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
