package dk.ek.seismicmonitor.controller;

import dk.ek.seismicmonitor.dto.SensorDataRequest;
import dk.ek.seismicmonitor.service.SensorDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CONTROLLER LAYER — the public front door for incoming sensor data.
// @RestController = this class handles web requests and returns data (JSON), not web pages.
// @RequestMapping sets the base URL all methods here live under.
@RestController
@RequestMapping("/api/sensor-data")
public class SensorDataController {

    // Controllers stay thin: they just receive the request and hand the work to a service.
    private final SensorDataService sensorDataService;

    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    // Handles POST /api/sensor-data. @RequestBody turns the incoming JSON array into a list of DTOs.
    // We pass it to the service and reply 201 Created with an empty body.
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody List<SensorDataRequest> measurements) {
        sensorDataService.ingest(measurements);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
