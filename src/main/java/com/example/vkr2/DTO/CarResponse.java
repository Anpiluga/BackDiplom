package com.example.vkr2.DTO;

import com.example.vkr2.entity.CarStatus;
import com.example.vkr2.entity.CounterType;
import com.example.vkr2.entity.FuelEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с данными об автомобиле")
public class CarResponse {
    @Schema(description = "ID автомобиля", example = "1")
    private Long id;

    @Schema(description = "VIN автомобиля", example = "1HGCM82633A004352")
    private String vin;

    @Schema(description = "Госномер автомобиля", example = "А123БВ45")
    private String licensePlate;

    @Schema(description = "Марка автомобиля", example = "Honda")
    private String brand;

    @Schema(description = "Модель автомобиля", example = "Civic")
    private String model;

    @Schema(description = "Год выпуска", example = "2020")
    private Integer year;

    @Schema(description = "Пробег (км)", example = "50000")
    private Integer odometr;

    @Schema(description = "Расход топлива (л/100км)", example = "7.5")
    private Double fuelConsumption;

    @Schema(description = "Статус автомобиля", example = "IN_USE")
    private CarStatus status;

    @Schema(description = "ID водителя (если привязан)", example = "1")
    private Long driverId;

    @Schema(description = "ФИО водителя (если привязан)", example = "Иванов Иван Иванович")
    private String driverFullName;

    // Новые поля
    @Schema(description = "Тип счётчика", example = "ODOMETER")
    private CounterType counterType;

    @Schema(description = "Включён ли второй счётчик", example = "false")
    private Boolean secondaryCounterEnabled = Boolean.FALSE;

    @Schema(description = "Объём топливного бака (л)", example = "50.0")
    private Double fuelTankVolume;

    @Schema(description = "Тип топлива", example = "GASOLINE")
    private FuelEntry.FuelType fuelType;

    @Schema(description = "Описание автомобиля", example = "Седан в отличном состоянии")
    private String description;

    // Добавляем метод с префиксом is для совместимости с существующим кодом
    public boolean isSecondaryCounterEnabled() {
        return secondaryCounterEnabled != null ? secondaryCounterEnabled : Boolean.FALSE;
    }
}