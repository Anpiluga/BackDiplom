package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

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

    @Schema(description = "Дата начала работ", example = "2025-05-20")
    @NotNull(message = "Дата начала работ не может быть пустой")
    private LocalDate startDate;

    @Schema(description = "Планируемая дата окончания работ", example = "2025-05-25")
    private LocalDate plannedEndDate;

    @Schema(description = "Детали сервисных работ", example = "Замена масла и фильтров")
    @Size(max = 2000, message = "Детали не могут превышать 2000 символов")
    private String details;

    @Schema(description = "Общая сумма работ (руб)", example = "5000.0")
    @PositiveOrZero(message = "Сумма должна быть положительной или равной нулю")
    private Double totalCost;
}