package com.example.vkr2.services;

import com.example.vkr2.DTO.NotificationDTO;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.Notification;
import com.example.vkr2.entity.ReminderSettings;
import com.example.vkr2.entity.ServiceRecord;
import com.example.vkr2.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final CarRepository carRepository;
    private final ReminderSettingsRepository reminderSettingsRepository;
    private final ServiceRecordRepository serviceRecordRepository;

    @Scheduled(fixedDelay = 1800000) // 30 минут
    @Transactional
    public void checkAndCreateNotifications() {
        logger.info("Начало проверки необходимости создания уведомлений о ТО");

        List<Car> allCars = carRepository.findAll();

        for (Car car : allCars) {
            try {
                checkCarMaintenanceNotification(car);
            } catch (Exception e) {
                logger.error("Ошибка при проверке уведомлений для автомобиля ID {}: {}",
                        car.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void checkCarMaintenanceNotification(Car car) {
        Optional<ReminderSettings> settingsOpt = reminderSettingsRepository.findByCarId(car.getId());

        if (settingsOpt.isEmpty() || !settingsOpt.get().getNotificationsEnabled()) {
            return;
        }

        ReminderSettings settings = settingsOpt.get();

        // Получаем последнее выполненное ТО
        Optional<ServiceRecord> lastServiceOpt = serviceRecordRepository.findLastCompletedByCarId(car.getId());

        if (lastServiceOpt.isEmpty()) {
            return;
        }

        ServiceRecord lastService = lastServiceOpt.get();
        Long nextServiceOdometer = lastService.getCounterReading() + settings.getServiceIntervalKm();
        Integer kmToNextService = (int)(nextServiceOdometer - car.getOdometr());

        // Проверяем, нужно ли создать уведомление
        if (kmToNextService <= settings.getNotificationThresholdKm()) {
            Optional<Notification> existingNotification = notificationRepository
                    .findActiveNotificationByCarId(car.getId());

            if (existingNotification.isEmpty()) {
                createNotification(car, kmToNextService, settings);
            } else {
                updateNotification(existingNotification.get(), kmToNextService);
            }
        }
    }

    @Transactional
    private void createNotification(Car car, Integer kmToNextService, ReminderSettings settings) {
        // Получаем количество выполненных ТО
        List<ServiceRecord> completedServices = serviceRecordRepository
                .findAllCompletedByCarId(car.getId());

        Notification notification = Notification.builder()
                .car(car)
                .kmToNextService(kmToNextService)
                .serviceCount(completedServices.size())
                .type(kmToNextService < 0 ? Notification.NotificationType.OVERDUE : Notification.NotificationType.WARNING)
                .message(generateMessage(car, kmToNextService))
                .read(false)
                .active(true)
                .build();

        notificationRepository.save(notification);
        logger.info("Создано уведомление для автомобиля ID {}", car.getId());
    }

    @Transactional
    private void updateNotification(Notification notification, Integer kmToNextService) {
        notification.setKmToNextService(kmToNextService);
        notification.setType(kmToNextService < 0 ? Notification.NotificationType.OVERDUE : Notification.NotificationType.WARNING);
        notification.setMessage(generateMessage(notification.getCar(), kmToNextService));

        notificationRepository.save(notification);
    }

    private String generateMessage(Car car, Integer kmToNextService) {
        String carInfo = car.getBrand() + " " + car.getModel() + " " + car.getLicensePlate();

        if (kmToNextService < 0) {
            return String.format("ТО для %s просрочено на %d км!", carInfo, Math.abs(kmToNextService));
        } else {
            return String.format("До следующего ТО для %s осталось %d км", carInfo, kmToNextService);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getActiveNotifications() {
        List<Notification> notifications = notificationRepository.findByActiveTrue();
        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void deactivateNotificationsForCar(Long carId) {
        Optional<Notification> notificationOpt = notificationRepository.findActiveNotificationByCarId(carId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setActive(false);
            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications() {
        return notificationRepository.countUnreadNotifications();
    }

    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setCarId(notification.getCar().getId());
        dto.setCarDetails(notification.getCar().getBrand() + " " +
                notification.getCar().getModel() + " " +
                notification.getCar().getLicensePlate());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setKmToNextService(notification.getKmToNextService());
        dto.setServiceCount(notification.getServiceCount());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRead(notification.isRead());
        dto.setActive(notification.isActive());

        return dto;
    }
}