package com.example.vkr2.DTO;

import com.example.vkr2.entity.CarStatus;
import com.example.vkr2.entity.CounterType;
import com.example.vkr2.entity.FuelEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Запрос для создания или обновления автомобиля")
public class CarRequest {
    @Schema(description = "VIN автомобиля", example = "1HGCM82633A004352")
    @NotBlank(message = "VIN не может быть пустым")
    @Size(min = 17, max = 17, message = "VIN должен содержать ровно 17 символов")
    private String vin;

    @Schema(description = "Госномер автомобиля", example = "А123БВ45")
    @NotBlank(message = "Госномер не может быть пустым")
    @Size(max = 9, message = "Госномер должен содержать не более 9 символов")
    private String licensePlate;

    @Schema(description = "Марка автомобиля", example = "Honda")
    @NotBlank(message = "Марка не может быть пустой")
    private String brand;

    @Schema(description = "Модель автомобиля", example = "Civic")
    @NotBlank(message = "Модель не может быть пустой")
    private String model;

    @Schema(description = "Год выпуска", example = "2020")
    @NotNull(message = "Год не может быть пустым")
    @Min(value = 1900, message = "Год должен быть не ранее 1900")
    @Max(value = 2025, message = "Год не может быть позднее текущего года")
    private Integer year;

    @Schema(description = "Пробег (км)", example = "50000")
    @NotNull(message = "Одометр не может быть пустым")
    @Min(value = 0, message = "Одометр не может быть отрицательным")
    private Integer odometr;

    @Schema(description = "Расход топлива (л/100км)", example = "7.5")
    @NotNull(message = "Расход топлива не может быть пустым")
    @Positive(message = "Расход топлива должен быть положительным")
    private Double fuelConsumption;

    @Schema(description = "Статус автомобиля", example = "IN_USE")
    @NotNull(message = "Статус не может быть пустым")
    private CarStatus status;

    // Новые поля
    @Schema(description = "Тип счётчика", example = "ODOMETER")
    private CounterType counterType = CounterType.ODOMETER; // Установим значение по умолчанию

    @Schema(description = "Включён ли второй счётчик", example = "false")
    private Boolean secondaryCounterEnabled = Boolean.FALSE; // Используем объектный тип Boolean

    @Schema(description = "Объём топливного бака (л)", example = "50.0")
    private Double fuelTankVolume;

    @Schema(description = "Тип топлива", example = "GASOLINE")
    private FuelEntry.FuelType fuelType;

    @Schema(description = "Описание автомобиля", example = "Седан в отличном состоянии")
    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    private String description;

    // Добавляем метод с префиксом is для совместимости с существующим кодом
    public boolean isSecondaryCounterEnabled() {
        return secondaryCounterEnabled != null ? secondaryCounterEnabled : Boolean.FALSE;
    }
}