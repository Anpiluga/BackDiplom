package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ со списком напоминаний")
public class ReminderResponse {

    @Schema(description = "ID автомобиля", example = "1")
    private Long carId;

    @Schema(description = "Марка и модель автомобиля", example = "Honda Civic А123БВ45")
    private String carDetails;

    @Schema(description = "Интервал ТО в км", example = "15000")
    private Integer serviceIntervalKm;

    @Schema(description = "Одометр последнего ТО", example = "45000")
    private Long lastServiceOdometer;

    @Schema(description = "Дата последнего ТО", example = "2025-05-20T10:30:00")
    private LocalDateTime lastServiceDate;

    @Schema(description = "Текущий одометр", example = "50000")
    private Integer currentOdometer;

    @Schema(description = "Км до следующего ТО", example = "10000")
    private Integer kmToNextService;

    @Schema(description = "Статус напоминания", example = "OK")
    private ReminderStatus status;

    @Schema(description = "Сообщение напоминания", example = "До следующего ТО осталось 10000 км")
    private String message;

    public enum ReminderStatus {
        OK, // Всё в порядке
        WARNING, // Приближается ТО
        OVERDUE // ТО просрочено
    }
}