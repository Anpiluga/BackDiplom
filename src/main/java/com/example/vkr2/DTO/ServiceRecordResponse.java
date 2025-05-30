package com.example.vkr2.DTO;

import com.example.vkr2.entity.ServiceRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с данными о сервисной записи")
public class ServiceRecordResponse {

    @Schema(description = "ID записи", example = "1")
    private Long id;

    @Schema(description = "ID автомобиля", example = "1")
    private Long carId;

    @Schema(description = "Марка и модель автомобиля", example = "Honda Civic А123БВ45")
    private String carDetails;

    @Schema(description = "Текущее показание счётчика", example = "50000")
    private Long counterReading;

    @Schema(description = "Дата начала работ", example = "2025-05-20")
    private LocalDate startDate;

    @Schema(description = "Планируемая дата окончания работ", example = "2025-05-25")
    private LocalDate plannedEndDate;

    @Schema(description = "Детали сервисных работ", example = "Замена масла и фильтров")
    private String details;

    @Schema(description = "Общая сумма работ (руб)", example = "5000.0")
    private Double totalCost;

    @Schema(description = "Статус сервисной записи", example = "PLANNED")
    private ServiceRecord.ServiceStatus status;

    @Schema(description = "Дата и время завершения работ", example = "2025-05-25T15:30:00")
    private LocalDateTime completedAt;
}