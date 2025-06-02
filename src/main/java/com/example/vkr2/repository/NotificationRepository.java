package com.example.vkr2.repository;

import com.example.vkr2.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByActiveTrue();

    List<Notification> findByCarId(Long carId);

    @Query("SELECT n FROM Notification n WHERE n.car.id = :carId AND n.active = true")
    Optional<Notification> findActiveNotificationByCarId(@Param("carId") Long carId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true AND n.read = false")
    long countUnreadNotifications();
}