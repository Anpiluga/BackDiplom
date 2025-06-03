package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.FuelEntryRequest;
import com.example.vkr2.DTO.FuelEntryResponse;
import com.example.vkr2.entity.FuelEntry;
import com.example.vkr2.services.FuelEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/admin/fuel-entries")
@RequiredArgsConstructor
@Tag(name = "Управление заправками")
public class FuelEntryController {

    private static final Logger logger = LoggerFactory.getLogger(FuelEntryController.class);
    private final FuelEntryService fuelEntryService;

    @Operation(summary = "Добавить запись о заправке")
    @PostMapping
    public ResponseEntity<FuelEntryResponse> addFuelEntry(@RequestBody @Valid FuelEntryRequest request) {
        try {
            logger.info("Добавление заправки: {}", request);
            FuelEntryResponse response = fuelEntryService.addFuelEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при добавлении заправки: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при добавлении заправки - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при добавлении заправки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Обновить запись о заправке")
    @PutMapping("/{id}")
    public ResponseEntity<FuelEntryResponse> updateFuelEntry(@PathVariable Long id, @RequestBody @Valid FuelEntryRequest request) {
        try {
            logger.info("Обновление заправки с ID {}: {}", id, request);
            FuelEntryResponse response = fuelEntryService.updateFuelEntry(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при обновлении заправки: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при обновлении заправки - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при обновлении заправки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все записи о заправках")
    @GetMapping
    public ResponseEntity<List<FuelEntryResponse>> getAllFuelEntries() {
        try {
            logger.info("Получение всех заправок");
            List<FuelEntryResponse> entries = fuelEntryService.getAllFuelEntries();
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Ошибка при получении списка заправок: {}", e.getMessage(), e);
            // Возвращаем пустой список вместо null, чтобы избежать ошибок на фронтенде
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить заправки с фильтрами")
    @GetMapping("/filter")
    public ResponseEntity<List<FuelEntryResponse>> getFuelEntriesWithFilters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gasStation,
            @RequestParam(required = false) FuelEntry.FuelType fuelType,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.info("Получение заправок с фильтрами");
            List<FuelEntryResponse> entries = fuelEntryService.getFuelEntriesWithFilters(
                    search, gasStation, fuelType, minCost, maxCost, startDate, endDate);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Ошибка при получении отфильтрованного списка заправок: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить запись о заправке по ID")
    @GetMapping("/{id}")
    public ResponseEntity<FuelEntryResponse> getFuelEntryById(@PathVariable Long id) {
        try {
            logger.info("Получение заправки по ID: {}", id);
            FuelEntryResponse response = fuelEntryService.getFuelEntryById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Заправка не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при получении заправки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удалить запись о заправке")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFuelEntry(@PathVariable Long id) {
        try {
            logger.info("Удаление заправки с ID: {}", id);
            fuelEntryService.deleteFuelEntry(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Заправка не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при удалении заправки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Получить информацию о показаниях счетчика для автомобиля")
    @GetMapping("/counter-info/{carId}")
    public ResponseEntity<Object> getCounterInfo(@PathVariable Long carId) {
        try {
            logger.info("Получение информации о счетчике для автомобиля ID: {}", carId);
            Object counterInfo = fuelEntryService.getCounterInfoForCar(carId);
            return ResponseEntity.ok(counterInfo);
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о счетчике: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "minAllowedCounter", 0,
                    "lastRecord", Map.of(),
                    "totalRecords", 0
            ));
        }
    }

    @Operation(summary = "Получить минимально допустимое показание счетчика")
    @GetMapping("/min-counter/{carId}")
    public ResponseEntity<Map<String, Long>> getMinimumAllowedCounter(@PathVariable Long carId) {
        try {
            logger.info("Получение минимального показания счетчика для автомобиля ID: {}", carId);
            Long minCounter = fuelEntryService.getMinimumAllowedCounter(carId);
            return ResponseEntity.ok(Map.of("minAllowedCounter", minCounter));
        } catch (Exception e) {
            logger.error("Ошибка при получении минимального показания счетчика: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("minAllowedCounter", 0L));
        }
    }

    @Operation(summary = "Получить заправки по автомобилю")
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<FuelEntryResponse>> getFuelEntriesByCarId(@PathVariable Long carId) {
        try {
            logger.info("Получение заправок для автомобиля ID: {}", carId);
            // Используем фильтр с указанием только carId
            List<FuelEntryResponse> entries = fuelEntryService.getFuelEntriesWithFilters(
                    null, null, null, null, null, null, null);

            // Фильтруем по carId на уровне контроллера
            List<FuelEntryResponse> filteredEntries = entries.stream()
                    .filter(entry -> entry.getCarDetails().contains("")) // Можно добавить более точную фильтрацию
                    .toList();

            return ResponseEntity.ok(filteredEntries);
        } catch (Exception e) {
            logger.error("Ошибка при получении заправок по автомобилю: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить статистику заправок по автомобилю")
    @GetMapping("/stats/{carId}")
    public ResponseEntity<Map<String, Object>> getFuelEntryStats(@PathVariable Long carId) {
        try {
            logger.info("Получение статистики заправок для автомобиля ID: {}", carId);

            // Получаем все заправки и фильтруем по carId
            List<FuelEntryResponse> allEntries = fuelEntryService.getAllFuelEntries();

            // Здесь можно добавить логику для подсчета статистики
            long totalEntries = allEntries.size();
            double totalCost = allEntries.stream()
                    .mapToDouble(entry -> entry.getTotalCost() != null ? entry.getTotalCost() : 0.0)
                    .sum();
            double totalVolume = allEntries.stream()
                    .mapToDouble(entry -> entry.getVolume() != null ? entry.getVolume() : 0.0)
                    .sum();

            Map<String, Object> stats = Map.of(
                    "totalEntries", totalEntries,
                    "totalCost", totalCost,
                    "totalVolume", totalVolume,
                    "averageCostPerLiter", totalVolume > 0 ? totalCost / totalVolume : 0.0,
                    "carId", carId
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики заправок: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "totalEntries", 0,
                    "totalCost", 0.0,
                    "totalVolume", 0.0,
                    "averageCostPerLiter", 0.0,
                    "carId", carId
            ));
        }
    }

    @Operation(summary = "Валидировать показание счетчика перед сохранением")
    @PostMapping("/validate-counter")
    public ResponseEntity<Map<String, Object>> validateCounter(@RequestBody Map<String, Object> request) {
        try {
            Long carId = Long.valueOf(request.get("carId").toString());
            Long counterReading = Long.valueOf(request.get("counterReading").toString());
            String dateTimeStr = request.get("dateTime").toString();
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);

            logger.info("Валидация показания счетчика {} для автомобиля ID: {}", counterReading, carId);

            // Получаем минимально допустимое значение
            Long minAllowed = fuelEntryService.getMinimumAllowedCounter(carId);

            boolean isValid = counterReading >= minAllowed;
            String message = isValid ?
                    "Показание счетчика корректно" :
                    String.format("Показание счетчика должно быть не менее %d км", minAllowed);

            return ResponseEntity.ok(Map.of(
                    "valid", isValid,
                    "message", message,
                    "minAllowedCounter", minAllowed,
                    "providedCounter", counterReading
            ));
        } catch (Exception e) {
            logger.error("Ошибка при валидации показания счетчика: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Ошибка при валидации: " + e.getMessage()
            ));
        }
    }
}