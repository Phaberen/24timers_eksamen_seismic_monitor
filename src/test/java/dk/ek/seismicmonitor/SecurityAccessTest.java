package dk.ek.seismicmonitor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired MockMvc mvc;

    @Test
    void sensorData_isPublic() throws Exception {
        mvc.perform(post("/api/sensor-data").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isCreated());
    }

    @Test
    void allAlerts_withoutLogin_isUnauthorized() throws Exception {
        mvc.perform(get("/api/alerts")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_cannotListAllAlerts() throws Exception {
        mvc.perform(get("/api/alerts")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_canSeeActiveAlerts() throws Exception {
        mvc.perform(get("/api/alerts/active")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canListAllAlerts() throws Exception {
        mvc.perform(get("/api/alerts")).andExpect(status().isOk());
    }
}
