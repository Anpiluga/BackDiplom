package com.example.vkr2.repository;

import com.example.vkr2.entity.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {
    List<FuelEntry> findByCarId(Long carId);

    // Комплексный фильтр для заправок
    @Query("SELECT fe FROM FuelEntry fe WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(fe.gasStation) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(fe.car.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(fe.car.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(fe.car.licensePlate) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:gasStation IS NULL OR :gasStation = '' OR " +
            "LOWER(fe.gasStation) LIKE LOWER(CONCAT('%', :gasStation, '%'))) AND " +
            "(:fuelType IS NULL OR fe.fuelType = :fuelType) AND " +
            "(:minCost IS NULL OR fe.totalCost >= :minCost) AND " +
            "(:maxCost IS NULL OR fe.totalCost <= :maxCost) AND " +
            "(:startDate IS NULL OR fe.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR fe.dateTime <= :endDate)")
    List<FuelEntry> findFuelEntriesWithFilters(@Param("search") String search,
                                               @Param("gasStation") String gasStation,
                                               @Param("fuelType") FuelEntry.FuelType fuelType,
                                               @Param("minCost") Double minCost,
                                               @Param("maxCost") Double maxCost,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    List<FuelEntry> findByGasStationContainingIgnoreCase(String gasStation);
    List<FuelEntry> findByFuelType(FuelEntry.FuelType fuelType);
    List<FuelEntry> findByTotalCostBetween(Double minCost, Double maxCost);
    List<FuelEntry> findByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}