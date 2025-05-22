package com.example.vkr2.services;

import com.example.vkr2.entity.Car;
import com.example.vkr2.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final FuelEntryRepository fuelEntryRepository;
    private final AdditionalExpenseRepository additionalExpenseRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final SparePartRepository sparePartRepository;
    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getTotalExpenses(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Calculating total expenses from {} to {}", startDate, endDate);

        // Создаем final переменные для использования в lambda
        final LocalDateTime finalStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(3);
        final LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();

        Map<String, Object> result = new HashMap<>();

        // Расходы на топливо
        double fuelCosts = fuelEntryRepository.findAll().stream()
                .filter(entry -> entry.getDateTime().isAfter(finalStartDate) && entry.getDateTime().isBefore(finalEndDate))
                .mapToDouble(entry -> entry.getTotalCost())
                .sum();

        // Дополнительные расходы
        double additionalCosts = additionalExpenseRepository.findByDateTimeBetween(finalStartDate, finalEndDate).stream()
                .mapToDouble(expense -> expense.getPrice())
                .sum();

        // Расходы на сервис
        double serviceCosts = serviceRecordRepository.findAll().stream()
                .filter(record -> record.getStartDate().atStartOfDay().isAfter(finalStartDate) &&
                        record.getStartDate().atStartOfDay().isBefore(finalEndDate))
                .filter(record -> record.getTotalCost() != null)
                .mapToDouble(record -> record.getTotalCost())
                .sum();

        // Расходы на запчасти по датам добавления
        double sparePartsCosts = sparePartRepository.findByDateAddedBetween(finalStartDate, finalEndDate).stream()
                .mapToDouble(part -> part.getTotalSum())
                .sum();

        double totalCosts = fuelCosts + additionalCosts + serviceCosts + sparePartsCosts;

        result.put("fuelCosts", fuelCosts);
        result.put("additionalCosts", additionalCosts);
        result.put("serviceCosts", serviceCosts);
        result.put("sparePartsCosts", sparePartsCosts);
        result.put("totalCosts", totalCosts);

        // Процентное соотношение
        if (totalCosts > 0) {
            result.put("fuelPercentage", (fuelCosts / totalCosts) * 100);
            result.put("additionalPercentage", (additionalCosts / totalCosts) * 100);
            result.put("servicePercentage", (serviceCosts / totalCosts) * 100);
            result.put("sparePartsPercentage", (sparePartsCosts / totalCosts) * 100);
        } else {
            result.put("fuelPercentage", 0);
            result.put("additionalPercentage", 0);
            result.put("servicePercentage", 0);
            result.put("sparePartsPercentage", 0);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCarExpenses(Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Calculating expenses for car ID: {} from {} to {}", carId, startDate, endDate);

        // Создаем final переменные для использования в lambda
        final LocalDateTime finalStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(3);
        final LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();

        Map<String, Object> result = new HashMap<>();

        // Расходы на топливо для конкретного автомобиля
        double fuelCosts = fuelEntryRepository.findByCarId(carId).stream()
                .filter(entry -> entry.getDateTime().isAfter(finalStartDate) && entry.getDateTime().isBefore(finalEndDate))
                .mapToDouble(entry -> entry.getTotalCost())
                .sum();

        // Дополнительные расходы для конкретного автомобиля
        double additionalCosts = additionalExpenseRepository.findByCarIdAndDateTimeBetween(carId, finalStartDate, finalEndDate).stream()
                .mapToDouble(expense -> expense.getPrice())
                .sum();

        // Расходы на сервис для конкретного автомобиля
        double serviceCosts = serviceRecordRepository.findByCarId(carId).stream()
                .filter(record -> record.getStartDate().atStartOfDay().isAfter(finalStartDate) &&
                        record.getStartDate().atStartOfDay().isBefore(finalEndDate))
                .filter(record -> record.getTotalCost() != null)
                .mapToDouble(record -> record.getTotalCost())
                .sum();

        // НЕ УЧИТЫВАЕМ запчасти для расходов по автомобилю
        double totalCosts = fuelCosts + additionalCosts + serviceCosts;

        result.put("carId", carId);
        result.put("fuelCosts", fuelCosts);
        result.put("additionalCosts", additionalCosts);
        result.put("serviceCosts", serviceCosts);
        result.put("totalCosts", totalCosts);

        // Процентное соотношение
        if (totalCosts > 0) {
            result.put("fuelPercentage", (fuelCosts / totalCosts) * 100);
            result.put("additionalPercentage", (additionalCosts / totalCosts) * 100);
            result.put("servicePercentage", (serviceCosts / totalCosts) * 100);
        } else {
            result.put("fuelPercentage", 0);
            result.put("additionalPercentage", 0);
            result.put("servicePercentage", 0);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyExpenses(Long carId, int monthsBack) {
        logger.info("Calculating monthly expenses for car ID: {} for {} months back", carId, monthsBack);

        Map<String, Object> result = new HashMap<>();
        List<String> months = new ArrayList<>();
        List<Double> fuelExpenses = new ArrayList<>();
        List<Double> serviceExpenses = new ArrayList<>();
        List<Double> additionalExpenses = new ArrayList<>();
        List<Double> sparePartsExpenses = new ArrayList<>();
        List<Double> totalExpenses = new ArrayList<>();

        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("ru"));

        for (int i = monthsBack - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            final LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
            final LocalDateTime endOfMonth = month.atEndOfMonth().atTime(23, 59, 59);

            months.add(month.format(formatter));

            double fuelCost = 0;
            double serviceCost = 0;
            double additionalCost = 0;
            double sparePartsCost = 0;

            if (carId != null) {
                // Расходы для конкретного автомобиля (БЕЗ запчастей)
                fuelCost = fuelEntryRepository.findByCarId(carId).stream()
                        .filter(entry -> entry.getDateTime().isAfter(startOfMonth) && entry.getDateTime().isBefore(endOfMonth))
                        .mapToDouble(entry -> entry.getTotalCost())
                        .sum();

                additionalCost = additionalExpenseRepository.findByCarIdAndDateTimeBetween(carId, startOfMonth, endOfMonth).stream()
                        .mapToDouble(expense -> expense.getPrice())
                        .sum();

                serviceCost = serviceRecordRepository.findByCarId(carId).stream()
                        .filter(record -> record.getStartDate().atStartOfDay().isAfter(startOfMonth) &&
                                record.getStartDate().atStartOfDay().isBefore(endOfMonth))
                        .filter(record -> record.getTotalCost() != null)
                        .mapToDouble(record -> record.getTotalCost())
                        .sum();

                // НЕ учитываем запчасти для конкретного автомобиля
                sparePartsCost = 0;
            } else {
                // Общие расходы по всем автомобилям
                fuelCost = fuelEntryRepository.findAll().stream()
                        .filter(entry -> entry.getDateTime().isAfter(startOfMonth) && entry.getDateTime().isBefore(endOfMonth))
                        .mapToDouble(entry -> entry.getTotalCost())
                        .sum();

                additionalCost = additionalExpenseRepository.findByDateTimeBetween(startOfMonth, endOfMonth).stream()
                        .mapToDouble(expense -> expense.getPrice())
                        .sum();

                serviceCost = serviceRecordRepository.findAll().stream()
                        .filter(record -> record.getStartDate().atStartOfDay().isAfter(startOfMonth) &&
                                record.getStartDate().atStartOfDay().isBefore(endOfMonth))
                        .filter(record -> record.getTotalCost() != null)
                        .mapToDouble(record -> record.getTotalCost())
                        .sum();

                // Запчасти по датам добавления
                sparePartsCost = sparePartRepository.findByDateAddedBetween(startOfMonth, endOfMonth).stream()
                        .mapToDouble(part -> part.getTotalSum())
                        .sum();
            }

            fuelExpenses.add(fuelCost);
            serviceExpenses.add(serviceCost);
            additionalExpenses.add(additionalCost);
            sparePartsExpenses.add(sparePartsCost);
            totalExpenses.add(fuelCost + serviceCost + additionalCost + sparePartsCost);
        }

        result.put("months", months);
        result.put("fuelExpenses", fuelExpenses);
        result.put("serviceExpenses", serviceExpenses);
        result.put("additionalExpenses", additionalExpenses);
        result.put("sparePartsExpenses", sparePartsExpenses);
        result.put("totalExpenses", totalExpenses);

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCostPerKm(Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Calculating cost per km for car ID: {}", carId);

        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            return Map.of("error", "Автомобиль не найден");
        }

        Car car = carOpt.get();
        Map<String, Object> expenses = getCarExpenses(carId, startDate, endDate);
        double totalCosts = (Double) expenses.get("totalCosts");

        // Для расчета стоимости км используем пробег автомобиля
        // В реальности нужно было бы хранить историю пробега
        Integer currentOdometer = car.getOdometr();

        if (currentOdometer == null || currentOdometer == 0) {
            return Map.of("error", "Не удается рассчитать стоимость км - нет данных о пробеге");
        }

        // Примерный расчет: считаем что за период проехали 10% от общего пробега
        double estimatedKmDriven = currentOdometer * 0.1;
        double costPerKm = estimatedKmDriven > 0 ? totalCosts / estimatedKmDriven : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("carId", carId);
        result.put("carDetails", car.getBrand() + " " + car.getModel());
        result.put("totalCosts", totalCosts);
        result.put("estimatedKmDriven", estimatedKmDriven);
        result.put("costPerKm", costPerKm);
        result.put("currentOdometer", currentOdometer);

        return result;
    }
}