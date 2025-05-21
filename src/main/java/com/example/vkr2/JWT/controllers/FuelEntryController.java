package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.FuelEntryRequest;
import com.example.vkr2.DTO.FuelEntryResponse;
import com.example.vkr2.services.FuelEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;

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
}