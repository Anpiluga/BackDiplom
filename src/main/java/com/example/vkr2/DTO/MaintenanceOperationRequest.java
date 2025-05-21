package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Запрос для создания или обновления операции техобслуживания")
public class MaintenanceOperationRequest {

    @Schema(description = "Тип ТО", example = "ТО-1")
    @NotBlank(message = "Тип ТО не может быть пустым")
    private String maintenanceType;

    @Schema(description = "Дата последнего ТО", example = "2025-05-01")
    private LocalDate lastMaintenanceDate;

    @Schema(description = "Пробег на момент ТО (км)", example = "140000")
    @Min(value = 0, message = "Пробег не может быть отрицательным")
    private Integer lastMaintenanceMileage;

    @Schema(description = "Интервал ТО (км)", example = "10000")
    @Min(value = 0, message = "Интервал не может быть отрицательным")
    private Integer maintenanceIntervalKm;

    @Schema(description = "Интервал ТО (месяцы)", example = "6")
    @Min(value = 0, message = "Интервал не может быть отрицательным")
    private Integer maintenanceIntervalMonths;

    @Schema(description = "Стоимость работы (руб.)", example = "5000")
    @Min(value = 0, message = "Стоимость работы не может быть отрицательной")
    private Double laborCost;

    @Schema(description = "Стоимость запчастей (руб.)", example = "3000")
    @Min(value = 0, message = "Стоимость запчастей не может быть отрицательной")
    private Double partsCost;
}