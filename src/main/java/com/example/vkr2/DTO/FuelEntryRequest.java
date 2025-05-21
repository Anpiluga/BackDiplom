package com.example.vkr2.DTO;

import com.example.vkr2.entity.FuelEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Запрос для создания или обновления записи о заправке")
public class FuelEntryRequest {

    @Schema(description = "ID автомобиля", example = "1")
    @NotNull(message = "ID автомобиля не может быть пустым")
    private Long carId;

    @Schema(description = "Текущее показание одометра (км)", example = "50000")
    @NotNull(message = "Одометр не может быть пустым")
    @Min(value = 0, message = "Одометр не может быть отрицательным")
    private Long odometerReading;

    @Schema(description = "Название заправки", example = "Лукойл")
    @NotBlank(message = "Название заправки не может быть пустым")
    private String gasStation;

    @Schema(description = "Тип топлива", example = "GASOLINE")
    @NotNull(message = "Тип топлива не может быть пустым")
    private FuelEntry.FuelType fuelType;

    @Schema(description = "Объём топлива (л)", example = "45.5")
    @NotNull(message = "Объём не может быть пустым")
    @Positive(message = "Объём должен быть положительным")
    private Double volume;

    @Schema(description = "Цена за единицу (руб)", example = "50.0")
    @NotNull(message = "Цена за единицу не может быть пустой")
    @Positive(message = "Цена должна быть положительной")
    private Double pricePerUnit;

    @Schema(description = "Дата и время заправки", example = "2025-05-20T23:15:00")
    @NotNull(message = "Дата и время не могут быть пустыми")
    private LocalDateTime dateTime;
}