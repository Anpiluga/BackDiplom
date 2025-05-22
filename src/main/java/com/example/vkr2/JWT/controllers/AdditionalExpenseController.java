package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.AdditionalExpenseRequest;
import com.example.vkr2.DTO.AdditionalExpenseResponse;
import com.example.vkr2.services.AdditionalExpenseService;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/additional-expenses")
@RequiredArgsConstructor
@Tag(name = "Управление дополнительными расходами")
public class AdditionalExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(AdditionalExpenseController.class);
    private final AdditionalExpenseService additionalExpenseService;

    @Operation(summary = "Добавить дополнительный расход")
    @PostMapping
    public ResponseEntity<AdditionalExpenseResponse> addAdditionalExpense(@RequestBody @Valid AdditionalExpenseRequest request) {
        try {
            logger.info("Добавление дополнительного расхода: {}", request);
            AdditionalExpenseResponse response = additionalExpenseService.addAdditionalExpense(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при добавлении дополнительного расхода - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при добавлении дополнительного расхода: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Обновить дополнительный расход")
    @PutMapping("/{id}")
    public ResponseEntity<AdditionalExpenseResponse> updateAdditionalExpense(@PathVariable Long id, @RequestBody @Valid AdditionalExpenseRequest request) {
        try {
            logger.info("Обновление дополнительного расхода с ID {}: {}", id, request);
            AdditionalExpenseResponse response = additionalExpenseService.updateAdditionalExpense(id, request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при обновлении дополнительного расхода - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при обновлении дополнительного расхода: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все дополнительные расходы")
    @GetMapping
    public ResponseEntity<List<AdditionalExpenseResponse>> getAllAdditionalExpenses() {
        try {
            logger.info("Получение всех дополнительных расходов");
            List<AdditionalExpenseResponse> expenses = additionalExpenseService.getAllAdditionalExpenses();
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            logger.error("Ошибка при получении списка дополнительных расходов: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить дополнительный расход по ID")
    @GetMapping("/{id}")
    public ResponseEntity<AdditionalExpenseResponse> getAdditionalExpenseById(@PathVariable Long id) {
        try {
            logger.info("Получение дополнительного расхода по ID: {}", id);
            AdditionalExpenseResponse response = additionalExpenseService.getAdditionalExpenseById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Дополнительный расход не найден: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при получении дополнительного расхода: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удалить дополнительный расход")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdditionalExpense(@PathVariable Long id) {
        try {
            logger.info("Удаление дополнительного расхода с ID: {}", id);
            additionalExpenseService.deleteAdditionalExpense(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Дополнительный расход не найден: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при удалении дополнительного расхода: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Получить дополнительные расходы по автомобилю")
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<AdditionalExpenseResponse>> getExpensesByCarId(@PathVariable Long carId) {
        try {
            logger.info("Получение дополнительных расходов для автомобиля ID: {}", carId);
            List<AdditionalExpenseResponse> expenses = additionalExpenseService.getExpensesByCarId(carId);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            logger.error("Ошибка при получении расходов по автомобилю: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить расходы по диапазону дат")
    @GetMapping("/date-range")
    public ResponseEntity<List<AdditionalExpenseResponse>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.info("Получение расходов в диапазоне дат: {} - {}", startDate, endDate);
            List<AdditionalExpenseResponse> expenses = additionalExpenseService.getExpensesByDateRange(startDate, endDate);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            logger.error("Ошибка при получении расходов по диапазону дат: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}