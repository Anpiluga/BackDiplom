package com.example.vkr2.services;

import com.example.vkr2.DTO.MaintenanceOperationRequest;
import com.example.vkr2.DTO.MaintenanceOperationResponse;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.MaintenanceOperation;
import com.example.vkr2.repository.CarRepository;
import com.example.vkr2.repository.MaintenanceOperationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceOperationService {

    private final MaintenanceOperationRepository maintenanceOperationRepository;
    private final CarRepository carRepository;

    @Transactional
    public MaintenanceOperationResponse addMaintenanceOperation(Long carId, MaintenanceOperationRequest request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

        MaintenanceOperation operation = MaintenanceOperation.builder()
                .car(car)
                .maintenanceType(request.getMaintenanceType())
                .lastMaintenanceDate(request.getLastMaintenanceDate())
                .lastMaintenanceMileage(request.getLastMaintenanceMileage())
                .maintenanceIntervalKm(request.getMaintenanceIntervalKm())
                .maintenanceIntervalMonths(request.getMaintenanceIntervalMonths())
                .laborCost(request.getLaborCost())
                .partsCost(request.getPartsCost())
                .build();

        calculateMaintenanceDatesAndMileage(operation);
        calculateTotalCost(operation);

        MaintenanceOperation savedOperation = maintenanceOperationRepository.save(operation);
        return mapToResponse(savedOperation);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceOperationResponse> getMaintenanceOperationsByCarId(Long carId) {
        List<MaintenanceOperation> operations = maintenanceOperationRepository.findByCarId(carId);
        return operations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaintenanceOperationResponse updateMaintenanceOperation(Long carId, Long operationId, MaintenanceOperationRequest request) {
        MaintenanceOperation operation = maintenanceOperationRepository.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException("Операция с ID " + operationId + " не найдена"));

        if (!operation.getCar().getId().equals(carId)) {
            throw new IllegalArgumentException("Операция не принадлежит указанному автомобилю");
        }

        operation.setMaintenanceType(request.getMaintenanceType());
        operation.setLastMaintenanceDate(request.getLastMaintenanceDate());
        operation.setLastMaintenanceMileage(request.getLastMaintenanceMileage());
        operation.setMaintenanceIntervalKm(request.getMaintenanceIntervalKm());
        operation.setMaintenanceIntervalMonths(request.getMaintenanceIntervalMonths());
        operation.setLaborCost(request.getLaborCost());
        operation.setPartsCost(request.getPartsCost());

        calculateMaintenanceDatesAndMileage(operation);
        calculateTotalCost(operation);

        MaintenanceOperation updatedOperation = maintenanceOperationRepository.save(operation);
        return mapToResponse(updatedOperation);
    }

    @Transactional
    public void deleteMaintenanceOperation(Long carId, Long operationId) {
        MaintenanceOperation operation = maintenanceOperationRepository.findById(operationId)
                .orElseThrow(() -> new EntityNotFoundException("Операция с ID " + operationId + " не найдена"));

        if (!operation.getCar().getId().equals(carId)) {
            throw new IllegalArgumentException("Операция не принадлежит указанному автомобилю");
        }

        maintenanceOperationRepository.delete(operation);
    }

    // 3.1 Расчёт следующего ТО
    private void calculateMaintenanceDatesAndMileage(MaintenanceOperation operation) {
        if (operation.getLastMaintenanceDate() != null && operation.getMaintenanceIntervalMonths() != null) {
            operation.setNextMaintenanceDate(operation.getLastMaintenanceDate().plusMonths(operation.getMaintenanceIntervalMonths()));
        }
        if (operation.getLastMaintenanceMileage() != null && operation.getMaintenanceIntervalKm() != null) {
            operation.setNextMaintenanceMileage(operation.getLastMaintenanceMileage() + operation.getMaintenanceIntervalKm());
        }

        // Ориентировочная дата по среднему пробегу
        Car car = operation.getCar();
        if (car.getOdometr() != null && operation.getNextMaintenanceMileage() != null) {
            int avgDailyMileage = 200; // Можно сделать конфигурируемым
            int days = (operation.getNextMaintenanceMileage() - car.getOdometr()) / avgDailyMileage;
            if (days > 0) {
                operation.setNextMaintenanceDate(LocalDate.now().plusDays(days));
            }
        }
    }

    // 3.2 Расчёт общей стоимости ТО
    private void calculateTotalCost(MaintenanceOperation operation) {
        double laborCost = operation.getLaborCost() != null ? operation.getLaborCost() : 0.0;
        double partsCost = operation.getPartsCost() != null ? operation.getPartsCost() : 0.0;
        operation.setTotalCost(laborCost + partsCost);
    }

    // 3.3 Расчёт затрат за период
    public Double calculateTotalCostForPeriod(Long carId, LocalDate startDate, LocalDate endDate) {
        List<MaintenanceOperation> operations = maintenanceOperationRepository.findByCarId(carId)
                .stream()
                .filter(op -> op.getCreatedAt() != null &&
                        !op.getCreatedAt().isBefore(startDate) &&
                        !op.getCreatedAt().isAfter(endDate))
                .toList();
        return operations.stream()
                .mapToDouble(op -> op.getTotalCost() != null ? op.getTotalCost() : 0.0)
                .sum();
    }

    // 3.4 Расчёт средней стоимости ТО
    public Double calculateAvgMaintenanceCost(Long carId) {
        List<MaintenanceOperation> operations = maintenanceOperationRepository.findByCarId(carId);
        long count = operations.stream().filter(op -> op.getTotalCost() != null).count();
        if (count == 0) return 0.0;
        return operations.stream()
                .mapToDouble(op -> op.getTotalCost() != null ? op.getTotalCost() : 0.0)
                .average()
                .orElse(0.0);
    }

    // 3.5 Прогноз затрат
    public Double forecastCost(Long carId, int numberOfPlannedMaintenances) {
        Double avgCost = calculateAvgMaintenanceCost(carId);
        return avgCost * numberOfPlannedMaintenances;
    }

    private MaintenanceOperationResponse mapToResponse(MaintenanceOperation operation) {
        MaintenanceOperationResponse response = new MaintenanceOperationResponse();
        response.setId(operation.getId());
        response.setMaintenanceType(operation.getMaintenanceType());
        response.setLastMaintenanceDate(operation.getLastMaintenanceDate());
        response.setLastMaintenanceMileage(operation.getLastMaintenanceMileage());
        response.setMaintenanceIntervalKm(operation.getMaintenanceIntervalKm());
        response.setMaintenanceIntervalMonths(operation.getMaintenanceIntervalMonths());
        response.setNextMaintenanceDate(operation.getNextMaintenanceDate());
        response.setNextMaintenanceMileage(operation.getNextMaintenanceMileage());
        response.setLaborCost(operation.getLaborCost());
        response.setPartsCost(operation.getPartsCost());
        response.setTotalCost(operation.getTotalCost());
        response.setCreatedAt(operation.getCreatedAt());
        return response;
    }
}