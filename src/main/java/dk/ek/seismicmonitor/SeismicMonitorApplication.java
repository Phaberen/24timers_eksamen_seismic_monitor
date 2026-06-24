package dk.ek.seismicmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// The starting point of the whole app.
// @SpringBootApplication switches on Spring Boot's auto-setup AND tells Spring to scan this package
// (and everything under it) for our @RestController/@Service/@Repository/@Configuration classes,
// creating and wiring them all together.
@SpringBootApplication
public class SeismicMonitorApplication {

    public static void main(String[] args) {
        // Boots everything: builds the Spring container, wires the beans, and starts the web server
        // on port 8080. After this line returns, the app is up and listening for requests.
        SpringApplication.run(SeismicMonitorApplication.class, args);
    }

}
