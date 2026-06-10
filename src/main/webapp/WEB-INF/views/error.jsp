<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error ${status} | MicroURL</title>
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
        <div class="card error-page">
            <div class="error-code">${status}</div>
            <h1>
                <c:choose>
                    <c:when test="${status == 404}">Link not found</c:when>
                    <c:otherwise>Something went wrong</c:otherwise>
                </c:choose>
            </h1>
            <p>${message}</p>
            <a href="/" class="btn btn-primary">
                <svg viewBox="0 0 24 24" style="width:16px;height:16px;stroke:#fff;fill:none;stroke-width:2"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
                Return to home
            </a>
        </div>
    </main>

    <footer class="footer">
        Built with <span>Spring Boot</span> &amp; <span>Redis</span>
    </footer>
</div>
</body>
</html>
