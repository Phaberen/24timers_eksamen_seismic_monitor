// FRONTEND (api module) — the single place that talks to the backend.
// Every other module calls api() so login and error handling live in one spot.

// The "Authorization: Basic ..." header for the logged-in user. Null when nobody is logged in.
let authHeader = null;

// Called by the login/logout code to set or clear who we're sending requests as.
export function setAuth(header) {
    authHeader = header;
}

// Makes an HTTP request to the backend and returns the parsed JSON.
// "async/await" lets us write the network call as if it were step-by-step code.
export async function api(path, options = {}) {
    const headers = { ...(options.headers || {}) };
    // Attach the login header if we have one, so protected endpoints accept the request.
    if (authHeader) {
        headers["Authorization"] = authHeader;
    }
    const response = await fetch(path, { ...options, headers });
    // If the server returned an error status, pull the message out and throw so the caller can show it.
    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        throw new Error(problem.detail || `Request failed (${response.status})`);
    }
    // 204 means "no content", so there's nothing to parse; otherwise read the JSON body.
    return response.status === 204 ? null : response.json();
}
