package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.MaintenanceOperationRequest;
import com.example.vkr2.DTO.MaintenanceOperationResponse;
import com.example.vkr2.services.MaintenanceOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/cars/{carId}/maintenance")
@RequiredArgsConstructor
@Tag(name = "Управление операциями техобслуживания")
public class MaintenanceOperationController {

    private final MaintenanceOperationService maintenanceOperationService;

    @Operation(summary = "Добавить операцию ТО для автомобиля")
    @PostMapping
    public ResponseEntity<MaintenanceOperationResponse> addMaintenanceOperation(
            @PathVariable Long carId,
            @RequestBody @Valid MaintenanceOperationRequest request) {
        MaintenanceOperationResponse response = maintenanceOperationService.addMaintenanceOperation(carId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить все операции ТО для автомобиля")
    @GetMapping
    public ResponseEntity<List<MaintenanceOperationResponse>> getMaintenanceOperations(@PathVariable Long carId) {
        List<MaintenanceOperationResponse> operations = maintenanceOperationService.getMaintenanceOperationsByCarId(carId);
        return ResponseEntity.ok(operations);
    }

    @Operation(summary = "Обновить операцию ТО")
    @PutMapping("/{operationId}")
    public ResponseEntity<MaintenanceOperationResponse> updateMaintenanceOperation(
            @PathVariable Long carId,
            @PathVariable Long operationId,
            @RequestBody @Valid MaintenanceOperationRequest request) {
        MaintenanceOperationResponse response = maintenanceOperationService.updateMaintenanceOperation(carId, operationId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить операцию ТО")
    @DeleteMapping("/{operationId}")
    public ResponseEntity<Void> deleteMaintenanceOperation(@PathVariable Long carId, @PathVariable Long operationId) {
        maintenanceOperationService.deleteMaintenanceOperation(carId, operationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить общие затраты за период")
    @GetMapping("/total-cost")
    public ResponseEntity<Double> getTotalCostForPeriod(
            @PathVariable Long carId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Double totalCost = maintenanceOperationService.calculateTotalCostForPeriod(carId, startDate, endDate);
        return ResponseEntity.ok(totalCost);
    }

    @Operation(summary = "Получить среднюю стоимость ТО")
    @GetMapping("/avg-cost")
    public ResponseEntity<Double> getAvgMaintenanceCost(@PathVariable Long carId) {
        Double avgCost = maintenanceOperationService.calculateAvgMaintenanceCost(carId);
        return ResponseEntity.ok(avgCost);
    }

    @Operation(summary = "Получить прогноз затрат")
    @GetMapping("/forecast")
    public ResponseEntity<Double> getForecastCost(
            @PathVariable Long carId,
            @RequestParam int numberOfPlannedMaintenances) {
        Double forecast = maintenanceOperationService.forecastCost(carId, numberOfPlannedMaintenances);
        return ResponseEntity.ok(forecast);
    }
}