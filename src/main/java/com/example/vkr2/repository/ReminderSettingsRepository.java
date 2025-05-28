package com.example.vkr2.repository;

import com.example.vkr2.entity.ReminderSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReminderSettingsRepository extends JpaRepository<ReminderSettings, Long> {
    Optional<ReminderSettings> findByCarId(Long carId);
    boolean existsByCarId(Long carId);
}