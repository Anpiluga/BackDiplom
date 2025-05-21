package com.example.vkr2.repository;

import com.example.vkr2.entity.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {
    List<FuelEntry> findByCarId(Long carId);
}