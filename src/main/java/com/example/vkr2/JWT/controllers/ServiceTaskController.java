package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.ServiceTaskRequest;
import com.example.vkr2.DTO.ServiceTaskResponse;
import com.example.vkr2.services.ServiceTaskService;
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
@RequestMapping("/admin/service-tasks")
@RequiredArgsConstructor
@Tag(name = "Управление сервисными задачами")
public class ServiceTaskController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTaskController.class);
    private final ServiceTaskService serviceTaskService;

    @Operation(summary = "Добавить сервисную задачу")
    @PostMapping
    public ResponseEntity<ServiceTaskResponse> addServiceTask(@RequestBody @Valid ServiceTaskRequest request) {
        try {
            logger.info("Добавление сервисной задачи: {}", request);
            ServiceTaskResponse response = serviceTaskService.addServiceTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при добавлении сервисной задачи - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при добавлении сервисной задачи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Обновить сервисную задачу")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceTaskResponse> updateServiceTask(@PathVariable Long id, @RequestBody @Valid ServiceTaskRequest request) {
        try {
            logger.info("Обновление сервисной задачи с ID {}: {}", id, request);
            ServiceTaskResponse response = serviceTaskService.updateServiceTask(id, request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Ошибка при обновлении сервисной задачи - сущность не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при обновлении сервисной задачи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все сервисные задачи")
    @GetMapping
    public ResponseEntity<List<ServiceTaskResponse>> getAllServiceTasks() {
        try {
            logger.info("Получение всех сервисных задач");
            List<ServiceTaskResponse> tasks = serviceTaskService.getAllServiceTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Ошибка при получении списка сервисных задач: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить сервисные задачи с фильтрами")
    @GetMapping("/filter")
    public ResponseEntity<List<ServiceTaskResponse>> getServiceTasksWithFilters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long serviceRecordId) {
        try {
            logger.info("Получение сервисных задач с фильтрами");
            List<ServiceTaskResponse> tasks = serviceTaskService.getServiceTasksWithFilters(search, serviceRecordId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Ошибка при получении отфильтрованного списка сервисных задач: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить сервисную задачу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceTaskResponse> getServiceTaskById(@PathVariable Long id) {
        try {
            logger.info("Получение сервисной задачи по ID: {}", id);
            ServiceTaskResponse response = serviceTaskService.getServiceTaskById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Сервисная задача не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при получении сервисной задачи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удалить сервисную задачу")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceTask(@PathVariable Long id) {
        try {
            logger.info("Удаление сервисной задачи с ID: {}", id);
            serviceTaskService.deleteServiceTask(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Сервисная задача не найдена: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при удалении сервисной задачи: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}