package dk.ek.seismicmonitor.dto;

// The logged-in user's identity and whether they hold the admin role, so the
// frontend can show role-appropriate controls.
public record CurrentUser(String username, boolean admin) { }