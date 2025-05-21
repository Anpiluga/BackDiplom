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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelEntryService {

    private static final Logger logger = LoggerFactory.getLogger(FuelEntryService.class);

    private final FuelEntryRepository fuelEntryRepository;
    private final CarRepository carRepository;

    @Transactional
    public FuelEntryResponse addFuelEntry(FuelEntryRequest request) {
        logger.info("Adding fuel entry for car ID: {}", request.getCarId());
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

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
        logger.info("Fuel entry added with ID: {} for car ID: {}", savedEntry.getId(), request.getCarId());
        return mapToResponse(savedEntry);
    }

    @Transactional
    public FuelEntryResponse updateFuelEntry(Long id, FuelEntryRequest request) {
        logger.info("Updating fuel entry with ID: {}", id);
        FuelEntry existingEntry = fuelEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись о заправке с ID " + id + " не найдена"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        existingEntry.setCar(car);
        existingEntry.setOdometerReading(request.getOdometerReading());
        existingEntry.setGasStation(request.getGasStation());
        existingEntry.setFuelType(request.getFuelType());
        existingEntry.setVolume(request.getVolume());
        existingEntry.setPricePerUnit(request.getPricePerUnit());
        existingEntry.setTotalCost(request.getVolume() * request.getPricePerUnit());
        existingEntry.setDateTime(request.getDateTime());

        FuelEntry updatedEntry = fuelEntryRepository.save(existingEntry);
        logger.info("Fuel entry updated with ID: {}", updatedEntry.getId());
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

    private FuelEntryResponse mapToResponse(FuelEntry entry) {
        FuelEntryResponse response = new FuelEntryResponse();
        response.setId(entry.getId());
        response.setCarDetails(entry.getCar().getBrand() + " " + entry.getCar().getModel());
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