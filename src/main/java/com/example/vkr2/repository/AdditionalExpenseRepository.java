package com.example.vkr2.repository;

import com.example.vkr2.entity.AdditionalExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdditionalExpenseRepository extends JpaRepository<AdditionalExpense, Long> {
    List<AdditionalExpense> findByCarId(Long carId);

    @Query("SELECT ae FROM AdditionalExpense ae WHERE ae.dateTime BETWEEN :startDate AND :endDate")
    List<AdditionalExpense> findByDateTimeBetween(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ae FROM AdditionalExpense ae WHERE ae.car.id = :carId AND ae.dateTime BETWEEN :startDate AND :endDate")
    List<AdditionalExpense> findByCarIdAndDateTimeBetween(@Param("carId") Long carId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    // Комплексный фильтр для дополнительных расходов
    @Query("SELECT ae FROM AdditionalExpense ae WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(ae.type) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ae.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ae.car.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ae.car.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ae.car.licensePlate) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:type IS NULL OR :type = '' OR " +
            "LOWER(ae.type) LIKE LOWER(CONCAT('%', :type, '%'))) AND " +
            "(:minPrice IS NULL OR ae.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR ae.price <= :maxPrice) AND " +
            "(:startDate IS NULL OR ae.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR ae.dateTime <= :endDate)")
    List<AdditionalExpense> findAdditionalExpensesWithFilters(@Param("search") String search,
                                                              @Param("type") String type,
                                                              @Param("minPrice") Double minPrice,
                                                              @Param("maxPrice") Double maxPrice,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);

    List<AdditionalExpense> findByTypeContainingIgnoreCase(String type);
    List<AdditionalExpense> findByPriceBetween(Double minPrice, Double maxPrice);
}