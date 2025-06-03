package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.*;
import com.example.vkr2.services.ReminderService;
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
import java.util.Map;

@RestController
@RequestMapping("/admin/reminders")
@RequiredArgsConstructor
@Tag(name = "Управление напоминаниями")
public class ReminderController {

    private static final Logger logger = LoggerFactory.getLogger(ReminderController.class);
    private final ReminderService reminderService;

    @Operation(summary = "Создать или обновить настройки напоминаний")
    @PostMapping("/settings")
    public ResponseEntity<ReminderSettingsResponse> createOrUpdateSettings(@RequestBody @Valid ReminderSettingsRequest request) {
        try {
            logger.info("Creating/updating reminder settings for car ID: {}", request.getCarId());
            ReminderSettingsResponse response = reminderService.createOrUpdateReminderSettings(request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error creating/updating reminder settings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все настройки напоминаний")
    @GetMapping("/settings")
    public ResponseEntity<List<ReminderSettingsResponse>> getAllReminderSettings() {
        try {
            logger.info("Fetching all reminder settings");
            List<ReminderSettingsResponse> settings = reminderService.getAllReminderSettings();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            logger.error("Error fetching reminder settings: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить настройки напоминаний для автомобиля")
    @GetMapping("/settings/car/{carId}")
    public ResponseEntity<ReminderSettingsResponse> getSettingsByCarId(@PathVariable Long carId) {
        try {
            logger.info("Fetching reminder settings for car ID: {}", carId);
            ReminderSettingsResponse response = reminderService.getReminderSettingsByCarId(carId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Settings not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error fetching reminder settings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Проверить есть ли настройки для автомобиля")
    @GetMapping("/settings/car/{carId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkSettingsExist(@PathVariable Long carId) {
        try {
            boolean exists = reminderService.hasReminderSettings(carId);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            logger.error("Error checking settings existence: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }

    @Operation(summary = "Получить все напоминания")
    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getAllReminders() {
        try {
            logger.info("Fetching all reminders");
            List<ReminderResponse> reminders = reminderService.getAllReminders();
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            logger.error("Error fetching reminders: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить напоминания требующие внимания")
    @GetMapping("/attention")
    public ResponseEntity<List<ReminderResponse>> getRemindersRequiringAttention() {
        try {
            logger.info("Fetching reminders requiring attention");
            List<ReminderResponse> reminders = reminderService.getRemindersRequiringAttention();
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            logger.error("Error fetching reminders requiring attention: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить просроченные напоминания")
    @GetMapping("/overdue")
    public ResponseEntity<List<ReminderResponse>> getOverdueReminders() {
        try {
            logger.info("Fetching overdue reminders");
            List<ReminderResponse> reminders = reminderService.getOverdueReminders();
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            logger.error("Error fetching overdue reminders: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить напоминание для автомобиля")
    @GetMapping("/car/{carId}")
    public ResponseEntity<ReminderResponse> getReminderByCarId(@PathVariable Long carId) {
        try {
            logger.info("Fetching reminder for car ID: {}", carId);
            ReminderResponse response = reminderService.getReminderByCarId(carId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Car not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error fetching reminder: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить детальное напоминание с статусом уведомлений")
    @GetMapping("/car/{carId}/detailed")
    public ResponseEntity<ReminderResponse> getDetailedReminderByCarId(@PathVariable Long carId) {
        try {
            logger.info("Fetching detailed reminder for car ID: {}", carId);
            ReminderResponse response = reminderService.getReminderWithNotificationStatus(carId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Car not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error fetching detailed reminder: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Принудительная проверка ТО для автомобиля")
    @PostMapping("/car/{carId}/check")
    public ResponseEntity<Map<String, String>> triggerMaintenanceCheck(@PathVariable Long carId) {
        try {
            logger.info("Triggering maintenance check for car ID: {}", carId);
            reminderService.triggerMaintenanceCheckForCar(carId);
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "message", "Проверка ТО для автомобиля ID " + carId + " выполнена"
            ));
        } catch (EntityNotFoundException e) {
            logger.error("Car not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Автомобиль не найден"));
        } catch (Exception e) {
            logger.error("Error triggering maintenance check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при проверке ТО"));
        }
    }

    @Operation(summary = "Принудительная проверка ТО для всех автомобилей")
    @PostMapping("/check-all")
    public ResponseEntity<Map<String, String>> triggerMaintenanceCheckForAll() {
        try {
            logger.info("Triggering maintenance check for all cars");
            reminderService.triggerMaintenanceCheckForAllCars();
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "message", "Проверка ТО для всех автомобилей выполнена"
            ));
        } catch (Exception e) {
            logger.error("Error triggering maintenance check for all cars: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при массовой проверке ТО"));
        }
    }

    @Operation(summary = "Удалить настройки напоминаний")
    @DeleteMapping("/settings/car/{carId}")
    public ResponseEntity<Void> deleteSettings(@PathVariable Long carId) {
        try {
            logger.info("Deleting reminder settings for car ID: {}", carId);
            reminderService.deleteReminderSettings(carId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Settings not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting reminder settings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}