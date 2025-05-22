package com.example.vkr2.repository;

import com.example.vkr2.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByPhoneNumber(String phoneNumber);

    // Фильтр по наличию автомобиля
    @Query("SELECT d FROM Driver d WHERE " +
            "(:hasCar IS NULL OR " +
            "(:hasCar = true AND d.car IS NOT NULL) OR " +
            "(:hasCar = false AND d.car IS NULL))")
    List<Driver> findDriversWithCarFilter(@Param("hasCar") Boolean hasCar);

    // Водители с автомобилями
    @Query("SELECT d FROM Driver d WHERE d.car IS NOT NULL")
    List<Driver> findDriversWithCars();

    // Водители без автомобилей
    @Query("SELECT d FROM Driver d WHERE d.car IS NULL")
    List<Driver> findDriversWithoutCars();
}