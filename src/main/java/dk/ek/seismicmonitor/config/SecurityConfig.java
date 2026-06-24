package dk.ek.seismicmonitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// CONFIG LAYER — all the security rules live here in one place.
// It answers two questions for every request: "who are you?" (login) and "are you allowed?" (roles).
// @Configuration means this class hands Spring some ready-made objects (the @Bean methods below).
@Configuration
public class SecurityConfig {

    // Builds the chain of checks every request passes through before it reaches a controller.
    // Spring gives us the "http" builder; we switch features on/off and return the finished chain.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is for cookie/session websites. This API is stateless (login is
                // sent on every request, no session), so it doesn't apply -> turn it off.
                .csrf(AbstractHttpConfigurer::disable)
                // Don't keep any server-side session. Each request must prove who it is on its own.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // The access rules. Spring checks them top to bottom and the FIRST match wins,
                // so the specific rules come first and the catch-all comes last.
                .authorizeHttpRequests(auth -> auth
                        // Public: the simulator can't log in, so sending sensor data must be open.
                        .requestMatchers(HttpMethod.POST, "/api/sensor-data").permitAll()
                        // Public: the frontend files (page, styles, scripts) load without login.
                        .requestMatchers("/", "/index.html", "/styles.css", "/Js/**", "/favicon.ico").permitAll()
                        // "Who am I" endpoint: any logged-in user.
                        .requestMatchers("/api/me").authenticated()
                        // USER or ADMIN: view active alerts, see a report count, file a report.
                        .requestMatchers(HttpMethod.GET, "/api/alerts/active").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/*/reports/count").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/alerts/*/reports").hasAnyRole("USER", "ADMIN")
                        // ADMIN only: raw readings, all alerts, the reports list, the readings behind
                        // an alert, and changing an alert's status. Note GET vs POST on the same path
                        // can have different rules (filing a report vs reading the list).
                        .requestMatchers(HttpMethod.GET, "/api/readings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/*/reports").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/*/readings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/alerts/*/status").hasRole("ADMIN")
                        // Anything not listed above still needs a login.
                        .anyRequest().authenticated())
                // Use HTTP Basic login: the client sends username+password on each request.
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    // The list of accounts that are allowed to log in. Spring calls this to look someone up by name.
    @Bean
    public UserDetailsService userDetailsService() {
        // Two hardcoded accounts (no database — fine for an exam). roles("USER") becomes "ROLE_USER".
        // {noop} is a password marker meaning "no hashing — the password is stored as plain text".
        // A real app would hash it instead (e.g. BCrypt).
        UserDetails user = User.withUsername("user").password("{noop}user").roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("{noop}admin").roles("ADMIN").build();
        // Keeps those accounts in memory and serves them to the login machinery.
        return new InMemoryUserDetailsManager(user, admin);
    }
}
