package com.example.vkr2.repository;

import com.example.vkr2.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByVin(String vin);
    Optional<Car> findByLicensePlate(String licensePlate);
}