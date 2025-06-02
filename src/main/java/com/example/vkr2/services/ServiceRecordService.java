package com.example.vkr2.services;

import com.example.vkr2.DTO.ServiceRecordRequest;
import com.example.vkr2.DTO.ServiceRecordResponse;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.ServiceRecord;
import com.example.vkr2.repository.CarRepository;
import com.example.vkr2.repository.ServiceRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRecordService.class);

    private final ServiceRecordRepository serviceRecordRepository;
    private final CarRepository carRepository;



    @Transactional
    public ServiceRecordResponse addServiceRecord(ServiceRecordRequest request) {
        logger.info("Adding service record for car ID: {}", request.getCarId());
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        ServiceRecord serviceRecord = ServiceRecord.builder()
                .car(car)
                .counterReading(request.getCounterReading())
                .startDate(request.getStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .details(request.getDetails())
                .totalCost(request.getTotalCost())
                .status(ServiceRecord.ServiceStatus.PLANNED) // Явно устанавливаем статус
                .build();

        // Дополнительная проверка на случай, если builder не сработал
        if (serviceRecord.getStatus() == null) {
            serviceRecord.setStatus(ServiceRecord.ServiceStatus.PLANNED);
        }

        ServiceRecord savedRecord = serviceRecordRepository.save(serviceRecord);
        logger.info("Service record added with ID: {} for car ID: {} with status: {}",
                savedRecord.getId(), request.getCarId(), savedRecord.getStatus());
        return mapToResponse(savedRecord);
    }

    @Transactional
    public ServiceRecordResponse updateServiceRecord(Long id, ServiceRecordRequest request) {
        logger.info("Updating service record with ID: {}", id);
        ServiceRecord existingRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + id + " не найдена"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        existingRecord.setCar(car);
        existingRecord.setCounterReading(request.getCounterReading());
        existingRecord.setStartDate(request.getStartDate());
        existingRecord.setPlannedEndDate(request.getPlannedEndDate());
        existingRecord.setDetails(request.getDetails());
        existingRecord.setTotalCost(request.getTotalCost());

        // Не изменяем статус при обновлении через обычную форму
        // Статус должен изменяться отдельными методами

        ServiceRecord updatedRecord = serviceRecordRepository.save(existingRecord);
        logger.info("Service record updated with ID: {}", updatedRecord.getId());
        return mapToResponse(updatedRecord);
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getAllServiceRecords() {
        logger.info("Fetching all service records");
        try {
            return serviceRecordRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching service records", e);
            throw new RuntimeException("Ошибка при получении сервисных записей", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getServiceRecordsWithFilters(String search, Long carId,
                                                                    LocalDate startDate, LocalDate endDate,
                                                                    Double minCost, Double maxCost) {
        logger.info("Fetching service records with filters");
        try {
            return serviceRecordRepository.findServiceRecordsWithFilters(search, carId, startDate,
                            endDate, minCost, maxCost).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching filtered service records", e);
            throw new RuntimeException("Ошибка при получении отфильтрованных сервисных записей", e);
        }
    }

    @Transactional(readOnly = true)
    public ServiceRecordResponse getServiceRecordById(Long id) {
        logger.info("Fetching service record with ID: {}", id);
        ServiceRecord record = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + id + " не найдена"));
        return mapToResponse(record);
    }

    @Transactional
    public void deleteServiceRecord(Long id) {
        logger.info("Deleting service record with ID: {}", id);
        if (!serviceRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Сервисная запись с ID " + id + " не найдена");
        }
        serviceRecordRepository.deleteById(id);
        logger.info("Service record deleted with ID: {}", id);
    }

    // Новые методы для управления статусом
    @Transactional
    public ServiceRecordResponse updateServiceRecordStatus(Long id, ServiceRecord.ServiceStatus status) {
        logger.info("Updating service record status with ID: {} to status: {}", id, status);
        ServiceRecord record = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + id + " не найдена"));

        record.setStatus(status);

        // Если статус "Выполнено", устанавливаем время завершения
        if (status == ServiceRecord.ServiceStatus.COMPLETED && record.getCompletedAt() == null) {
            record.setCompletedAt(java.time.LocalDateTime.now());
        }

        ServiceRecord updatedRecord = serviceRecordRepository.save(record);
        logger.info("Service record status updated with ID: {} to status: {}", id, status);
        return mapToResponse(updatedRecord);
    }

    private ServiceRecordResponse mapToResponse(ServiceRecord record) {
        ServiceRecordResponse response = new ServiceRecordResponse();
        response.setId(record.getId());
        response.setCarId(record.getCar().getId());

        // Добавляем проверку на null
        if (record.getCar() != null) {
            response.setCarDetails(record.getCar().getBrand() + " " + record.getCar().getModel() + " " + record.getCar().getLicensePlate());
        } else {
            response.setCarDetails("Неизвестный автомобиль");
        }

        response.setCounterReading(record.getCounterReading());
        response.setStartDate(record.getStartDate());
        response.setPlannedEndDate(record.getPlannedEndDate());
        response.setDetails(record.getDetails());
        response.setTotalCost(record.getTotalCost());
        response.setStatus(record.getStatus());
        response.setCompletedAt(record.getCompletedAt());
        return response;
    }
}