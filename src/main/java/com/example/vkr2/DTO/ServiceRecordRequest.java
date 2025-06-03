package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Запрос для создания или обновления сервисной записи")
public class ServiceRecordRequest {

    @Schema(description = "ID автомобиля", example = "1")
    @NotNull(message = "ID автомобиля не может быть пустым")
    private Long carId;

    @Schema(description = "Текущее показание счётчика", example = "50000")
    @NotNull(message = "Показание счётчика не может быть пустым")
    @Min(value = 0, message = "Показание счётчика не может быть отрицательным")
    private Long counterReading;

    @Schema(description = "Дата и время начала работ", example = "2025-05-20T10:30:00")
    @NotNull(message = "Дата и время начала работ не могут быть пустыми")
    private LocalDateTime startDateTime;

    @Schema(description = "Планируемая дата и время окончания работ", example = "2025-05-25T18:00:00")
    private LocalDateTime plannedEndDateTime;

    @Schema(description = "Детали сервисных работ", example = "Замена масла и фильтров")
    @Size(max = 2000, message = "Детали не могут превышать 2000 символов")
    private String details;

    @Schema(description = "Общая сумма работ (руб)", example = "5000.0")
    @PositiveOrZero(message = "Сумма должна быть положительной или равной нулю")
    private Double totalCost;
}