package com.example.vkr2.repository;

import com.example.vkr2.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    List<SparePart> findByCategory(SparePart.Category category);
    List<SparePart> findByNameContainingIgnoreCase(String name);
    List<SparePart> findByManufacturerContainingIgnoreCase(String manufacturer);

    @Query("SELECT sp FROM SparePart sp WHERE sp.dateAdded BETWEEN :startDate AND :endDate")
    List<SparePart> findByDateAddedBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}