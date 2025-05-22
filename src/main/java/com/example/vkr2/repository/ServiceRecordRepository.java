package com.example.vkr2.repository;

import com.example.vkr2.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByCarId(Long carId);
}