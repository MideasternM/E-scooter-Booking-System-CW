package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Scooter;
import org.example.escooter_booking_system.repository.ScooterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScooterServiceImpl implements ScooterService {

    @Autowired
    private ScooterRepository scooterRepository;

    @Override
    public Scooter addScooter(Scooter scooter) {
        return scooterRepository.save(scooter);
    }

    @Override
    public List<Scooter> getAllScooters() {
        return scooterRepository.findAll();
    }

    @Override
    public List<Scooter> getAvailableScooters() {
        return scooterRepository.findByIsAvailable(true);
    }

    @Override
    public Scooter getScooterById(Long id) {
        return scooterRepository.findById(id).orElse(null);
    }

    @Override
    public Scooter updateScooter(Long id, Scooter scooterDetails) {
        Scooter scooter = scooterRepository.findById(id).orElse(null);
        if (scooter != null) {
            scooter.setScooterCode(scooterDetails.getScooterCode());
            scooter.setModel(scooterDetails.getModel());
            scooter.setBatteryLevel(scooterDetails.getBatteryLevel());
            scooter.setLocation(scooterDetails.getLocation());
            scooter.setAvailable(scooterDetails.getAvailable());
            return scooterRepository.save(scooter);
        }
        return null;
    }

    @Override
    public void deleteScooter(Long id) {
        scooterRepository.deleteById(id);
    }
}
