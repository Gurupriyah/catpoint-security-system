package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.image.service.ImageService;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static com.udacity.catpoint.data.AlarmStatus.*;


public class SecurityService {

    private final ImageService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    public void setArmingStatus(ArmingStatus armingStatus) {

        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(NO_ALARM);
        } else {
            securityRepository.getSensors()
                    .forEach(sensor -> {
                        sensor.setActive(false);
                        securityRepository.updateSensor(sensor);
                    });
        }

        securityRepository.setArmingStatus(armingStatus);

        if (armingStatus == ArmingStatus.ARMED_HOME &&
                imageService.imageContainsCat(null, 50.0f)) {
            setAlarmStatus(ALARM);
        }
    }

    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {

        if (securityRepository.getAlarmStatus() == ALARM) {
            return;
        }

        // No-op: deactivating inactive sensor
        if (!sensor.getActive() && !active) {
            return;
        }

        // Duplicate activation while pending → alarm
        if (sensor.getActive() && active &&
                securityRepository.getAlarmStatus() == PENDING_ALARM) {
            setAlarmStatus(ALARM);
            return;
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if (active) {
            handleSensorActivated();
        } else {
            handleSensorDeactivated();
        }
    }

    private void handleSensorActivated() {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) return;

        if (securityRepository.getAlarmStatus() == NO_ALARM) {
            setAlarmStatus(PENDING_ALARM);
        } else if (securityRepository.getAlarmStatus() == PENDING_ALARM) {
            setAlarmStatus(ALARM);
        }
    }


    private void handleSensorDeactivated() {
        if (securityRepository.getAlarmStatus() != PENDING_ALARM) return;

        boolean allInactive = securityRepository.getSensors()
                .stream()
                .noneMatch(Sensor::getActive);

        if (allInactive) {
            setAlarmStatus(NO_ALARM);
        }
    }


    public void processImage(BufferedImage image) {
        boolean catDetected = imageService.imageContainsCat(image, 50.0f);

        if (catDetected && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(ALARM);
        } else if (!catDetected &&
                securityRepository.getSensors().stream().noneMatch(Sensor::getActive)) {
            setAlarmStatus(NO_ALARM);
        }

        statusListeners.forEach(sl -> sl.catDetected(catDetected));
    }

    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }
    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }
    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }




}
