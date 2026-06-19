// HTTP Basic credentials for the API client, set on login and cleared on logout.
let authHeader = null;

export function setAuth(header) {
    authHeader = header;
}

// Thin fetch wrapper: attaches the auth header, returns parsed JSON, or throws
// with the backend's problem-detail message so callers can show an error.
export async function api(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    if (authHeader) {
        headers["Authorization"] = authHeader;
    }
    const response = await fetch(path, { ...options, headers });
    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        throw new Error(problem.detail || `Request failed (${response.status})`);
    }
    return response.status === 204 ? null : response.json();
}