let authHeader = null;

export function setAuth(header) {
    authHeader = header;
}

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
