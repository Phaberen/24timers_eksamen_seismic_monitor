package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// CONTROLLER LAYER — tells the frontend who is logged in, so it can show the right buttons.
@RestController
public class MeController {

    // GET /api/me — returns the current user's name and whether they're an admin.
    // Spring Security injects "Authentication" (the logged-in user). We scan their roles for
    // ROLE_ADMIN to set the admin flag.
    @GetMapping("/api/me")
    public CurrentUser me(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return new CurrentUser(authentication.getName(), admin);
    }
}