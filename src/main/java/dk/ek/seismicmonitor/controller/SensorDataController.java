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

@RestController
@RequestMapping("/api/sensor-data")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody List<SensorDataRequest> measurements) {
        sensorDataService.ingest(measurements);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
