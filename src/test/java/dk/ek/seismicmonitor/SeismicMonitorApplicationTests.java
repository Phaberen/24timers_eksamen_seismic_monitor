package dk.ek.seismicmonitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Runs on the H2-backed "test" profile so the context loads without MySQL/Docker.
@SpringBootTest
@ActiveProfiles("test")
class SeismicMonitorApplicationTests {

    @Test
    void contextLoads() {
    }

}
