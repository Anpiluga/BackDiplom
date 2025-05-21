package com.example.vkr2.DTO;

import com.example.vkr2.entity.FuelEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с данными о заправке")
public class FuelEntryResponse {

    @Schema(description = "ID записи", example = "1")
    private Long id;

    @Schema(description = "Марка и модель автомобиля", example = "Honda Civic")
    private String carDetails;

    @Schema(description = "Текущее показание одометра (км)", example = "50000")
    private Long odometerReading;

    @Schema(description = "Название заправки", example = "Лукойл")
    private String gasStation;

    @Schema(description = "Тип топлива", example = "GASOLINE")
    private FuelEntry.FuelType fuelType;

    @Schema(description = "Объём топлива (л)", example = "45.5")
    private Double volume;

    @Schema(description = "Цена за единицу (руб)", example = "50.0")
    private Double pricePerUnit;

    @Schema(description = "Общая сумма (руб)", example = "2275.0")
    private Double totalCost;

    @Schema(description = "Дата и время заправки", example = "2025-05-20T23:15:00")
    private LocalDateTime dateTime;
}