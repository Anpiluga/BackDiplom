package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "maintenance_operations")
public class MaintenanceOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private String maintenanceType;

    @Column
    private LocalDate lastMaintenanceDate;

    @Column
    private Integer lastMaintenanceMileage;

    @Column
    private Integer maintenanceIntervalKm;

    @Column
    private Integer maintenanceIntervalMonths;

    @Column
    private LocalDate nextMaintenanceDate;

    @Column
    private Integer nextMaintenanceMileage;

    @Column
    private Double laborCost;

    @Column
    private Double partsCost;

    @Column
    private Double totalCost; // Добавлено поле для общей стоимости

    @Column
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}