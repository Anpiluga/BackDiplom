package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Ответ с данными об операции техобслуживания")
public class MaintenanceOperationResponse {

    @Schema(description = "ID операции", example = "1")
    private Long id;

    @Schema(description = "Тип ТО", example = "ТО-1")
    private String maintenanceType;

    @Schema(description = "Дата последнего ТО", example = "2025-05-01")
    private LocalDate lastMaintenanceDate;

    @Schema(description = "Пробег на момент ТО (км)", example = "140000")
    private Integer lastMaintenanceMileage;

    @Schema(description = "Интервал ТО (км)", example = "10000")
    private Integer maintenanceIntervalKm;

    @Schema(description = "Интервал ТО (месяцы)", example = "6")
    private Integer maintenanceIntervalMonths;

    @Schema(description = "Дата следующего ТО", example = "2025-11-01")
    private LocalDate nextMaintenanceDate;

    @Schema(description = "Пробег следующего ТО (км)", example = "150000")
    private Integer nextMaintenanceMileage;

    @Schema(description = "Стоимость работы (руб.)", example = "5000")
    private Double laborCost;

    @Schema(description = "Стоимость запчастей (руб.)", example = "3000")
    private Double partsCost;

    @Schema(description = "Общая стоимость (руб.)", example = "8000")
    private Double totalCost;

    @Schema(description = "Дата создания", example = "2025-05-18")
    private LocalDate createdAt;
}