document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('url-form');
    if (!form) return;

    const urlInput = document.getElementById('url-input');
    const errorMessage = document.getElementById('error-message');
    const result = document.getElementById('result');
    const resultCode = document.getElementById('result-code');
    const resultShortUrl = document.getElementById('result-short-url');
    const resultOriginalUrl = document.getElementById('result-original-url');
    const analyticsLink = document.getElementById('analytics-link');
    const submitBtn = document.getElementById('submit-btn');
    const btnText = document.getElementById('btn-text');
    const copyShortBtn = document.getElementById('copy-short-btn');
    const copyCodeBtn = document.getElementById('copy-code-btn');
    const newLinkBtn = document.getElementById('new-link-btn');

    let lastShortUrl = '';
    let lastCode = '';

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        hideError();
        result.classList.add('hidden');
        setLoading(true);

        try {
            const response = await fetch('/api/urls', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ url: urlInput.value.trim() })
            });

            const data = await response.json();

            if (!response.ok) {
                showError(data.error || 'Failed to create short URL');
                return;
            }

            lastShortUrl = data.shortUrl;
            lastCode = data.code;

            const resultTitle = result.querySelector('.result-header h2');
            if (response.status === 200) {
                resultTitle.textContent = 'Existing link returned';
            } else {
                resultTitle.textContent = 'Link created successfully';
            }

            resultCode.textContent = data.code;
            resultShortUrl.textContent = data.shortUrl;
            resultShortUrl.href = data.shortUrl;
            resultOriginalUrl.textContent = data.originalUrl;
            analyticsLink.href = '/analytics/' + data.code;
            result.classList.remove('hidden');
            result.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } catch (err) {
            showError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    });

    copyShortBtn.addEventListener('click', function () {
        copyToClipboard(lastShortUrl, copyShortBtn);
    });

    copyCodeBtn.addEventListener('click', function () {
        copyToClipboard(lastCode, copyCodeBtn);
    });

    newLinkBtn.addEventListener('click', function () {
        result.classList.add('hidden');
        urlInput.value = '';
        urlInput.focus();
        hideError();
    });

    function setLoading(loading) {
        submitBtn.disabled = loading;
        if (loading) {
            btnText.innerHTML = '<span class="spinner"></span>';
        } else {
            btnText.textContent = 'Shorten';
        }
    }

    function showError(msg) {
        errorMessage.textContent = msg;
        errorMessage.classList.remove('hidden');
    }

    function hideError() {
        errorMessage.classList.add('hidden');
    }

    function copyToClipboard(text, btn) {
        if (!text) return;

        navigator.clipboard.writeText(text).then(function () {
            const original = btn.innerHTML;
            btn.innerHTML = '<svg viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>';
            btn.style.color = '#34d399';
            setTimeout(function () {
                btn.innerHTML = original;
                btn.style.color = '';
            }, 1800);
        }).catch(function () {
            showError('Could not copy to clipboard.');
        });
    }
});
