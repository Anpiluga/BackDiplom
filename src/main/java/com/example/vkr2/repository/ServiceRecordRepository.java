package com.example.vkr2.repository;

import com.example.vkr2.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByCarId(Long carId);

    // Комплексный фильтр для сервисных записей (обновлен для DateTime)
    @Query("SELECT sr FROM ServiceRecord sr WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(sr.details) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sr.car.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sr.car.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sr.car.licensePlate) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:carId IS NULL OR sr.car.id = :carId) AND " +
            "(:startDateTime IS NULL OR sr.startDateTime >= :startDateTime) AND " +
            "(:endDateTime IS NULL OR sr.plannedEndDateTime <= :endDateTime) AND " +
            "(:minCost IS NULL OR sr.totalCost >= :minCost) AND " +
            "(:maxCost IS NULL OR sr.totalCost <= :maxCost)")
    List<ServiceRecord> findServiceRecordsWithFilters(@Param("search") String search,
                                                      @Param("carId") Long carId,
                                                      @Param("startDateTime") LocalDateTime startDateTime,
                                                      @Param("endDateTime") LocalDateTime endDateTime,
                                                      @Param("minCost") Double minCost,
                                                      @Param("maxCost") Double maxCost);

    List<ServiceRecord> findByStartDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<ServiceRecord> findByPlannedEndDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<ServiceRecord> findByTotalCostBetween(Double minCost, Double maxCost);

    // ИСПРАВЛЕНИЕ: Добавляем LIMIT 1 для получения только одной записи
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED' " +
            "ORDER BY sr.completedAt DESC, sr.startDateTime DESC LIMIT 1")
    Optional<ServiceRecord> findLastCompletedByCarId(@Param("carId") Long carId);

    // Альтернативный метод с сортировкой по дате начала
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED' " +
            "ORDER BY sr.startDateTime DESC, sr.createdAt DESC LIMIT 1")
    Optional<ServiceRecord> findLastCompletedByCarIdByStartDate(@Param("carId") Long carId);

    // Найти все выполненные сервисные записи для автомобиля (отсортированные)
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED' " +
            "ORDER BY sr.completedAt DESC, sr.startDateTime DESC")
    List<ServiceRecord> findAllCompletedByCarId(@Param("carId") Long carId);

    // Метод для получения последней записи (любого статуса) по показанию счетчика
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId " +
            "ORDER BY sr.counterReading DESC, sr.startDateTime DESC LIMIT 1")
    Optional<ServiceRecord> findLastServiceRecordByCounterReading(@Param("carId") Long carId);

    // Метод для получения всех записей автомобиля, отсортированных по дате
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId " +
            "ORDER BY sr.startDateTime DESC, sr.createdAt DESC")
    List<ServiceRecord> findByCarIdOrderByDateTime(@Param("carId") Long carId);

    // Поиск записей с показаниями счетчика больше указанного
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.counterReading > :counterReading " +
            "ORDER BY sr.startDateTime ASC")
    List<ServiceRecord> findByCarIdAndCounterReadingGreaterThan(@Param("carId") Long carId,
                                                                @Param("counterReading") Long counterReading);

    // Поиск записей в диапазоне дат с учетом времени
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND " +
            "sr.startDateTime BETWEEN :startDateTime AND :endDateTime " +
            "ORDER BY sr.startDateTime ASC")
    List<ServiceRecord> findByCarIdAndDateTimeRange(@Param("carId") Long carId,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);

    // Получить максимальное показание счетчика для автомобиля
    @Query("SELECT MAX(sr.counterReading) FROM ServiceRecord sr WHERE sr.car.id = :carId")
    Optional<Long> findMaxCounterReadingByCarId(@Param("carId") Long carId);

    // Статистика по автомобилю
    @Query("SELECT COUNT(sr) FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED'")
    Long countCompletedServicesByCarId(@Param("carId") Long carId);

    @Query("SELECT SUM(sr.totalCost) FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED'")
    Optional<Double> sumTotalCostByCarId(@Param("carId") Long carId);
}