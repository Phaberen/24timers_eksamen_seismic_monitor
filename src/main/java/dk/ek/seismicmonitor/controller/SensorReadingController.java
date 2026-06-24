package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.SensorReadingResponse;
import dk.ek.seismicmonitor.service.SensorDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CONTROLLER LAYER — admin-only view of all raw readings (access is enforced in SecurityConfig).
@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

    private final SensorDataService sensorDataService;

    public SensorReadingController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    // Handles GET /api/readings — returns every stored reading as JSON.
    @GetMapping
    public List<SensorReadingResponse> getAllReadings() {
        return sensorDataService.getAllReadings();
    }
}
