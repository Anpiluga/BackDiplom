package com.example.vkr2.repository;

import com.example.vkr2.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Основные методы поиска
    List<Notification> findByActiveTrue();

    List<Notification> findByActiveTrueAndReadFalse();

    List<Notification> findByCarId(Long carId);

    List<Notification> findByCarIdAndActiveTrue(Long carId);

    @Query("SELECT n FROM Notification n WHERE n.car.id = :carId AND n.active = true")
    Optional<Notification> findActiveNotificationByCarId(@Param("carId") Long carId);

    // Подсчеты
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true AND n.read = false")
    long countUnreadNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true")
    long countActiveNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.active = true AND n.type = :type")
    long countByType(@Param("type") Notification.NotificationType type);

    // Поиск по типу уведомления
    List<Notification> findByActiveTrueAndType(Notification.NotificationType type);

    List<Notification> findByActiveTrueAndTypeIn(List<Notification.NotificationType> types);

    // Поиск по дате
    List<Notification> findByActiveTrueAndCreatedAtAfter(LocalDateTime dateTime);

    List<Notification> findByActiveTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Комплексные запросы с фильтрами
    @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(n.car.brand, ' ', n.car.model, ' ', n.car.licensePlate)) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:type IS NULL OR n.type = :type) AND " +
            "(:isRead IS NULL OR n.read = :isRead)")
    List<Notification> findNotificationsWithFilters(@Param("search") String search,
                                                    @Param("type") Notification.NotificationType type,
                                                    @Param("isRead") Boolean isRead);

    // Поиск просроченных уведомлений
    @Query("SELECT n FROM Notification n WHERE n.active = true AND n.type = 'OVERDUE'")
    List<Notification> findOverdueNotifications();

    // Поиск предупреждений
    @Query("SELECT n FROM Notification n WHERE n.active = true AND n.type = 'WARNING'")
    List<Notification> findWarningNotifications();

    // Поиск уведомлений по диапазону км до ТО
    @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
            "n.kmToNextService IS NOT NULL AND " +
            "n.kmToNextService BETWEEN :minKm AND :maxKm")
    List<Notification> findByKmToNextServiceBetween(@Param("minKm") Integer minKm,
                                                    @Param("maxKm") Integer maxKm);

    // Поиск уведомлений для автомобилей с определенным количеством ТО
    @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
            "n.serviceCount IS NOT NULL AND " +
            "n.serviceCount >= :minServiceCount")
    List<Notification> findByServiceCountGreaterThanEqual(@Param("minServiceCount") Integer minServiceCount);

    // Удаление старых неактивных уведомлений
    @Query("DELETE FROM Notification n WHERE n.active = false AND n.createdAt < :dateTime")
    void deleteInactiveNotificationsOlderThan(@Param("dateTime") LocalDateTime dateTime);

    // Статистика по автомобилям
    @Query("SELECT n.car.id, COUNT(n) FROM Notification n WHERE n.active = true GROUP BY n.car.id")
    List<Object[]> getNotificationCountByCarId();

    // Последние уведомления
    @Query("SELECT n FROM Notification n WHERE n.active = true ORDER BY n.createdAt DESC")
    List<Notification> findLatestNotifications();

    // Критические уведомления (просроченные или очень близкие к ТО)
    @Query("SELECT n FROM Notification n WHERE n.active = true AND " +
            "(n.type = 'OVERDUE' OR (n.type = 'WARNING' AND n.kmToNextService <= 100))")
    List<Notification> findCriticalNotifications();
}