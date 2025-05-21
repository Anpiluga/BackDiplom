package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Column(nullable = false, unique = true, length = 9)
    private String licensePlate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer odometr;

    @Column(nullable = false)
    private Double fuelConsumption;

    @Column
    private LocalDate lastMaintenanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status;

    @OneToOne
    @JoinColumn(name = "driver_id", unique = true)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "counter_type")
    private CounterType counterType = CounterType.ODOMETER; // По умолчанию одометр

    @Column(name = "secondary_counter_enabled")
    private Boolean secondaryCounterEnabled = Boolean.FALSE; // Используем объектный тип Boolean

    @Column
    private Double fuelTankVolume;

    @Enumerated(EnumType.STRING)
    @Column
    private FuelEntry.FuelType fuelType;

    @Column(length = 1000)
    private String description;

    // Добавляем метод с префиксом is для совместимости с существующим кодом
    public boolean isSecondaryCounterEnabled() {
        return secondaryCounterEnabled != null ? secondaryCounterEnabled : Boolean.FALSE;
    }

    // Добавляем сеттер для правильной работы с null значениями
    public void setSecondaryCounterEnabled(Boolean secondaryCounterEnabled) {
        this.secondaryCounterEnabled = secondaryCounterEnabled != null ? secondaryCounterEnabled : Boolean.FALSE;
    }
}