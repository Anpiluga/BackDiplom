package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.DriverRequest;
import com.example.vkr2.DTO.DriverResponse;
import com.example.vkr2.entity.Driver;
import com.example.vkr2.services.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@Tag(name = "Управление водителями")
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Добавить водителя")
    @PostMapping
    public ResponseEntity<DriverResponse> addDriver(@RequestBody @Valid DriverRequest request) {
        System.out.println("Adding driver: " + request);
        Driver driver = new Driver();
        driver.setFullName(request.getFullName());
        driver.setPhoneNumber(request.getPhoneNumber());
        DriverResponse savedDriver = driverService.addDriver(driver);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDriver);
    }

    @Operation(summary = "Получить всех водителей")
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        System.out.println("Fetching all drivers");
        List<DriverResponse> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    @Operation(summary = "Изменение информации о водителе")
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable Long id, @RequestBody @Valid DriverRequest request) {
        System.out.println("Updating driver with ID: " + id);
        Driver driver = new Driver();
        driver.setFullName(request.getFullName());
        driver.setPhoneNumber(request.getPhoneNumber());
        DriverResponse updatedDriver = driverService.updateDriver(id, driver);
        return ResponseEntity.ok(updatedDriver);
    }

    @Operation(summary = "Удаление водителя")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        System.out.println("Deleting driver with ID: " + id);
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<String> handleEntityNotFoundException(jakarta.persistence.EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Водитель с указанным ID не найден: " + ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Некорректные данные: " + ex.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка сервера: " + ex.getMessage());
    }
}