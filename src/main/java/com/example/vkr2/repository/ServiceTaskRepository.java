package com.example.vkr2.repository;

import com.example.vkr2.entity.ServiceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceTaskRepository extends JpaRepository<ServiceTask, Long> {
    List<ServiceTask> findByServiceRecordId(Long serviceRecordId);

    // Комплексный фильтр для сервисных задач
    @Query("SELECT st FROM ServiceTask st WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(st.taskName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(st.taskDescription) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:serviceRecordId IS NULL OR st.serviceRecord.id = :serviceRecordId)")
    List<ServiceTask> findServiceTasksWithFilters(@Param("search") String search,
                                                  @Param("serviceRecordId") Long serviceRecordId);

    @Query("SELECT st FROM ServiceTask st WHERE " +
            "LOWER(st.taskName) LIKE LOWER(CONCAT('%', :taskName, '%'))")
    List<ServiceTask> findByTaskNameContainingIgnoreCase(@Param("taskName") String taskName);
}