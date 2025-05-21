package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fuel_entries")
public class FuelEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private Long odometerReading;

    @Column(nullable = false)
    private String gasStation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Column(nullable = false)
    private Double volume;

    @Column(nullable = false)
    private Double pricePerUnit;

    @Column(nullable = false)
    private Double totalCost;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    public enum FuelType {
        GASOLINE, DIESEL, PROPANE, METHANE, ELECTRICITY
    }
}