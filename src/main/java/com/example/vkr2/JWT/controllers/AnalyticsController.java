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
import java.util.HashMap;
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
            // Возвращаем пустые данные вместо ошибки
            Map<String, Object> emptyResult = createEmptyAnalyticsResult();
            return ResponseEntity.ok(emptyResult);
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
            // Возвращаем пустые данные для автомобиля
            Map<String, Object> emptyResult = createEmptyCarAnalyticsResult(carId);
            return ResponseEntity.ok(emptyResult);
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
            // Возвращаем пустые месячные данные
            Map<String, Object> emptyResult = createEmptyMonthlyResult(monthsBack);
            return ResponseEntity.ok(emptyResult);
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
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Не удалось рассчитать стоимость километра");
            errorResult.put("carId", carId);
            return ResponseEntity.ok(errorResult);
        }
    }

    // Вспомогательные методы для создания пустых результатов
    private Map<String, Object> createEmptyAnalyticsResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("fuelCosts", 0.0);
        result.put("additionalCosts", 0.0);
        result.put("serviceCosts", 0.0);
        result.put("sparePartsCosts", 0.0);
        result.put("totalCosts", 0.0);
        result.put("fuelPercentage", 0.0);
        result.put("additionalPercentage", 0.0);
        result.put("servicePercentage", 0.0);
        result.put("sparePartsPercentage", 0.0);
        return result;
    }

    private Map<String, Object> createEmptyCarAnalyticsResult(Long carId) {
        Map<String, Object> result = new HashMap<>();
        result.put("carId", carId);
        result.put("fuelCosts", 0.0);
        result.put("additionalCosts", 0.0);
        result.put("serviceCosts", 0.0);
        result.put("totalCosts", 0.0);
        result.put("fuelPercentage", 0.0);
        result.put("additionalPercentage", 0.0);
        result.put("servicePercentage", 0.0);
        return result;
    }

    private Map<String, Object> createEmptyMonthlyResult(int monthsBack) {
        Map<String, Object> result = new HashMap<>();
        result.put("months", java.util.Collections.nCopies(monthsBack, ""));
        result.put("fuelExpenses", java.util.Collections.nCopies(monthsBack, 0.0));
        result.put("serviceExpenses", java.util.Collections.nCopies(monthsBack, 0.0));
        result.put("additionalExpenses", java.util.Collections.nCopies(monthsBack, 0.0));
        result.put("sparePartsExpenses", java.util.Collections.nCopies(monthsBack, 0.0));
        result.put("totalExpenses", java.util.Collections.nCopies(monthsBack, 0.0));
        return result;
    }
}