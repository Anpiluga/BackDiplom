package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "spare_part")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private Double pricePerUnit;

    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    @Column(nullable = false)
    private Double totalSum;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateAdded;

    public enum Category {
        CONSUMABLES("Расходники"),
        OILS("Масла"),
        TECHNICAL_FLUIDS("Пр. технические жидкости"),
        ENGINE_ELEMENTS("Элементы двигателя"),
        ENGINE_FUEL_SYSTEM("Система питания двигателя"),
        ENGINE_EXHAUST_SYSTEM("Система выпуска газов двигателя"),
        COOLING_SYSTEM("Система охлаждения"),
        BODY_ELEMENTS("Элементы кузова"),
        INSTRUMENTS_EQUIPMENT("Приборы и доп. оборудование"),
        ELECTRICAL_EQUIPMENT("Электрооборудование"),
        BRAKES("Тормоза"),
        STEERING("Рулевое управление"),
        WHEELS_HUBS("Колёса и ступицы"),
        SUSPENSION_ELEMENTS("Элементы подвески"),
        FRAME_ELEMENTS("Элементы рамы"),
        TRANSMISSION("Коробка передач"),
        CLUTCH_ELEMENTS("Элементы сцепления"),
        OTHER("Прочее");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Unit {
        PIECES("шт."),
        LITERS("л."),
        METERS("м."),
        RUNNING_METERS("пог. м."),
        UNITS("ед.");

        private final String displayName;

        Unit(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}