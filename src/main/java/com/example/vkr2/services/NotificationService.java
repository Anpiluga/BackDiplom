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
import java.util.*;
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
    public int checkAndCreateNotifications() {
        logger.info("Начало проверки необходимости создания уведомлений о ТО");

        List<Car> allCars = carRepository.findAll();
        int createdCount = 0;

        for (Car car : allCars) {
            try {
                if (checkCarMaintenanceNotification(car)) {
                    createdCount++;
                }
            } catch (Exception e) {
                logger.error("Ошибка при проверке уведомлений для автомобиля ID {}: {}",
                        car.getId(), e.getMessage());
            }
        }

        logger.info("Проверка завершена. Создано уведомлений: {}", createdCount);
        return createdCount;
    }

    @Transactional
    public boolean checkCarMaintenanceNotification(Car car) {
        Optional<ReminderSettings> settingsOpt = reminderSettingsRepository.findByCarId(car.getId());

        if (settingsOpt.isEmpty() || !settingsOpt.get().getNotificationsEnabled()) {
            return false;
        }

        ReminderSettings settings = settingsOpt.get();

        // Получаем последнее выполненное ТО
        Optional<ServiceRecord> lastServiceOpt = serviceRecordRepository.findLastCompletedByCarId(car.getId());

        Long nextServiceOdometer;
        Integer kmToNextService;

        if (lastServiceOpt.isPresent()) {
            ServiceRecord lastService = lastServiceOpt.get();
            nextServiceOdometer = lastService.getCounterReading() + settings.getServiceIntervalKm();
            kmToNextService = (int)(nextServiceOdometer - car.getOdometr());
        } else {
            // Если нет выполненных ТО, считаем от текущего пробега
            kmToNextService = settings.getServiceIntervalKm() - car.getOdometr();
        }

        // Проверяем, нужно ли создать уведомление
        if (kmToNextService <= settings.getNotificationThresholdKm()) {
            Optional<Notification> existingNotification = notificationRepository
                    .findActiveNotificationByCarId(car.getId());

            if (existingNotification.isEmpty()) {
                createNotification(car, kmToNextService, settings);
                return true;
            } else {
                updateNotification(existingNotification.get(), kmToNextService);
                return false;
            }
        }

        return false;
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
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        logger.info("Создано уведомление для автомобиля ID {} (км до ТО: {})", car.getId(), kmToNextService);
    }

    @Transactional
    private void updateNotification(Notification notification, Integer kmToNextService) {
        notification.setKmToNextService(kmToNextService);
        notification.setType(kmToNextService < 0 ? Notification.NotificationType.OVERDUE : Notification.NotificationType.WARNING);
        notification.setMessage(generateMessage(notification.getCar(), kmToNextService));

        notificationRepository.save(notification);
        logger.debug("Обновлено уведомление для автомобиля ID {}", notification.getCar().getId());
    }

    private String generateMessage(Car car, Integer kmToNextService) {
        String carInfo = car.getBrand() + " " + car.getModel() + " " + car.getLicensePlate();

        if (kmToNextService < 0) {
            return String.format("ТО для %s просрочено на %d км!", carInfo, Math.abs(kmToNextService));
        } else if (kmToNextService == 0) {
            return String.format("Пора проводить ТО для %s!", carInfo);
        } else {
            return String.format("До следующего ТО для %s осталось %d км", carInfo, kmToNextService);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getActiveNotificationsWithFilters(String search, String type, String status, String sortBy) {
        List<Notification> notifications = notificationRepository.findByActiveTrue();

        return processNotifications(notifications, search, type, status, sortBy);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByCarId(Long carId) {
        List<Notification> notifications = notificationRepository.findByCarId(carId);
        return notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private List<NotificationDTO> processNotifications(List<Notification> notifications,
                                                       String search, String type, String status, String sortBy) {
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Применяем фильтры
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            dtos = dtos.stream()
                    .filter(n -> n.getCarDetails().toLowerCase().contains(searchLower) ||
                            n.getMessage().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        if (type != null && !type.equals("all")) {
            dtos = dtos.stream()
                    .filter(n -> n.getType().name().toLowerCase().equals(type.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.equals("all")) {
            boolean isRead = status.equals("read");
            dtos = dtos.stream()
                    .filter(n -> n.isRead() == isRead)
                    .collect(Collectors.toList());
        }

        // Применяем сортировку
        if (sortBy != null) {
            switch (sortBy) {
                case "date":
                    dtos.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                    break;
                case "km":
                    dtos.sort((a, b) -> {
                        if (a.getKmToNextService() == null && b.getKmToNextService() == null) return 0;
                        if (a.getKmToNextService() == null) return 1;
                        if (b.getKmToNextService() == null) return -1;
                        return a.getKmToNextService().compareTo(b.getKmToNextService());
                    });
                    break;
                case "car":
                    dtos.sort((a, b) -> a.getCarDetails().compareTo(b.getCarDetails()));
                    break;
                case "priority":
                    dtos.sort((a, b) -> {
                        // Сначала просроченные, потом предупреждения, потом информационные
                        int priorityA = a.getType() == Notification.NotificationType.OVERDUE ? 0 :
                                a.getType() == Notification.NotificationType.WARNING ? 1 : 2;
                        int priorityB = b.getType() == Notification.NotificationType.OVERDUE ? 0 :
                                b.getType() == Notification.NotificationType.WARNING ? 1 : 2;
                        return Integer.compare(priorityA, priorityB);
                    });
                    break;
                default:
                    dtos.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            }
        }

        return dtos;
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notificationRepository.save(notification);
            logger.info("Уведомление ID {} отмечено как прочитанное", notificationId);
        }
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unreadNotifications = notificationRepository.findByActiveTrueAndReadFalse();
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
        logger.info("Все активные уведомления отмечены как прочитанные");
    }

    @Transactional
    public void deactivateNotification(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setActive(false);
            notificationRepository.save(notification);
            logger.info("Уведомление ID {} деактивировано", notificationId);
        }
    }

    @Transactional
    public void deactivateNotificationsForCar(Long carId) {
        Optional<Notification> notificationOpt = notificationRepository.findActiveNotificationByCarId(carId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setActive(false);
            notificationRepository.save(notification);
            logger.info("Уведомления для автомобиля ID {} деактивированы", carId);
        }
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications() {
        return notificationRepository.countUnreadNotifications();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationStats() {
        List<Notification> activeNotifications = notificationRepository.findByActiveTrue();

        long total = activeNotifications.size();
        long unread = activeNotifications.stream()
                .mapToLong(n -> n.isRead() ? 0 : 1)
                .sum();
        long warning = activeNotifications.stream()
                .mapToLong(n -> n.getType() == Notification.NotificationType.WARNING ? 1 : 0)
                .sum();
        long overdue = activeNotifications.stream()
                .mapToLong(n -> n.getType() == Notification.NotificationType.OVERDUE ? 1 : 0)
                .sum();
        long info = activeNotifications.stream()
                .mapToLong(n -> n.getType() == Notification.NotificationType.INFO ? 1 : 0)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("unread", unread);
        stats.put("warning", warning);
        stats.put("overdue", overdue);
        stats.put("info", info);

        return stats;
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