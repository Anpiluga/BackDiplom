package com.example.vkr2.repository;

import com.example.vkr2.entity.ServiceTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTaskRepository extends JpaRepository<ServiceTask, Long> {
    List<ServiceTask> findByServiceRecordId(Long serviceRecordId);
}