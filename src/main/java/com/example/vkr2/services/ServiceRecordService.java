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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRecordService.class);

    private final ServiceRecordRepository serviceRecordRepository;
    private final CarRepository carRepository;
    private final CounterValidationService counterValidationService;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Transactional
    public ServiceRecordResponse addServiceRecord(ServiceRecordRequest request) {
        logger.info("Adding service record for car ID: {}", request.getCarId());

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        // ВАЛИДАЦИЯ ПОКАЗАНИЙ СЧЕТЧИКА
        try {
            counterValidationService.validateServiceRecordCounter(
                    request.getCarId(),
                    request.getCounterReading(),
                    request.getStartDateTime()
            );
        } catch (IllegalArgumentException e) {
            logger.error("Counter validation failed for car {}: {}", request.getCarId(), e.getMessage());
            throw e;
        }

        ServiceRecord serviceRecord = ServiceRecord.builder()
                .car(car)
                .counterReading(request.getCounterReading())
                .startDateTime(request.getStartDateTime())
                .plannedEndDateTime(request.getPlannedEndDateTime())
                .details(request.getDetails())
                .totalCost(request.getTotalCost())
                .status(ServiceRecord.ServiceStatus.PLANNED)
                .createdAt(LocalDateTime.now())
                .build();

        if (serviceRecord.getStatus() == null) {
            serviceRecord.setStatus(ServiceRecord.ServiceStatus.PLANNED);
        }

        ServiceRecord savedRecord = serviceRecordRepository.save(serviceRecord);

        // ОБНОВЛЯЕМ ПРОБЕГ АВТОМОБИЛЯ, если новое значение больше текущего
        if (request.getCounterReading() > car.getOdometr()) {
            car.setOdometr(request.getCounterReading().intValue());
            carRepository.save(car);
            logger.info("Updated car odometer to {} km for car ID: {}",
                    request.getCounterReading(), request.getCarId());
        }

        logger.info("Service record added with ID: {} for car ID: {} with status: {}, counter: {}",
                savedRecord.getId(), request.getCarId(), savedRecord.getStatus(), savedRecord.getCounterReading());

        return mapToResponse(savedRecord);
    }

    @Transactional
    public ServiceRecordResponse updateServiceRecord(Long id, ServiceRecordRequest request) {
        logger.info("Updating service record with ID: {}", id);

        ServiceRecord existingRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + id + " не найдена"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        // ВАЛИДАЦИЯ ПОКАЗАНИЙ СЧЕТЧИКА (только если они изменились)
        if (!existingRecord.getCounterReading().equals(request.getCounterReading()) ||
                !existingRecord.getStartDateTime().equals(request.getStartDateTime())) {

            try {
                counterValidationService.validateServiceRecordCounter(
                        request.getCarId(),
                        request.getCounterReading(),
                        request.getStartDateTime()
                );
            } catch (IllegalArgumentException e) {
                logger.error("Counter validation failed during update for car {}: {}", request.getCarId(), e.getMessage());
                throw e;
            }
        }

        existingRecord.setCar(car);
        existingRecord.setCounterReading(request.getCounterReading());
        existingRecord.setStartDateTime(request.getStartDateTime());
        existingRecord.setPlannedEndDateTime(request.getPlannedEndDateTime());
        existingRecord.setDetails(request.getDetails());
        existingRecord.setTotalCost(request.getTotalCost());

        ServiceRecord updatedRecord = serviceRecordRepository.save(existingRecord);

        // ОБНОВЛЯЕМ ПРОБЕГ АВТОМОБИЛЯ, если новое значение больше текущего
        if (request.getCounterReading() > car.getOdometr()) {
            car.setOdometr(request.getCounterReading().intValue());
            carRepository.save(car);
            logger.info("Updated car odometer to {} km for car ID: {}",
                    request.getCounterReading(), request.getCarId());
        }

        logger.info("Service record updated with ID: {}, counter: {}", updatedRecord.getId(), updatedRecord.getCounterReading());
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
                                                                    LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                                    Double minCost, Double maxCost) {
        logger.info("Fetching service records with filters");
        try {
            return serviceRecordRepository.findServiceRecordsWithFilters(search, carId, startDateTime,
                            endDateTime, minCost, maxCost).stream()
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

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getServiceRecordsByCarId(Long carId) {
        logger.info("Fetching service records for car ID: {}", carId);
        try {
            return serviceRecordRepository.findByCarIdOrderByDateTime(carId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching service records for car ID: {}", carId, e);
            throw new RuntimeException("Ошибка при получении сервисных записей для автомобиля", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getCompletedServiceRecordsByCarId(Long carId) {
        logger.info("Fetching completed service records for car ID: {}", carId);
        try {
            return serviceRecordRepository.findAllCompletedByCarId(carId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching completed service records for car ID: {}", carId, e);
            throw new RuntimeException("Ошибка при получении выполненных сервисных записей", e);
        }
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

    // Основной метод для изменения статуса с интеграцией уведомлений
    @Transactional
    public ServiceRecordResponse updateServiceRecordStatus(Long id, ServiceRecord.ServiceStatus status) {
        logger.info("Updating service record status with ID: {} to status: {}", id, status);
        ServiceRecord record = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сервисная запись с ID " + id + " не найдена"));

        ServiceRecord.ServiceStatus oldStatus = record.getStatus();
        record.setStatus(status);

        // Если статус "Выполнено", устанавливаем время завершения
        if (status == ServiceRecord.ServiceStatus.COMPLETED && record.getCompletedAt() == null) {
            record.setCompletedAt(LocalDateTime.now());

            // Деактивируем уведомления для этого автомобиля, так как ТО выполнено
            try {
                notificationService.deactivateNotificationsForCar(record.getCar().getId());
                logger.info("Deactivated notifications for car ID: {} after service completion", record.getCar().getId());
            } catch (Exception e) {
                logger.error("Ошибка при деактивации уведомлений после завершения ТО: {}", e.getMessage());
            }
        }

        // Если статус изменился с "Выполнено" на другой, очищаем время завершения
        if (status != ServiceRecord.ServiceStatus.COMPLETED && oldStatus == ServiceRecord.ServiceStatus.COMPLETED) {
            record.setCompletedAt(null);
        }

        ServiceRecord updatedRecord = serviceRecordRepository.save(record);

        // Если статус изменился на "Выполнено", проверяем необходимость создания новых уведомлений
        if (status == ServiceRecord.ServiceStatus.COMPLETED && oldStatus != ServiceRecord.ServiceStatus.COMPLETED) {
            try {
                // Небольшая задержка для корректного пересчета
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 2 секунды задержки
                        notificationService.checkCarMaintenanceNotification(record.getCar());
                        logger.info("Checked maintenance notifications for car ID: {} after service completion", record.getCar().getId());
                    } catch (Exception e) {
                        logger.error("Ошибка при проверке уведомлений в фоновом потоке: {}", e.getMessage());
                    }
                }).start();
            } catch (Exception e) {
                logger.error("Ошибка при запуске фоновой проверки уведомлений: {}", e.getMessage());
            }
        }

        logger.info("Service record status updated with ID: {} to status: {}", id, status);
        return mapToResponse(updatedRecord);
    }

    // Специализированные методы для быстрого изменения статуса
    @Transactional
    public ServiceRecordResponse markAsInProgress(Long id) {
        logger.info("Marking service record ID: {} as in progress", id);
        return updateServiceRecordStatus(id, ServiceRecord.ServiceStatus.IN_PROGRESS);
    }

    @Transactional
    public ServiceRecordResponse markAsCompleted(Long id) {
        logger.info("Marking service record ID: {} as completed", id);
        return updateServiceRecordStatus(id, ServiceRecord.ServiceStatus.COMPLETED);
    }

    @Transactional
    public ServiceRecordResponse markAsCancelled(Long id) {
        logger.info("Marking service record ID: {} as cancelled", id);
        return updateServiceRecordStatus(id, ServiceRecord.ServiceStatus.CANCELLED);
    }

    @Transactional
    public ServiceRecordResponse markAsPlanned(Long id) {
        logger.info("Marking service record ID: {} as planned", id);
        return updateServiceRecordStatus(id, ServiceRecord.ServiceStatus.PLANNED);
    }

    // Методы для получения записей по статусу
    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getServiceRecordsByStatus(ServiceRecord.ServiceStatus status) {
        logger.info("Fetching service records with status: {}", status);
        try {
            return serviceRecordRepository.findAll().stream()
                    .filter(record -> record.getStatus() == status)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching service records by status: {}", status, e);
            throw new RuntimeException("Ошибка при получении сервисных записей по статусу", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getPlannedServiceRecords() {
        return getServiceRecordsByStatus(ServiceRecord.ServiceStatus.PLANNED);
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getInProgressServiceRecords() {
        return getServiceRecordsByStatus(ServiceRecord.ServiceStatus.IN_PROGRESS);
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getCompletedServiceRecords() {
        return getServiceRecordsByStatus(ServiceRecord.ServiceStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getCancelledServiceRecords() {
        return getServiceRecordsByStatus(ServiceRecord.ServiceStatus.CANCELLED);
    }

    // Методы для работы с датами
    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getServiceRecordsByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        logger.info("Fetching service records between {} and {}", startDateTime, endDateTime);
        try {
            return serviceRecordRepository.findByStartDateTimeBetween(startDateTime, endDateTime).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching service records by date time range", e);
            throw new RuntimeException("Ошибка при получении сервисных записей по диапазону дат", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceRecordResponse> getOverdueServiceRecords() {
        logger.info("Fetching overdue service records");
        LocalDateTime now = LocalDateTime.now();
        try {
            return serviceRecordRepository.findAll().stream()
                    .filter(record -> record.getPlannedEndDateTime() != null &&
                            record.getPlannedEndDateTime().isBefore(now) &&
                            record.getStatus() != ServiceRecord.ServiceStatus.COMPLETED &&
                            record.getStatus() != ServiceRecord.ServiceStatus.CANCELLED)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching overdue service records", e);
            throw new RuntimeException("Ошибка при получении просроченных сервисных записей", e);
        }
    }

    // Статистические методы
    @Transactional(readOnly = true)
    public long countServiceRecordsByCarId(Long carId) {
        return serviceRecordRepository.findByCarId(carId).size();
    }

    @Transactional(readOnly = true)
    public long countCompletedServiceRecordsByCarId(Long carId) {
        return serviceRecordRepository.countCompletedServicesByCarId(carId);
    }

    @Transactional(readOnly = true)
    public Double getTotalCostByCarId(Long carId) {
        return serviceRecordRepository.sumTotalCostByCarId(carId).orElse(0.0);
    }

    // Метод для получения информации о показаниях счетчика
    @Transactional(readOnly = true)
    public Object getCounterInfoForCar(Long carId) {
        return counterValidationService.getCounterInfo(carId);
    }

    // Метод для принудительной проверки уведомлений после изменений
    @Transactional
    public void triggerNotificationCheckForCar(Long carId) {
        logger.info("Triggering notification check for car ID: {}", carId);
        try {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));
            notificationService.checkCarMaintenanceNotification(car);
        } catch (Exception e) {
            logger.error("Ошибка при принудительной проверке уведомлений: {}", e.getMessage());
            throw new RuntimeException("Ошибка при проверке уведомлений", e);
        }
    }

    private ServiceRecordResponse mapToResponse(ServiceRecord record) {
        ServiceRecordResponse response = new ServiceRecordResponse();
        response.setId(record.getId());
        response.setCarId(record.getCar().getId());

        // Добавляем проверку на null
        if (record.getCar() != null) {
            response.setCarDetails(record.getCar().getBrand() + " " +
                    record.getCar().getModel() + " " +
                    record.getCar().getLicensePlate());
        } else {
            response.setCarDetails("Неизвестный автомобиль");
        }

        response.setCounterReading(record.getCounterReading());
        response.setStartDateTime(record.getStartDateTime());
        response.setPlannedEndDateTime(record.getPlannedEndDateTime());
        response.setDetails(record.getDetails());
        response.setTotalCost(record.getTotalCost());
        response.setStatus(record.getStatus() != null ? record.getStatus() : ServiceRecord.ServiceStatus.PLANNED);
        response.setCompletedAt(record.getCompletedAt());
        response.setCreatedAt(record.getCreatedAt());

        return response;
    }
}