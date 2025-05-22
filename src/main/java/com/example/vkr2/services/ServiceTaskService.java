package com.example.vkr2.services;

import com.example.vkr2.DTO.ServiceTaskRequest;
import com.example.vkr2.DTO.ServiceTaskResponse;
import com.example.vkr2.entity.ServiceRecord;
import com.example.vkr2.entity.ServiceTask;
import com.example.vkr2.repository.ServiceRecordRepository;
import com.example.vkr2.repository.ServiceTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTaskService.class);

    private final ServiceTaskRepository serviceTaskRepository;
    private final ServiceRecordRepository serviceRecordRepository;

    @Transactional
    public ServiceTaskResponse addServiceTask(ServiceTaskRequest request) {
        logger.info("Adding service task for service record ID: {}", request.getServiceRecordId());
        ServiceRecord serviceRecord = serviceRecordRepository.findById(request.getServiceRecordId())
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + request.getServiceRecordId() + " не найдена"));

        ServiceTask serviceTask = ServiceTask.builder()
                .serviceRecord(serviceRecord)
                .taskName(request.getTaskName())
                .taskDescription(request.getTaskDescription())
                .build();

        ServiceTask savedTask = serviceTaskRepository.save(serviceTask);
        logger.info("Service task added with ID: {} for service record ID: {}", savedTask.getId(), request.getServiceRecordId());
        return mapToResponse(savedTask);
    }

    @Transactional
    public ServiceTaskResponse updateServiceTask(Long id, ServiceTaskRequest request) {
        logger.info("Updating service task with ID: {}", id);
        ServiceTask existingTask = serviceTaskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная задача с ID " + id + " не найдена"));

        ServiceRecord serviceRecord = serviceRecordRepository.findById(request.getServiceRecordId())
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + request.getServiceRecordId() + " не найдена"));

        existingTask.setServiceRecord(serviceRecord);
        existingTask.setTaskName(request.getTaskName());
        existingTask.setTaskDescription(request.getTaskDescription());

        ServiceTask updatedTask = serviceTaskRepository.save(existingTask);
        logger.info("Service task updated with ID: {}", updatedTask.getId());
        return mapToResponse(updatedTask);
    }

    @Transactional(readOnly = true)
    public List<ServiceTaskResponse> getAllServiceTasks() {
        logger.info("Fetching all service tasks");
        try {
            return serviceTaskRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching service tasks", e);
            throw new RuntimeException("Ошибка при получении сервисных задач", e);
        }
    }

    @Transactional(readOnly = true)
    public ServiceTaskResponse getServiceTaskById(Long id) {
        logger.info("Fetching service task with ID: {}", id);
        ServiceTask task = serviceTaskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная задача с ID " + id + " не найдена"));
        return mapToResponse(task);
    }

    @Transactional
    public void deleteServiceTask(Long id) {
        logger.info("Deleting service task with ID: {}", id);
        if (!serviceTaskRepository.existsById(id)) {
            throw new EntityNotFoundException("Сервисная задача с ID " + id + " не найдена");
        }
        serviceTaskRepository.deleteById(id);
        logger.info("Service task deleted with ID: {}", id);
    }

    private ServiceTaskResponse mapToResponse(ServiceTask task) {
        ServiceTaskResponse response = new ServiceTaskResponse();
        response.setId(task.getId());
        response.setServiceRecordId(task.getServiceRecord().getId());
        response.setTaskName(task.getTaskName());
        response.setTaskDescription(task.getTaskDescription());

        // Добавляем проверку на null
        if (task.getServiceRecord() != null && task.getServiceRecord().getCar() != null) {
            response.setServiceRecordDetails("Сервисная запись по автомобилю " +
                    task.getServiceRecord().getCar().getBrand() + " " +
                    task.getServiceRecord().getCar().getModel() + " " +
                    task.getServiceRecord().getCar().getLicensePlate());
        } else {
            response.setServiceRecordDetails("Неизвестная сервисная запись");
        }

        return response;
    }
}