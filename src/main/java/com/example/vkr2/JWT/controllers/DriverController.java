package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.DriverRequest;
import com.example.vkr2.DTO.DriverResponse;
import com.example.vkr2.entity.Driver;
import com.example.vkr2.services.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@Tag(name = "Управление водителями")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);
    private final DriverService driverService;

    @Operation(summary = "Добавить водителя")
    @PostMapping
    public ResponseEntity<DriverResponse> addDriver(@RequestBody @Valid DriverRequest request) {
        try {
            logger.info("Adding driver: {}", request);
            Driver driver = new Driver();
            // Объединяем поля firstName, lastName и middleName в одно поле fullName
            StringBuilder fullNameBuilder = new StringBuilder();
            if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
                fullNameBuilder.append(request.getFirstName()).append(" ");
            }
            if (request.getLastName() != null && !request.getLastName().isEmpty()) {
                fullNameBuilder.append(request.getLastName()).append(" ");
            }
            if (request.getMiddleName() != null && !request.getMiddleName().isEmpty()) {
                fullNameBuilder.append(request.getMiddleName());
            }

            driver.setFullName(fullNameBuilder.toString().trim());
            driver.setPhoneNumber(request.getPhoneNumber());
            DriverResponse savedDriver = driverService.addDriver(driver);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDriver);
        } catch (Exception e) {
            logger.error("Error adding driver", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить всех водителей")
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        try {
            logger.info("Fetching all drivers");
            List<DriverResponse> drivers = driverService.getAllDrivers();
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            logger.error("Error fetching drivers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить водителей с фильтром по наличию автомобиля")
    @GetMapping("/filter")
    public ResponseEntity<List<DriverResponse>> getDriversWithCarFilter(
            @RequestParam(required = false) Boolean hasCar) {
        try {
            logger.info("Fetching drivers with car filter: {}", hasCar);
            List<DriverResponse> drivers = driverService.getDriversWithCarFilter(hasCar);
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            logger.error("Error fetching drivers with filter", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить водителя по ID")
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        try {
            logger.info("Fetching driver with ID: {}", id);
            DriverResponse driver = driverService.getDriverById(id);
            return ResponseEntity.ok(driver);
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            logger.error("Driver not found", ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error fetching driver", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Изменение информации о водителе")
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable Long id, @RequestBody @Valid DriverRequest request) {
        try {
            logger.info("Updating driver with ID: {}", id);
            Driver driver = new Driver();

            // Объединяем поля в fullName
            StringBuilder fullNameBuilder = new StringBuilder();
            if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
                fullNameBuilder.append(request.getFirstName()).append(" ");
            }
            if (request.getLastName() != null && !request.getLastName().isEmpty()) {
                fullNameBuilder.append(request.getLastName()).append(" ");
            }
            if (request.getMiddleName() != null && !request.getMiddleName().isEmpty()) {
                fullNameBuilder.append(request.getMiddleName());
            }

            driver.setFullName(fullNameBuilder.toString().trim());
            driver.setPhoneNumber(request.getPhoneNumber());
            DriverResponse updatedDriver = driverService.updateDriver(id, driver);
            return ResponseEntity.ok(updatedDriver);
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            logger.error("Driver not found", ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error updating driver", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удаление водителя")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        try {
            logger.info("Deleting driver with ID: {}", id);
            driverService.deleteDriver(id);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            logger.error("Driver not found", ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting driver", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}