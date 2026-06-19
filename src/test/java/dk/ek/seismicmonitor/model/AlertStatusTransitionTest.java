package dk.ek.seismicmonitor.model;

import dk.ek.seismicmonitor.exception.IllegalAlertTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertStatusTransitionTest {

    private EarthquakeAlert newAlert() {
        return new EarthquakeAlert(55.0, 12.0, 4.0, "Copenhagen");
    }

    @Test
    void underReview_canBecomeActive() {
        EarthquakeAlert alert = newAlert();
        alert.changeStatusTo(AlertStatus.ACTIVE);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACTIVE);
    }

    @Test
    void underReview_canBecomeFalseAlarm() {
        EarthquakeAlert alert = newAlert();
        alert.changeStatusTo(AlertStatus.FALSE_ALARM);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.FALSE_ALARM);
    }

    @Test
    void active_canBecomeNotActive() {
        EarthquakeAlert alert = newAlert();
        alert.changeStatusTo(AlertStatus.ACTIVE);
        alert.changeStatusTo(AlertStatus.NOT_ACTIVE);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.NOT_ACTIVE);
    }

    @Test
    void illegalJump_isRejected() {
        EarthquakeAlert alert = newAlert();
        assertThatThrownBy(() -> alert.changeStatusTo(AlertStatus.NOT_ACTIVE))
                .isInstanceOf(IllegalAlertTransitionException.class);
    }

    @Test
    void terminalState_cannotChange() {
        EarthquakeAlert alert = newAlert();
        alert.changeStatusTo(AlertStatus.FALSE_ALARM);
        assertThatThrownBy(() -> alert.changeStatusTo(AlertStatus.ACTIVE))
                .isInstanceOf(IllegalAlertTransitionException.class);
    }
}
