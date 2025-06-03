package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.NotificationDTO;
import com.example.vkr2.entity.Notification;
import com.example.vkr2.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Управление уведомлениями")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    @Operation(summary = "Получить все активные уведомления с фильтрами")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getActiveNotifications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy) {
        try {
            List<NotificationDTO> notifications = notificationService.getActiveNotificationsWithFilters(
                    search, type, status, sortBy);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Ошибка при получении уведомлений: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @Operation(summary = "Получить все уведомления без фильтров")
    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех уведомлений: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @Operation(summary = "Получить количество непрочитанных уведомлений")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        try {
            long count = notificationService.countUnreadNotifications();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            logger.error("Ошибка при подсчете уведомлений: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }

    @Operation(summary = "Получить уведомления по автомобилю")
    @GetMapping("/car/{carId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByCarId(@PathVariable Long carId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByCarId(carId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Ошибка при получении уведомлений по автомобилю: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @Operation(summary = "Отметить уведомление как прочитанное")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при отметке уведомления: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Отметить все уведомления как прочитанные")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при отметке всех уведомлений: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Деактивировать уведомление")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateNotification(@PathVariable Long id) {
        try {
            notificationService.deactivateNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при деактивации уведомления: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Принудительная проверка уведомлений")
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkNotifications() {
        try {
            int createdCount = notificationService.checkAndCreateNotifications();
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "created", createdCount,
                    "message", "Проверка завершена, создано уведомлений: " + createdCount
            ));
        } catch (Exception e) {
            logger.error("Ошибка при проверке: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка при проверке: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить статистику уведомлений")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            Map<String, Object> stats = notificationService.getNotificationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "total", 0,
                    "unread", 0,
                    "warning", 0,
                    "overdue", 0
            ));
        }
    }
}