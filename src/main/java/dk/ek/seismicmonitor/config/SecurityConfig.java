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

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/sensor-data").permitAll()
                        .requestMatchers("/", "/index.html", "/styles.css", "/Js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/alerts/active").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/*/reports/count").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/alerts/*/reports").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/readings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/*/reports").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/*/readings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/alerts/*/status").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user").password("{noop}user").roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("{noop}admin").roles("ADMIN").build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
