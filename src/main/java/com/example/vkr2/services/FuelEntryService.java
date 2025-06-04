package com.example.vkr2.services;

import com.example.vkr2.DTO.FuelEntryRequest;
import com.example.vkr2.DTO.FuelEntryResponse;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.FuelEntry;
import com.example.vkr2.repository.CarRepository;
import com.example.vkr2.repository.FuelEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelEntryService {

    private static final Logger logger = LoggerFactory.getLogger(FuelEntryService.class);

    private final FuelEntryRepository fuelEntryRepository;
    private final CarRepository carRepository;
    private final CounterValidationService counterValidationService;

    @Transactional
    public FuelEntryResponse addFuelEntry(FuelEntryRequest request) {
        logger.info("Adding fuel entry for car ID: {}", request.getCarId());

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        // ВАЛИДАЦИЯ ПОКАЗАНИЙ СЧЕТЧИКА
        try {
            counterValidationService.validateFuelEntryCounter(
                    request.getCarId(),
                    request.getOdometerReading(),
                    request.getDateTime()
            );
        } catch (IllegalArgumentException e) {
            logger.error("Counter validation failed for fuel entry car {}: {}", request.getCarId(), e.getMessage());
            throw e;
        }

        FuelEntry fuelEntry = FuelEntry.builder()
                .car(car)
                .odometerReading(request.getOdometerReading())
                .gasStation(request.getGasStation())
                .fuelType(request.getFuelType())
                .volume(request.getVolume())
                .pricePerUnit(request.getPricePerUnit())
                .totalCost(request.getVolume() * request.getPricePerUnit())
                .dateTime(request.getDateTime())
                .build();

        FuelEntry savedEntry = fuelEntryRepository.save(fuelEntry);

        // ОБНОВЛЯЕМ ПРОБЕГ АВТОМОБИЛЯ, если новое значение больше текущего
        if (request.getOdometerReading() > car.getOdometr()) {
            car.setOdometr(request.getOdometerReading().intValue());
            carRepository.save(car);
            logger.info("Updated car odometer to {} km for car ID: {}",
                    request.getOdometerReading(), request.getCarId());
        }

        logger.info("Fuel entry added with ID: {} for car ID: {}, counter: {}",
                savedEntry.getId(), request.getCarId(), savedEntry.getOdometerReading());

        return mapToResponse(savedEntry);
    }

    @Transactional
    public FuelEntryResponse updateFuelEntry(Long id, FuelEntryRequest request) {
        logger.info("Updating fuel entry with ID: {}", id);

        FuelEntry existingEntry = fuelEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись о заправке с ID " + id + " не найдена"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        // ВАЛИДАЦИЯ ПОКАЗАНИЙ СЧЕТЧИКА (только если они изменились)
        if (!existingEntry.getOdometerReading().equals(request.getOdometerReading()) ||
                !existingEntry.getDateTime().equals(request.getDateTime())) {

            try {
                counterValidationService.validateFuelEntryCounter(
                        request.getCarId(),
                        request.getOdometerReading(),
                        request.getDateTime()
                );
            } catch (IllegalArgumentException e) {
                logger.error("Counter validation failed during update for fuel entry car {}: {}",
                        request.getCarId(), e.getMessage());
                throw e;
            }
        }

        existingEntry.setCar(car);
        existingEntry.setOdometerReading(request.getOdometerReading());
        existingEntry.setGasStation(request.getGasStation());
        existingEntry.setFuelType(request.getFuelType());
        existingEntry.setVolume(request.getVolume());
        existingEntry.setPricePerUnit(request.getPricePerUnit());
        existingEntry.setTotalCost(request.getVolume() * request.getPricePerUnit());
        existingEntry.setDateTime(request.getDateTime());

        FuelEntry updatedEntry = fuelEntryRepository.save(existingEntry);

        // ОБНОВЛЯЕМ ПРОБЕГ АВТОМОБИЛЯ, если новое значение больше текущего
        if (request.getOdometerReading() > car.getOdometr()) {
            car.setOdometr(request.getOdometerReading().intValue());
            carRepository.save(car);
            logger.info("Updated car odometer to {} km for car ID: {}",
                    request.getOdometerReading(), request.getCarId());
        }

        logger.info("Fuel entry updated with ID: {}, counter: {}",
                updatedEntry.getId(), updatedEntry.getOdometerReading());

        return mapToResponse(updatedEntry);
    }

    @Transactional(readOnly = true)
    public List<FuelEntryResponse> getAllFuelEntries() {
        logger.info("Fetching all fuel entries");
        try {
            return fuelEntryRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching fuel entries", e);
            throw new RuntimeException("Ошибка при получении записей о заправках", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FuelEntryResponse> getFuelEntriesWithFilters(String search, String gasStation,
                                                             FuelEntry.FuelType fuelType, Double minCost,
                                                             Double maxCost, LocalDateTime startDate,
                                                             LocalDateTime endDate) {
        logger.info("Fetching fuel entries with filters");
        try {
            return fuelEntryRepository.findFuelEntriesWithFilters(search, gasStation, fuelType,
                            minCost, maxCost, startDate, endDate).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching filtered fuel entries", e);
            throw new RuntimeException("Ошибка при получении отфильтрованных записей о заправках", e);
        }
    }

    @Transactional(readOnly = true)
    public FuelEntryResponse getFuelEntryById(Long id) {
        logger.info("Fetching fuel entry with ID: {}", id);
        FuelEntry entry = fuelEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись о заправке с ID " + id + " не найдена"));
        return mapToResponse(entry);
    }

    @Transactional
    public void deleteFuelEntry(Long id) {
        logger.info("Deleting fuel entry with ID: {}", id);
        if (!fuelEntryRepository.existsById(id)) {
            throw new EntityNotFoundException("Запись о заправке с ID " + id + " не найдена");
        }
        fuelEntryRepository.deleteById(id);
        logger.info("Fuel entry deleted with ID: {}", id);
    }

    // Методы для получения информации о показаниях счетчика
    @Transactional(readOnly = true)
    public Object getCounterInfoForCar(Long carId) {
        return counterValidationService.getCounterInfo(carId);
    }

    @Transactional(readOnly = true)
    public Long getMinimumAllowedCounter(Long carId) {
        return counterValidationService.getMinimumAllowedCounter(carId);
    }

    private FuelEntryResponse mapToResponse(FuelEntry entry) {
        FuelEntryResponse response = new FuelEntryResponse();
        response.setId(entry.getId());

        // Добавляем проверку на null
        if (entry.getCar() != null) {
            response.setCarDetails(entry.getCar().getBrand() + " " + entry.getCar().getModel());
        } else {
            response.setCarDetails("Неизвестный автомобиль");
        }

        response.setOdometerReading(entry.getOdometerReading());
        response.setGasStation(entry.getGasStation());
        response.setFuelType(entry.getFuelType());
        response.setVolume(entry.getVolume());
        response.setPricePerUnit(entry.getPricePerUnit());
        response.setTotalCost(entry.getTotalCost());
        response.setDateTime(entry.getDateTime());
        return response;
    }
}