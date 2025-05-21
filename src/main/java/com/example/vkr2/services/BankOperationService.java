package com.example.vkr2.services;

import com.example.vkr2.DTO.BankOperationRequest;
import com.example.vkr2.DTO.BankOperationResponse;
import com.example.vkr2.entity.BankOperation;
import com.example.vkr2.entity.Car;
import com.example.vkr2.repository.BankOperationRepository;
import com.example.vkr2.repository.CarRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankOperationService {

    private static final Logger logger = LoggerFactory.getLogger(BankOperationService.class);

    private final BankOperationRepository bankOperationRepository;
    private final CarRepository carRepository;
    private final Random random = new Random();

    @Transactional
    public BankOperationResponse addBankOperation(Long carId, BankOperationRequest request) {
        logger.info("Adding bank operation for car ID: {}", carId);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

        BankOperation operation = BankOperation.builder()
                .car(car)
                .operationType(request.getOperationType())
                .cost(request.getCost())
                .operationDate(request.getOperationDate())
                .build();

        BankOperation savedOperation = bankOperationRepository.save(operation);
        logger.info("Bank operation added with ID: {} for car ID: {}", savedOperation.getId(), carId);
        return mapToResponse(savedOperation);
    }

    @Transactional(readOnly = true)
    public List<BankOperationResponse> getBankOperationsByCarId(Long carId) {
        logger.info("Fetching bank operations for car ID: {}", carId);
        List<BankOperation> operations = bankOperationRepository.findByCarId(carId);
        logger.info("Found {} bank operations for car ID: {}", operations.size(), carId);
        return operations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void generateRandomBankOperations(Long carId, int count) {
        logger.info("Generating {} random bank operations for car ID: {}", count, carId);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Автомобиль с ID " + carId + " не найден"));

        String[] operationTypes = {"Замена масла", "ТО-1", "Ремонт тормозов", "Замена шин", "Диагностика"};
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        long daysBetween = LocalDate.now().toEpochDay() - startDate.toEpochDay();

        for (int i = 0; i < count; i++) {
            String operationType = operationTypes[random.nextInt(operationTypes.length)];
            double cost = random.nextDouble() * 15000 + 1000; // От 1000 до 16000
            LocalDate operationDate = startDate.plusDays(random.nextInt((int) daysBetween));

            BankOperation operation = BankOperation.builder()
                    .car(car)
                    .operationType(operationType)
                    .cost(cost)
                    .operationDate(operationDate)
                    .build();
            bankOperationRepository.save(operation);
            logger.info("Generated operation: Type={}, Cost={}, Date={} for car ID: {}",
                    operationType, cost, operationDate, carId);
        }
        logger.info("Finished generating random bank operations for car ID: {}", carId);
    }

    private BankOperationResponse mapToResponse(BankOperation operation) {
        BankOperationResponse response = new BankOperationResponse();
        response.setId(operation.getId());
        response.setOperationType(operation.getOperationType());
        response.setCost(operation.getCost());
        response.setOperationDate(operation.getOperationDate());
        return response;
    }
}