package com.example.vkr2.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reminder_settings")
public class ReminderSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "car_id", nullable = false, unique = true)
    private Car car;

    @Column(name = "service_interval_km", nullable = false)
    private Integer serviceIntervalKm;

    @Column(name = "notification_threshold_km")
    private Integer notificationThresholdKm = 500; // За сколько км до ТО предупреждать

    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;
}