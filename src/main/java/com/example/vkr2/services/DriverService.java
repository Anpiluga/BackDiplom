package com.example.vkr2.services;

import com.example.vkr2.DTO.DriverRequest;
import com.example.vkr2.DTO.DriverResponse;
import com.example.vkr2.entity.Driver;
import com.example.vkr2.repository.DriverRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);
    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponse addDriver(Driver driver) {
        logger.info("Adding driver: {}", driver.getFullName());
        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        logger.info("Fetching all drivers");
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DriverResponse updateDriver(Long id, Driver driverDetails) {
        logger.info("Updating driver with ID: {}", id);
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Водитель с ID " + id + " не найден"));
        driver.setFullName(driverDetails.getFullName());
        driver.setPhoneNumber(driverDetails.getPhoneNumber());
        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    @Transactional
    public void deleteDriver(Long id) {
        logger.info("Deleting driver with ID: {}", id);
        if (!driverRepository.existsById(id)) {
            throw new EntityNotFoundException("Водитель с ID " + id + " не найден");
        }
        driverRepository.deleteById(id);
    }

    private DriverResponse mapToResponse(Driver driver) {
        // Разбиваем полное имя на части для фронтенда
        String fullName = driver.getFullName();
        String[] parts = new String[]{"", "", ""};

        if (fullName != null) {
            String[] nameParts = fullName.split("\\s+", 3);
            for (int i = 0; i < nameParts.length && i < 3; i++) {
                parts[i] = nameParts[i];
            }
        }

        boolean hasCar = driver.getCar() != null;

        return new DriverResponse(
                driver.getId(),
                parts[0], // firstName
                parts[1], // lastName
                parts[2], // middleName/patronymic
                driver.getPhoneNumber(),
                hasCar
        );
    }
}