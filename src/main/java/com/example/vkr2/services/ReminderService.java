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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderSettingsRepository reminderSettingsRepository;
    private final CarRepository carRepository;
    private final ServiceRecordRepository serviceRecordRepository;

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

            // Получаем последнее выполненное ТО
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
                // Если нет выполненных ТО
                reminder.setStatus(ReminderResponse.ReminderStatus.WARNING);
                reminder.setMessage("Нет данных о предыдущих ТО");
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

        reminderSettingsRepository.delete(settings);
        logger.info("Reminder settings deleted for car ID: {}", carId);
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