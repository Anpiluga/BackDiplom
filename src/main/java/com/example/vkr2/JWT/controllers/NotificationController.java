package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.NotificationDTO;
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

    @Operation(summary = "Получить все активные уведомления")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getActiveNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getActiveNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Ошибка при получении уведомлений: {}", e.getMessage());
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

    @Operation(summary = "Принудительная проверка уведомлений")
    @PostMapping("/check")
    public ResponseEntity<Map<String, String>> checkNotifications() {
        try {
            notificationService.checkAndCreateNotifications();
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (Exception e) {
            logger.error("Ошибка при проверке: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка при проверке"));
        }
    }
}