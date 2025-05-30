package com.example.vkr2.repository;

import com.example.vkr2.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByCarId(Long carId);

    // Комплексный фильтр для сервисных записей
    @Query("SELECT sr FROM ServiceRecord sr WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(sr.details) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sr.car.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sr.car.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(sr.car.licensePlate) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:carId IS NULL OR sr.car.id = :carId) AND " +
            "(:startDate IS NULL OR sr.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR sr.plannedEndDate <= :endDate) AND " +
            "(:minCost IS NULL OR sr.totalCost >= :minCost) AND " +
            "(:maxCost IS NULL OR sr.totalCost <= :maxCost)")
    List<ServiceRecord> findServiceRecordsWithFilters(@Param("search") String search,
                                                      @Param("carId") Long carId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate,
                                                      @Param("minCost") Double minCost,
                                                      @Param("maxCost") Double maxCost);

    List<ServiceRecord> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<ServiceRecord> findByPlannedEndDateBetween(LocalDate startDate, LocalDate endDate);
    List<ServiceRecord> findByTotalCostBetween(Double minCost, Double maxCost);

    // Добавляем недостающий метод для системы напоминаний
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED' ORDER BY sr.completedAt DESC")
    Optional<ServiceRecord> findLastCompletedByCarId(@Param("carId") Long carId);

    // Также можно добавить альтернативный метод, если completedAt может быть null
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED' ORDER BY sr.startDate DESC")
    Optional<ServiceRecord> findLastCompletedByCarIdByStartDate(@Param("carId") Long carId);

    // Найти все выполненные сервисные записи для автомобиля
    @Query("SELECT sr FROM ServiceRecord sr WHERE sr.car.id = :carId AND sr.status = 'COMPLETED' ORDER BY sr.completedAt DESC")
    List<ServiceRecord> findAllCompletedByCarId(@Param("carId") Long carId);
}