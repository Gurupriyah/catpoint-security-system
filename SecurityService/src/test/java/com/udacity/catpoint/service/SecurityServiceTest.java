package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.*;
import com.udacity.catpoint.image.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    SecurityRepository securityRepository;

    @Mock
    ImageService imageService;

    @Mock
    private Sensor sensor;

    @Mock
    private StatusListener statusListener;

    SecurityService securityService;

    Sensor doorSensor;
    Sensor windowSensor;

    @BeforeEach
    void setup() {
        securityService = new SecurityService(securityRepository, imageService);

        doorSensor = new Sensor("Door", SensorType.DOOR);
        windowSensor = new Sensor("Window", SensorType.WINDOW);

        doorSensor.setActive(false);
        windowSensor.setActive(false);

        lenient().when(securityRepository.getSensors())
                .thenReturn(Set.of(doorSensor, windowSensor));

        lenient().when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        lenient().when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_AWAY);
    }


    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM"})
    void armedSystem_sensorActivated_escalatesAlarm(AlarmStatus initialStatus) {

        when(securityRepository.getAlarmStatus()).thenReturn(initialStatus);

        securityService.changeSensorActivationStatus(doorSensor, true);

        if (initialStatus == AlarmStatus.NO_ALARM) {
            verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        } else {
            verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    @Test
    void pendingAlarm_allSensorsInactive_returnsNoAlarm() {

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        doorSensor.setActive(true);

        securityService.changeSensorActivationStatus(doorSensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void alarmActive_sensorChange_doesNothing() {

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(doorSensor, true);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void activeSensorReactivated_pendingAlarm_setsAlarm() {

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        doorSensor.setActive(true);

        securityService.changeSensorActivationStatus(doorSensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void inactiveSensorDeactivated_noChange() {

        securityService.changeSensorActivationStatus(doorSensor, false);

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void armedHome_catDetected_setsAlarm() {

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(true);

        securityService.processImage(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCat_noActiveSensors_setsNoAlarm() {

        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(false);

        securityService.processImage(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void disarmedSystem_setsNoAlarm() {

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void armingSystem_resetsSensors() {

        doorSensor.setActive(true);
        windowSensor.setActive(true);

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        assertFalse(doorSensor.getActive());
        assertFalse(windowSensor.getActive());
    }

    @Test
    void armedHome_withExistingCat_setsAlarm() {

        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(true);

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    @Test
    void getSensors_returnsRepositorySensors() {
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor);
        when(securityRepository.getSensors()).thenReturn(sensors);

        assertEquals(1, securityService.getSensors().size());
    }

    @Test
    void addSensor_delegatesToRepository() {
        securityService.addSensor(sensor);

        verify(securityRepository).addSensor(sensor);
    }

    @Test
    void removeSensor_delegatesToRepository() {
        securityService.removeSensor(sensor);

        verify(securityRepository).removeSensor(sensor);
    }

    @Test
    void getAlarmStatus_returnsRepositoryValue() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    @Test
    void getArmingStatus_returnsRepositoryValue() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
    }

    @Test
    void addStatusListener_registersListenerSuccessfully() {
        securityService.addStatusListener(statusListener);
        securityService.setAlarmStatus(AlarmStatus.ALARM);
        verify(statusListener).notify(AlarmStatus.ALARM);
    }

}









