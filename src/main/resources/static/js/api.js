/**
 * Thin wrapper around fetch() that:
 *  - always sends/receives the session cookie (credentials: 'include')
 *  - attaches the CSRF token (read from the XSRF-TOKEN cookie) on state-changing requests
 *  - throws a normalized Error with a readable message on non-2xx responses
 */
const Api = (() => {

    function getCookie(name) {
        const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
        return match ? decodeURIComponent(match[2]) : null;
    }

    async function request(method, url, body, extraOptions = {}) {
        const headers = { 'Content-Type': 'application/json', ...(extraOptions.headers || {}) };

        if (!['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase())) {
            const csrfToken = getCookie('XSRF-TOKEN');
            if (csrfToken) headers['X-XSRF-TOKEN'] = csrfToken;
        }

		const response = await fetch(url, {
		            method,
		            headers,
		            credentials: 'include',
		            cache: 'no-store',
		            body: body !== undefined ? JSON.stringify(body) : undefined,
		            ...extraOptions
		        });

        if (response.status === 401) {
            // Session expired or not authenticated - send back to login
            if (!window.location.pathname.endsWith('index.html') && window.location.pathname !== '/') {
                window.location.href = 'index.html';
            }
            throw new Error('Not authenticated');
        }

        if (!response.ok) {
            let message = `Request failed (${response.status})`;
            try {
                const errBody = await response.json();
                if (errBody && errBody.message) message = errBody.message;
            } catch (e) { /* ignore parse errors */ }
            throw new Error(message);
        }

        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            return response.json();
        }
        return response;
    }

    return {
        get: (url) => request('GET', url),
        post: (url, body) => request('POST', url, body),
        put: (url, body) => request('PUT', url, body),
        del: (url) => request('DELETE', url),
        download: async (url, filenameFallback) => {
            const response = await request('GET', url, undefined, { headers: {} });
            const blob = await response.blob();
            const disposition = response.headers.get('content-disposition') || '';
            const match = disposition.match(/filename="?([^"]+)"?/);
            const filename = match ? match[1] : filenameFallback;

            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            link.remove();
        }
    };
})();
