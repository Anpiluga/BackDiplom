package com.example.vkr2.repository;

import com.example.vkr2.entity.BankOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankOperationRepository extends JpaRepository<BankOperation, Long> {
    List<BankOperation> findByCarId(Long carId);
}