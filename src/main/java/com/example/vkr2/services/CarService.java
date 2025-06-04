package com.example.vkr2.services;

import com.example.vkr2.DTO.CarResponse;
import com.example.vkr2.entity.*;
import com.example.vkr2.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {

    private static final Logger logger = LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;
    private final DriverRepository driverRepository;
    private final ReminderSettingsRepository reminderSettingsRepository;
    private final FuelEntryRepository fuelEntryRepository;
    private final AdditionalExpenseRepository additionalExpenseRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final ServiceTaskRepository serviceTaskRepository;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Transactional
    public CarResponse addCar(Car car) {
        logger.info("Adding car with VIN: {}", car.getVin());
        try {
            if (carRepository.findByVin(car.getVin()).isPresent()) {
                logger.error("Car with VIN {} already exists", car.getVin());
                throw new IllegalArgumentException("Автомобиль с таким VIN уже существует");
            }
            if (carRepository.findByLicensePlate(car.getLicensePlate()).isPresent()) {
                logger.error("Car with license plate {} already exists", car.getLicensePlate());
                throw new IllegalArgumentException("Автомобиль с таким госномером уже существует");
            }

            // Безопасная обработка null значений
            if (car.getSecondaryCounterEnabled() == null) {
                car.setSecondaryCounterEnabled(Boolean.FALSE);
            }

            Car savedCar = carRepository.save(car);
            logger.info("Car saved with ID: {}", savedCar.getId());
            return mapToCarResponse(savedCar);
        } catch (Exception e) {
            logger.error("Error adding car with VIN: {}", car.getVin(), e);
            throw new RuntimeException("Ошибка при добавлении автомобиля", e);
        }
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars() {
        logger.info("Fetching all cars");
        try {
            List<Car> cars = carRepository.findAll();
            return cars.stream()
                    .map(this::mapToCarResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all cars", e);
            throw new RuntimeException("Ошибка при получении списка автомобилей: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<CarResponse> getCarsWithFilters(String search, CarStatus status, Integer yearFrom, Integer yearTo) {
        logger.info("Fetching cars with filters - search: {}, status: {}, yearFrom: {}, yearTo: {}",
                search, status, yearFrom, yearTo);
        try {
            List<Car> cars = carRepository.findCarsWithFilters(search, status, yearFrom, yearTo);
            return cars.stream()
                    .map(this::mapToCarResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching cars with filters", e);
            throw new RuntimeException("Ошибка при получении отфильтрованного списка автомобилей", e);
        }
    }

    @Transactional(readOnly = true)
    public CarResponse getCarById(Long id) {
        logger.info("Fetching car with ID: {}", id);
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + id + " не найден"));
            return mapToCarResponse(car);
        } catch (Exception e) {
            logger.error("Error fetching car with ID: {}", id, e);
            throw new RuntimeException("Ошибка при получении автомобиля", e);
        }
    }

    @Transactional
    public CarResponse updateCar(Long id, Car car) {
        logger.info("Updating car with ID: {}", id);
        try {
            Car existingCar = carRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + id + " не найден"));

            if (!existingCar.getVin().equals(car.getVin()) && carRepository.findByVin(car.getVin()).isPresent()) {
                logger.error("Car with VIN {} already exists", car.getVin());
                throw new IllegalArgumentException("Автомобиль с таким VIN уже существует");
            }
            if (!existingCar.getLicensePlate().equals(car.getLicensePlate()) &&
                    carRepository.findByLicensePlate(car.getLicensePlate()).isPresent()) {
                logger.error("Car with license plate {} already exists", car.getLicensePlate());
                throw new IllegalArgumentException("Автомобиль с таким госномером уже существует");
            }

            existingCar.setVin(car.getVin());
            existingCar.setLicensePlate(car.getLicensePlate());
            existingCar.setBrand(car.getBrand());
            existingCar.setModel(car.getModel());
            existingCar.setYear(car.getYear());
            existingCar.setOdometr(car.getOdometr());
            existingCar.setFuelConsumption(car.getFuelConsumption());
            existingCar.setStatus(car.getStatus());
            existingCar.setCounterType(car.getCounterType() != null ? car.getCounterType() : CounterType.ODOMETER);

            // Безопасная обработка null значений
            existingCar.setSecondaryCounterEnabled(car.getSecondaryCounterEnabled() != null
                    ? car.getSecondaryCounterEnabled()
                    : Boolean.FALSE);

            existingCar.setFuelTankVolume(car.getFuelTankVolume());
            existingCar.setFuelType(car.getFuelType());
            existingCar.setDescription(car.getDescription());

            Car updatedCar = carRepository.save(existingCar);
            logger.info("Car updated with ID: {}", updatedCar.getId());
            return mapToCarResponse(updatedCar);
        } catch (Exception e) {
            logger.error("Error updating car with ID: {}", id, e);
            throw new RuntimeException("Ошибка при обновлении автомобиля", e);
        }
    }

    @Transactional
    public void deleteCar(Long id) {
        logger.info("Deleting car with ID: {}", id);
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + id + " не найден"));

            // 1. Отвязываем водителя перед удалением автомобиля
            if (car.getDriver() != null) {
                Driver driver = car.getDriver();
                car.setDriver(null);
                driver.setCar(null);
                carRepository.save(car); // Сохраняем изменения
                logger.info("Driver unassigned from car ID: {} before deletion", id);
            }

            // 2. Удаляем настройки напоминаний
            try {
                Optional<ReminderSettings> reminderSettings = reminderSettingsRepository.findByCarId(id);
                if (reminderSettings.isPresent()) {
                    reminderSettingsRepository.delete(reminderSettings.get());
                    logger.info("Reminder settings deleted for car ID: {}", id);
                }
            } catch (Exception e) {
                logger.error("Error deleting reminder settings for car ID {}: {}", id, e.getMessage());
            }

            // 3. Деактивируем связанные уведомления
            try {
                if (notificationService != null) {
                    notificationService.deactivateNotificationsForCar(id);
                    logger.info("Notifications deactivated for car ID: {}", id);
                }
            } catch (Exception e) {
                logger.error("Error deactivating notifications for car ID {}: {}", id, e.getMessage());
            }

            // 4. Удаляем записи о заправках
            try {
                List<FuelEntry> fuelEntries = fuelEntryRepository.findByCarId(id);
                if (!fuelEntries.isEmpty()) {
                    fuelEntryRepository.deleteAll(fuelEntries);
                    logger.info("Deleted {} fuel entries for car ID: {}", fuelEntries.size(), id);
                }
            } catch (Exception e) {
                logger.error("Error deleting fuel entries for car ID {}: {}", id, e.getMessage());
            }

            // 5. Удаляем дополнительные расходы
            try {
                List<AdditionalExpense> additionalExpenses = additionalExpenseRepository.findByCarId(id);
                if (!additionalExpenses.isEmpty()) {
                    additionalExpenseRepository.deleteAll(additionalExpenses);
                    logger.info("Deleted {} additional expenses for car ID: {}", additionalExpenses.size(), id);
                }
            } catch (Exception e) {
                logger.error("Error deleting additional expenses for car ID {}: {}", id, e.getMessage());
            }

            // 6. Удаляем сервисные задачи через сервисные записи
            try {
                List<ServiceRecord> serviceRecords = serviceRecordRepository.findByCarId(id);
                for (ServiceRecord serviceRecord : serviceRecords) {
                    if (serviceRecord.getServiceTasks() != null) {
                        serviceTaskRepository.deleteAll(serviceRecord.getServiceTasks());
                    }
                }
                if (!serviceRecords.isEmpty()) {
                    serviceRecordRepository.deleteAll(serviceRecords);
                    logger.info("Deleted {} service records for car ID: {}", serviceRecords.size(), id);
                }
            } catch (Exception e) {
                logger.error("Error deleting service records for car ID {}: {}", id, e.getMessage());
            }

            // 7. Наконец удаляем сам автомобиль
            carRepository.delete(car);
            logger.info("Car deleted with ID: {}", id);

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting car with ID: {}", id, e);
            throw new RuntimeException("Ошибка при удалении автомобиля: " + e.getMessage(), e);
        }
    }

    @Transactional
    public CarResponse assignDriver(Long carId, Long driverId) {
        logger.info("Assigning driver ID: {} to car ID: {}", driverId, carId);
        try {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));
            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new EntityNotFoundException("Водитель с ID " + driverId + " не найден"));

            if (car.getDriver() != null) {
                logger.error("Car ID: {} already has a driver", carId);
                throw new IllegalArgumentException("Автомобиль уже привязан к водителю");
            }
            if (driver.getCar() != null) {
                logger.error("Driver ID: {} already assigned to a car", driverId);
                throw new IllegalArgumentException("Водитель уже привязан к автомобилю");
            }

            car.setDriver(driver);
            driver.setCar(car);
            Car updatedCar = carRepository.save(car);
            logger.info("Driver assigned to car ID: {}", carId);
            return mapToCarResponse(updatedCar);
        } catch (Exception e) {
            logger.error("Error assigning driver ID: {} to car ID: {}", driverId, carId, e);
            throw new RuntimeException("Ошибка при привязке водителя", e);
        }
    }

    @Transactional
    public CarResponse unassignDriver(Long carId) {
        logger.info("Unassigning driver from car ID: {}", carId);
        try {
            Car car = carRepository.findById(carId)
                    .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

            if (car.getDriver() == null) {
                logger.error("Car ID: {} has no driver to unassign", carId);
                throw new IllegalArgumentException("Автомобиль не привязан к водителю");
            }

            Driver driver = car.getDriver();
            car.setDriver(null);
            driver.setCar(null);
            Car updatedCar = carRepository.save(car);
            logger.info("Driver unassigned from car ID: {}", carId);
            return mapToCarResponse(updatedCar);
        } catch (Exception e) {
            logger.error("Error unassigning driver from car ID: {}", carId, e);
            throw new RuntimeException("Ошибка при отвязке водителя", e);
        }
    }

    // Обновленная функция mapToCarResponse в CarService
    private CarResponse mapToCarResponse(Car car) {
        CarResponse response = new CarResponse();
        response.setId(car.getId());
        response.setVin(car.getVin());
        response.setLicensePlate(car.getLicensePlate());
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setYear(car.getYear());
        response.setOdometr(car.getOdometr());
        response.setFuelConsumption(car.getFuelConsumption());
        response.setStatus(car.getStatus());

        // Безопасная обработка counterType
        if (car.getCounterType() != null) {
            response.setCounterType(car.getCounterType());
        } else {
            response.setCounterType(CounterType.ODOMETER); // Значение по умолчанию
        }

        // Безопасная обработка secondaryCounterEnabled
        response.setSecondaryCounterEnabled(car.getSecondaryCounterEnabled() != null
                ? car.getSecondaryCounterEnabled()
                : Boolean.FALSE);

        response.setFuelTankVolume(car.getFuelTankVolume());
        response.setFuelType(car.getFuelType());
        response.setDescription(car.getDescription());

        if (car.getDriver() != null) {
            response.setDriverId(car.getDriver().getId());
            response.setDriverFullName(car.getDriver().getFullName());
        }

        return response;
    }
}