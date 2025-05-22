package com.example.vkr2.services;

import com.example.vkr2.DTO.AdditionalExpenseRequest;
import com.example.vkr2.DTO.AdditionalExpenseResponse;
import com.example.vkr2.entity.AdditionalExpense;
import com.example.vkr2.entity.Car;
import com.example.vkr2.repository.AdditionalExpenseRepository;
import com.example.vkr2.repository.CarRepository;
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
public class AdditionalExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(AdditionalExpenseService.class);

    private final AdditionalExpenseRepository additionalExpenseRepository;
    private final CarRepository carRepository;

    @Transactional
    public AdditionalExpenseResponse addAdditionalExpense(AdditionalExpenseRequest request) {
        logger.info("Adding additional expense for car ID: {}", request.getCarId());
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        AdditionalExpense expense = AdditionalExpense.builder()
                .car(car)
                .type(request.getType())
                .price(request.getPrice())
                .dateTime(request.getDateTime())
                .description(request.getDescription())
                .build();

        AdditionalExpense savedExpense = additionalExpenseRepository.save(expense);
        logger.info("Additional expense added with ID: {} for car ID: {}", savedExpense.getId(), request.getCarId());
        return mapToResponse(savedExpense);
    }

    @Transactional
    public AdditionalExpenseResponse updateAdditionalExpense(Long id, AdditionalExpenseRequest request) {
        logger.info("Updating additional expense with ID: {}", id);
        AdditionalExpense existingExpense = additionalExpenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Дополнительный расход с ID " + id + " не найден"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + request.getCarId() + " не найден"));

        existingExpense.setCar(car);
        existingExpense.setType(request.getType());
        existingExpense.setPrice(request.getPrice());
        existingExpense.setDateTime(request.getDateTime());
        existingExpense.setDescription(request.getDescription());

        AdditionalExpense updatedExpense = additionalExpenseRepository.save(existingExpense);
        logger.info("Additional expense updated with ID: {}", updatedExpense.getId());
        return mapToResponse(updatedExpense);
    }

    @Transactional(readOnly = true)
    public List<AdditionalExpenseResponse> getAllAdditionalExpenses() {
        logger.info("Fetching all additional expenses");
        try {
            return additionalExpenseRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching additional expenses", e);
            throw new RuntimeException("Ошибка при получении дополнительных расходов", e);
        }
    }

    @Transactional(readOnly = true)
    public List<AdditionalExpenseResponse> getAdditionalExpensesWithFilters(String search, String type,
                                                                            Double minPrice, Double maxPrice,
                                                                            LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching additional expenses with filters");
        try {
            return additionalExpenseRepository.findAdditionalExpensesWithFilters(search, type, minPrice,
                            maxPrice, startDate, endDate).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching filtered additional expenses", e);
            throw new RuntimeException("Ошибка при получении отфильтрованных дополнительных расходов", e);
        }
    }

    @Transactional(readOnly = true)
    public AdditionalExpenseResponse getAdditionalExpenseById(Long id) {
        logger.info("Fetching additional expense with ID: {}", id);
        AdditionalExpense expense = additionalExpenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Дополнительный расход с ID " + id + " не найден"));
        return mapToResponse(expense);
    }

    @Transactional
    public void deleteAdditionalExpense(Long id) {
        logger.info("Deleting additional expense with ID: {}", id);
        if (!additionalExpenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Дополнительный расход с ID " + id + " не найден");
        }
        additionalExpenseRepository.deleteById(id);
        logger.info("Additional expense deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<AdditionalExpenseResponse> getExpensesByCarId(Long carId) {
        logger.info("Fetching additional expenses for car ID: {}", carId);
        return additionalExpenseRepository.findByCarId(carId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdditionalExpenseResponse> getExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching additional expenses between {} and {}", startDate, endDate);
        return additionalExpenseRepository.findByDateTimeBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdditionalExpenseResponse> getExpensesByCarAndDateRange(Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching additional expenses for car ID: {} between {} and {}", carId, startDate, endDate);
        return additionalExpenseRepository.findByCarIdAndDateTimeBetween(carId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AdditionalExpenseResponse mapToResponse(AdditionalExpense expense) {
        AdditionalExpenseResponse response = new AdditionalExpenseResponse();
        response.setId(expense.getId());

        if (expense.getCar() != null) {
            response.setCarDetails(expense.getCar().getBrand() + " " + expense.getCar().getModel());
        } else {
            response.setCarDetails("Неизвестный автомобиль");
        }

        response.setType(expense.getType());
        response.setPrice(expense.getPrice());
        response.setDateTime(expense.getDateTime());
        response.setDescription(expense.getDescription());
        return response;
    }
}