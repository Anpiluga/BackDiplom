package com.example.vkr2.JWT.controllers;

import com.example.vkr2.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Аналитика и статистика")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    private final AnalyticsService analyticsService;

    @Operation(summary = "Получить общую статистику расходов")
    @GetMapping("/total-expenses")
    public ResponseEntity<Map<String, Object>> getTotalExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.info("Получение общей статистики расходов");
            Map<String, Object> analytics = analyticsService.getTotalExpenses(startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of());
        }
    }

    @Operation(summary = "Получить статистику расходов по автомобилю")
    @GetMapping("/car/{carId}/expenses")
    public ResponseEntity<Map<String, Object>> getCarExpenses(
            @PathVariable Long carId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.info("Получение статистики расходов для автомобиля ID: {}", carId);
            Map<String, Object> analytics = analyticsService.getCarExpenses(carId, startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики по автомобилю: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of());
        }
    }

    @Operation(summary = "Получить месячную статистику")
    @GetMapping("/monthly-expenses")
    public ResponseEntity<Map<String, Object>> getMonthlyExpenses(
            @RequestParam(required = false) Long carId,
            @RequestParam(defaultValue = "3") int monthsBack) {
        try {
            logger.info("Получение месячной статистики за {} месяцев", monthsBack);
            Map<String, Object> analytics = analyticsService.getMonthlyExpenses(carId, monthsBack);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            logger.error("Ошибка при получении месячной статистики: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of());
        }
    }

    @Operation(summary = "Получить стоимость километра для автомобиля")
    @GetMapping("/car/{carId}/cost-per-km")
    public ResponseEntity<Map<String, Object>> getCostPerKm(
            @PathVariable Long carId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            logger.info("Получение стоимости километра для автомобиля ID: {}", carId);
            Map<String, Object> costData = analyticsService.getCostPerKm(carId, startDate, endDate);
            return ResponseEntity.ok(costData);
        } catch (Exception e) {
            logger.error("Ошибка при расчете стоимости километра: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("error", "Не удалось рассчитать стоимость километра"));
        }
    }
}