package com.example.vkr2.services;

import com.example.vkr2.DTO.BankOperationResponse;
import com.example.vkr2.DTO.CarResponse;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.CounterType;
import com.example.vkr2.entity.Driver;
import com.example.vkr2.repository.CarRepository;
import com.example.vkr2.repository.DriverRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {

    private static final Logger logger = LoggerFactory.getLogger(CarService.class);

    private final CarRepository carRepository;
    private final DriverRepository driverRepository;
    private final BankOperationService bankOperationService;

    private final Random random = new Random();

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

            Car savedCar = carRepository.save(car);
            logger.info("Car saved with ID: {}", savedCar.getId());

            try {
                int operationCount = random.nextInt(3) + 3;
                logger.info("Generating {} random bank operations for car ID: {}", operationCount, savedCar.getId());
                bankOperationService.generateRandomBankOperations(savedCar.getId(), operationCount);
                logger.info("Random bank operations generated for car ID: {}", savedCar.getId());
            } catch (Exception e) {
                logger.error("Failed to generate random bank operations for car ID: {}", savedCar.getId(), e);
            }

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
            for (Car car : cars) {
                try {
                    List<BankOperationResponse> operations = bankOperationService.getBankOperationsByCarId(car.getId());
                    if (operations.isEmpty()) {
                        logger.info("No bank operations found for car ID: {}. Generating random operations.", car.getId());
                        bankOperationService.generateRandomBankOperations(car.getId(), random.nextInt(3) + 3);
                        List<BankOperationResponse> newOperations = bankOperationService.getBankOperationsByCarId(car.getId());
                        logger.info("After generation, found {} operations for car ID: {}", newOperations.size(), car.getId());
                    } else {
                        logger.info("Found {} existing operations for car ID: {}", operations.size(), car.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error processing bank operations for car ID: {}", car.getId(), e);
                }
            }
            return cars.stream()
                    .map(this::mapToCarResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching all cars", e);
            throw new RuntimeException("Ошибка при получении списка автомобилей: " + e.getMessage(), e);
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
            existingCar.setSecondaryCounterEnabled(car.isSecondaryCounterEnabled());
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
            carRepository.delete(car);
            logger.info("Car deleted with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting car with ID: {}", id, e);
            throw new RuntimeException("Ошибка при удалении автомобиля", e);
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
        response.setCounterType(car.getCounterType() != null ? car.getCounterType() : CounterType.ODOMETER);
        response.setSecondaryCounterEnabled(car.isSecondaryCounterEnabled());
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