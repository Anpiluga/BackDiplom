package com.example.vkr2.services;

import com.example.vkr2.DTO.*;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.ReminderSettings;
import com.example.vkr2.entity.ServiceRecord;
import com.example.vkr2.repository.CarRepository;
import com.example.vkr2.repository.ReminderSettingsRepository;
import com.example.vkr2.repository.ServiceRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderSettingsRepository reminderSettingsRepository;
    private final CarRepository carRepository;
    private final ServiceRecordRepository serviceRecordRepository;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Transactional
    public ReminderSettingsResponse createOrUpdateReminderSettings(ReminderSettingsRequest request) {
        logger.info("Creating/updating reminder settings for car ID: {}", request.getCarId());

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        ReminderSettings settings = reminderSettingsRepository.findByCarId(request.getCarId())
                .orElse(ReminderSettings.builder()
                        .car(car)
                        .build());

        settings.setServiceIntervalKm(request.getServiceIntervalKm());
        settings.setNotificationThresholdKm(request.getNotificationThresholdKm());
        settings.setNotificationsEnabled(request.getNotificationsEnabled());

        ReminderSettings savedSettings = reminderSettingsRepository.save(settings);
        logger.info("Reminder settings saved with ID: {}", savedSettings.getId());

        // Принудительно проверяем уведомления для этого автомобиля после изменения настроек
        try {
            notificationService.checkCarMaintenanceNotification(car);
        } catch (Exception e) {
            logger.error("Ошибка при проверке уведомлений после обновления настроек: {}", e.getMessage());
        }

        return mapToSettingsResponse(savedSettings);
    }

    @Transactional(readOnly = true)
    public ReminderSettingsResponse getReminderSettingsByCarId(Long carId) {
        logger.info("Fetching reminder settings for car ID: {}", carId);

        ReminderSettings settings = reminderSettingsRepository.findByCarId(carId)
                .orElseThrow(() -> new EntityNotFoundException("Настройки напоминаний для автомобиля с ID " + carId + " не найдены"));

        return mapToSettingsResponse(settings);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getAllReminders() {
        logger.info("Fetching all reminders");

        List<Car> cars = carRepository.findAll();
        List<ReminderResponse> reminders = new ArrayList<>();

        for (Car car : cars) {
            try {
                ReminderResponse reminder = createReminderForCar(car);
                reminders.add(reminder);
            } catch (Exception e) {
                logger.error("Error creating reminder for car ID {}: {}", car.getId(), e.getMessage());
                // Создаем базовое напоминание с ошибкой
                ReminderResponse errorReminder = new ReminderResponse();
                errorReminder.setCarId(car.getId());
                errorReminder.setCarDetails(car.getBrand() + " " + car.getModel() + " " + car.getLicensePlate());
                errorReminder.setCurrentOdometer(car.getOdometr());
                errorReminder.setStatus(ReminderResponse.ReminderStatus.WARNING);
                errorReminder.setMessage("Ошибка при расчете напоминания");
                reminders.add(errorReminder);
            }
        }

        return reminders;
    }

    @Transactional(readOnly = true)
    public ReminderResponse getReminderByCarId(Long carId) {
        logger.info("Fetching reminder for car ID: {}", carId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

        return createReminderForCar(car);
    }

    @Transactional(readOnly = true)
    public ReminderResponse getReminderWithNotificationStatus(Long carId) {
        logger.info("Fetching detailed reminder for car ID: {}", carId);

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

        ReminderResponse reminder = createReminderForCar(car);

        // Добавляем информацию о том, есть ли активные уведомления
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByCarId(carId);
            long activeNotifications = notifications.stream()
                    .filter(NotificationDTO::isActive)
                    .count();

            logger.debug("Car ID {} has {} active notifications", carId, activeNotifications);
        } catch (Exception e) {
            logger.error("Ошибка при получении уведомлений для автомобиля: {}", e.getMessage());
        }

        return reminder;
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersRequiringAttention() {
        logger.info("Fetching reminders requiring attention");

        List<ReminderResponse> allReminders = getAllReminders();

        return allReminders.stream()
                .filter(reminder -> reminder.getStatus() == ReminderResponse.ReminderStatus.WARNING ||
                        reminder.getStatus() == ReminderResponse.ReminderStatus.OVERDUE)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getOverdueReminders() {
        logger.info("Fetching overdue reminders");

        List<ReminderResponse> allReminders = getAllReminders();

        return allReminders.stream()
                .filter(reminder -> reminder.getStatus() == ReminderResponse.ReminderStatus.OVERDUE)
                .toList();
    }

    private ReminderResponse createReminderForCar(Car car) {
        ReminderResponse reminder = new ReminderResponse();
        reminder.setCarId(car.getId());
        reminder.setCarDetails(car.getBrand() + " " + car.getModel() + " " + car.getLicensePlate());
        reminder.setCurrentOdometer(car.getOdometr());

        // Получаем настройки напоминаний
        Optional<ReminderSettings> settingsOpt = reminderSettingsRepository.findByCarId(car.getId());
        if (settingsOpt.isPresent()) {
            ReminderSettings settings = settingsOpt.get();
            reminder.setServiceIntervalKm(settings.getServiceIntervalKm());

            // ИСПРАВЛЕНИЕ: Используем исправленный метод для получения последнего ТО
            try {
                Optional<ServiceRecord> lastServiceOpt = serviceRecordRepository.findLastCompletedByCarId(car.getId());

                if (lastServiceOpt.isPresent()) {
                    ServiceRecord lastService = lastServiceOpt.get();
                    reminder.setLastServiceOdometer(lastService.getCounterReading());
                    reminder.setLastServiceDate(lastService.getCompletedAt());

                    // Рассчитываем км до следующего ТО
                    Long nextServiceOdometer = lastService.getCounterReading() + settings.getServiceIntervalKm();
                    Integer kmToNextService = (int)(nextServiceOdometer - car.getOdometr());
                    reminder.setKmToNextService(kmToNextService);

                    // Определяем статус
                    if (kmToNextService < 0) {
                        reminder.setStatus(ReminderResponse.ReminderStatus.OVERDUE);
                        reminder.setMessage("ТО просрочено на " + Math.abs(kmToNextService) + " км!");
                    } else if (kmToNextService <= settings.getNotificationThresholdKm()) {
                        reminder.setStatus(ReminderResponse.ReminderStatus.WARNING);
                        reminder.setMessage("До следующего ТО осталось " + kmToNextService + " км");
                    } else {
                        reminder.setStatus(ReminderResponse.ReminderStatus.OK);
                        reminder.setMessage("До следующего ТО осталось " + kmToNextService + " км");
                    }
                } else {
                    // Если нет выполненных ТО, рассчитываем от текущего пробега
                    Integer kmToFirstService = settings.getServiceIntervalKm() - car.getOdometr();
                    reminder.setKmToNextService(kmToFirstService);

                    if (kmToFirstService <= settings.getNotificationThresholdKm()) {
                        reminder.setStatus(ReminderResponse.ReminderStatus.WARNING);
                        reminder.setMessage("Приближается время первого ТО. Осталось " + kmToFirstService + " км");
                    } else {
                        reminder.setStatus(ReminderResponse.ReminderStatus.OK);
                        reminder.setMessage("До первого ТО осталось " + kmToFirstService + " км");
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при получении последнего сервиса для автомобиля ID {}: {}", car.getId(), e.getMessage());

                // Устанавливаем базовые значения при ошибке
                reminder.setStatus(ReminderResponse.ReminderStatus.WARNING);
                reminder.setMessage("Ошибка при расчете напоминания о ТО");
                reminder.setKmToNextService(0);
            }
        } else {
            // Если нет настроек напоминаний
            reminder.setStatus(ReminderResponse.ReminderStatus.WARNING);
            reminder.setMessage("Не настроен интервал ТО");
        }

        return reminder;
    }

    @Transactional
    public void deleteReminderSettings(Long carId) {
        logger.info("Deleting reminder settings for car ID: {}", carId);

        ReminderSettings settings = reminderSettingsRepository.findByCarId(carId)
                .orElseThrow(() -> new EntityNotFoundException("Настройки напоминаний для автомобиля с ID " + carId + " не найдены"));

        // Деактивируем связанные уведомления
        try {
            notificationService.deactivateNotificationsForCar(carId);
        } catch (Exception e) {
            logger.error("Ошибка при деактивации уведомлений: {}", e.getMessage());
        }

        reminderSettingsRepository.delete(settings);
        logger.info("Reminder settings deleted for car ID: {}", carId);
    }

    @Transactional
    public void triggerMaintenanceCheckForCar(Long carId) {
        logger.info("Triggering maintenance check for car ID: {}", carId);

        try {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

            notificationService.checkCarMaintenanceNotification(car);
        } catch (Exception e) {
            logger.error("Ошибка при принудительной проверке ТО для автомобиля ID {}: {}", carId, e.getMessage());
            throw new RuntimeException("Ошибка при проверке ТО", e);
        }
    }

    @Transactional
    public void triggerMaintenanceCheckForAllCars() {
        logger.info("Triggering maintenance check for all cars");

        try {
            notificationService.checkAndCreateNotifications();
        } catch (Exception e) {
            logger.error("Ошибка при массовой проверке ТО: {}", e.getMessage());
            throw new RuntimeException("Ошибка при массовой проверке ТО", e);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasReminderSettings(Long carId) {
        return reminderSettingsRepository.existsByCarId(carId);
    }

    @Transactional(readOnly = true)
    public List<ReminderSettingsResponse> getAllReminderSettings() {
        logger.info("Fetching all reminder settings");

        return reminderSettingsRepository.findAll().stream()
                .map(this::mapToSettingsResponse)
                .toList();
    }

    private ReminderSettingsResponse mapToSettingsResponse(ReminderSettings settings) {
        ReminderSettingsResponse response = new ReminderSettingsResponse();
        response.setId(settings.getId());
        response.setCarId(settings.getCar().getId());
        response.setCarDetails(settings.getCar().getBrand() + " " + settings.getCar().getModel());
        response.setServiceIntervalKm(settings.getServiceIntervalKm());
        response.setNotificationThresholdKm(settings.getNotificationThresholdKm());
        response.setNotificationsEnabled(settings.getNotificationsEnabled());
        return response;
    }
}