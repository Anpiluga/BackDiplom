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
}