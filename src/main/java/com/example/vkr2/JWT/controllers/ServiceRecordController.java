package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.ServiceRecordRequest;
import com.example.vkr2.DTO.ServiceRecordResponse;
import com.example.vkr2.services.ServiceRecordService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/admin/service-records")
@RequiredArgsConstructor
@Tag(name = "Управление сервисными записями")
public class ServiceRecordController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRecordController.class);
    private final ServiceRecordService serviceRecordService;

    @Operation(summary = "Добавить сервисную запись")
    @PostMapping
    public ResponseEntity<ServiceRecordResponse> addServiceRecord(@RequestBody @Valid ServiceRecordRequest request) {
        try {
            logger.info("Добавление сервисной записи: {}", request);
            ServiceRecordResponse response = serviceRecordService.addServiceRecord(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при добавлении сервисной записи - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при добавлении сервисной записи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Обновить сервисную запись")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecordResponse> updateServiceRecord(@PathVariable Long id, @RequestBody @Valid ServiceRecordRequest request) {
        try {
            logger.info("Обновление сервисной записи с ID {}: {}", id, request);
            ServiceRecordResponse response = serviceRecordService.updateServiceRecord(id, request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при обновлении сервисной записи - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при обновлении сервисной записи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все сервисные записи")
    @GetMapping
    public ResponseEntity<List<ServiceRecordResponse>> getAllServiceRecords() {
        try {
            logger.info("Получение всех сервисных записей");
            List<ServiceRecordResponse> records = serviceRecordService.getAllServiceRecords();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Ошибка при получении списка сервисных записей: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить сервисные записи с фильтрами")
    @GetMapping("/filter")
    public ResponseEntity<List<ServiceRecordResponse>> getServiceRecordsWithFilters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long carId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost) {
        try {
            logger.info("Получение сервисных записей с фильтрами");
            List<ServiceRecordResponse> records = serviceRecordService.getServiceRecordsWithFilters(
                    search, carId, startDate, endDate, minCost, maxCost);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Ошибка при получении отфильтрованного списка сервисных записей: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить сервисную запись по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecordResponse> getServiceRecordById(@PathVariable Long id) {
        try {
            logger.info("Получение сервисной записи по ID: {}", id);
            ServiceRecordResponse response = serviceRecordService.getServiceRecordById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Сервисная запись не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при получении сервисной записи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удалить сервисную запись")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceRecord(@PathVariable Long id) {
        try {
            logger.info("Удаление сервисной записи с ID: {}", id);
            serviceRecordService.deleteServiceRecord(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Сервисная запись не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при удалении сервисной записи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}