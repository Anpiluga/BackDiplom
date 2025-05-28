package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate plannedEndDate;

    @Column(length = 2000)
    private String details;

    @Column
    private Double totalCost;

    @OneToMany(mappedBy = "serviceRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceTask> serviceTasks;

    // Новые поля для системы напоминаний
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status = ServiceStatus.PLANNED;

    @Column
    private LocalDateTime completedAt;

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