package com.example.vkr2.repository;

import com.example.vkr2.entity.MaintenanceOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceOperationRepository extends JpaRepository<MaintenanceOperation, Long> {
    List<MaintenanceOperation> findByCarId(Long carId);
}