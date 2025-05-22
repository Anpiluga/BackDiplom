package com.example.vkr2.repository;

import com.example.vkr2.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    List<SparePart> findByCategory(SparePart.Category category);
    List<SparePart> findByNameContainingIgnoreCase(String name);
    List<SparePart> findByManufacturerContainingIgnoreCase(String manufacturer);
}