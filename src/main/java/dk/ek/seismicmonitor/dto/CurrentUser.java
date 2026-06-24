package dk.ek.seismicmonitor.dto;

// DTO — what GET /api/me returns: the logged-in user's name and whether they're an admin.
// The frontend uses the "admin" flag to decide which buttons/tabs to show.
public record CurrentUser(String username, boolean admin) { }
