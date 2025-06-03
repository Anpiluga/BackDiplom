package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_record")
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private Long counterReading;

    // Изменяем на LocalDateTime для единообразия
    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column
    private LocalDateTime plannedEndDateTime;

    @Column(length = 2000)
    private String details;

    @Column
    private Double totalCost;

    @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceTask> serviceTasks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.PLANNED;

    @Column
    private LocalDateTime completedAt;

    // Добавляем поле для отслеживания создания записи
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ServiceStatus.PLANNED;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void setDefaultStatus() {
        if (this.status == null) {
            this.status = ServiceStatus.PLANNED;
        }
    }

    public enum ServiceStatus {
        PLANNED("Запланировано"),
        IN_PROGRESS("В процессе"),
        COMPLETED("Выполнено"),
        CANCELLED("Отменено");

        private final String displayName;

        ServiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}