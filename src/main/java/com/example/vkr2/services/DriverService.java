package com.example.vkr2.services;

import com.example.vkr2.DTO.DriverRequest;
import com.example.vkr2.DTO.DriverResponse;
import com.example.vkr2.entity.Driver;
import com.example.vkr2.repository.DriverRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponse addDriver(Driver driver) {
        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DriverResponse updateDriver(Long id, Driver driverDetails) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Водитель с ID " + id + " не найден"));
        driver.setFullName(driverDetails.getFullName());
        driver.setPhoneNumber(driverDetails.getPhoneNumber());
        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    @Transactional
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new EntityNotFoundException("Водитель с ID " + id + " не найден");
        }
        driverRepository.deleteById(id);
    }

    private DriverResponse mapToResponse(Driver driver) {
        boolean hasCar = driver.getCar() != null;
        return new DriverResponse(
                driver.getId(),
                driver.getFullName(),
                driver.getPhoneNumber(),
                hasCar
        );
    }
}