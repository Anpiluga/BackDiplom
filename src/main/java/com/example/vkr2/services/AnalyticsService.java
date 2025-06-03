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

        final LocalDateTime finalStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(12);
        final LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();

        Map<String, Object> result = new HashMap<>();

        // Расходы на топливо
        double fuelCosts = 0;
        try {
            List<com.example.vkr2.entity.FuelEntry> allFuelEntries = fuelEntryRepository.findAll();
            logger.info("Found {} fuel entries total", allFuelEntries.size());

            fuelCosts = allFuelEntries.stream()
                    .filter(entry -> entry.getDateTime() != null && entry.getTotalCost() != null)
                    .filter(entry -> entry.getDateTime().isAfter(finalStartDate) && entry.getDateTime().isBefore(finalEndDate))
                    .peek(entry -> logger.debug("Including fuel entry: {} - {} руб", entry.getDateTime(), entry.getTotalCost()))
                    .mapToDouble(entry -> entry.getTotalCost())
                    .sum();

            logger.info("Total fuel costs in period: {} руб", fuelCosts);
        } catch (Exception e) {
            logger.error("Error calculating fuel costs: {}", e.getMessage(), e);
            fuelCosts = 0;
        }

        // Дополнительные расходы
        double additionalCosts = 0;
        try {
            List<com.example.vkr2.entity.AdditionalExpense> additionalExpenses =
                    additionalExpenseRepository.findByDateTimeBetween(finalStartDate, finalEndDate);
            logger.info("Found {} additional expenses in period", additionalExpenses.size());

            additionalCosts = additionalExpenses.stream()
                    .filter(expense -> expense.getPrice() != null)
                    .peek(expense -> logger.debug("Including additional expense: {} - {} руб", expense.getDateTime(), expense.getPrice()))
                    .mapToDouble(expense -> expense.getPrice())
                    .sum();

            logger.info("Total additional costs in period: {} руб", additionalCosts);
        } catch (Exception e) {
            logger.error("Error calculating additional costs: {}", e.getMessage(), e);
            additionalCosts = 0;
        }

        // Расходы на сервис
        double serviceCosts = 0;
        try {
            List<com.example.vkr2.entity.ServiceRecord> allServiceRecords = serviceRecordRepository.findAll();
            logger.info("Found {} service records total", allServiceRecords.size());

            serviceCosts = allServiceRecords.stream()
                    .filter(record -> record.getStartDateTime() != null && record.getTotalCost() != null)
                    .filter(record -> {
                        LocalDateTime recordDateTime = record.getStartDateTime();
                        return recordDateTime.isAfter(finalStartDate) && recordDateTime.isBefore(finalEndDate);
                    })
                    .peek(record -> logger.debug("Including service record: {} - {} руб", record.getStartDateTime(), record.getTotalCost()))
                    .mapToDouble(record -> record.getTotalCost())
                    .sum();

            logger.info("Total service costs in period: {} руб", serviceCosts);
        } catch (Exception e) {
            logger.error("Error calculating service costs: {}", e.getMessage(), e);
            serviceCosts = 0;
        }

        // Расходы на запчасти - используем dateTime
        double sparePartsCosts = 0;
        try {
            List<com.example.vkr2.entity.SparePart> sparePartsInPeriod =
                    sparePartRepository.findByDateTimeBetween(finalStartDate, finalEndDate);
            logger.info("Found {} spare parts in period", sparePartsInPeriod.size());

            sparePartsCosts = sparePartsInPeriod.stream()
                    .filter(part -> part.getTotalSum() != null)
                    .peek(part -> logger.debug("Including spare part: {} - {} руб", part.getDateTime(), part.getTotalSum()))
                    .mapToDouble(part -> part.getTotalSum())
                    .sum();

            logger.info("Total spare parts costs in period: {} руб", sparePartsCosts);
        } catch (Exception e) {
            logger.warn("Error calculating spare parts costs: {}", e.getMessage());
            sparePartsCosts = 0;
        }

        double totalCosts = fuelCosts + additionalCosts + serviceCosts + sparePartsCosts;
        logger.info("Final calculation - Fuel: {}, Additional: {}, Service: {}, SpareParts: {}, Total: {}",
                fuelCosts, additionalCosts, serviceCosts, sparePartsCosts, totalCosts);

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

        final LocalDateTime finalStartDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(12);
        final LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();

        Map<String, Object> result = new HashMap<>();

        // Расходы на топливо для конкретного автомобиля
        double fuelCosts = 0;
        try {
            List<com.example.vkr2.entity.FuelEntry> carFuelEntries = fuelEntryRepository.findByCarId(carId);
            logger.info("Found {} fuel entries for car {}", carFuelEntries.size(), carId);

            fuelCosts = carFuelEntries.stream()
                    .filter(entry -> entry.getDateTime() != null && entry.getTotalCost() != null)
                    .filter(entry -> entry.getDateTime().isAfter(finalStartDate) && entry.getDateTime().isBefore(finalEndDate))
                    .mapToDouble(entry -> entry.getTotalCost())
                    .sum();

            logger.info("Total fuel costs for car {}: {} руб", carId, fuelCosts);
        } catch (Exception e) {
            logger.error("Error calculating fuel costs for car {}: {}", carId, e.getMessage());
            fuelCosts = 0;
        }

        // Дополнительные расходы для конкретного автомобиля
        double additionalCosts = 0;
        try {
            List<com.example.vkr2.entity.AdditionalExpense> carAdditionalExpenses =
                    additionalExpenseRepository.findByCarIdAndDateTimeBetween(carId, finalStartDate, finalEndDate);
            logger.info("Found {} additional expenses for car {}", carAdditionalExpenses.size(), carId);

            additionalCosts = carAdditionalExpenses.stream()
                    .filter(expense -> expense.getPrice() != null)
                    .mapToDouble(expense -> expense.getPrice())
                    .sum();

            logger.info("Total additional costs for car {}: {} руб", carId, additionalCosts);
        } catch (Exception e) {
            logger.error("Error calculating additional costs for car {}: {}", carId, e.getMessage());
            additionalCosts = 0;
        }

        // Расходы на сервис для конкретного автомобиля
        double serviceCosts = 0;
        try {
            List<com.example.vkr2.entity.ServiceRecord> carServiceRecords = serviceRecordRepository.findByCarId(carId);
            logger.info("Found {} service records for car {}", carServiceRecords.size(), carId);

            serviceCosts = carServiceRecords.stream()
                    .filter(record -> record.getStartDateTime() != null && record.getTotalCost() != null)
                    .filter(record -> {
                        LocalDateTime recordDateTime = record.getStartDateTime();
                        return recordDateTime.isAfter(finalStartDate) && recordDateTime.isBefore(finalEndDate);
                    })
                    .mapToDouble(record -> record.getTotalCost())
                    .sum();

            logger.info("Total service costs for car {}: {} руб", carId, serviceCosts);
        } catch (Exception e) {
            logger.error("Error calculating service costs for car {}: {}", carId, e.getMessage());
            serviceCosts = 0;
        }

        // НЕ УЧИТЫВАЕМ запчасти для конкретного автомобиля (как требовалось)
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

            try {
                if (carId != null) {
                    // Расходы для конкретного автомобиля (БЕЗ запчастей)
                    List<com.example.vkr2.entity.FuelEntry> monthlyFuelEntries = fuelEntryRepository.findByCarId(carId);
                    fuelCost = monthlyFuelEntries.stream()
                            .filter(entry -> entry.getDateTime() != null && entry.getTotalCost() != null)
                            .filter(entry -> entry.getDateTime().isAfter(startOfMonth) && entry.getDateTime().isBefore(endOfMonth))
                            .mapToDouble(entry -> entry.getTotalCost())
                            .sum();

                    List<com.example.vkr2.entity.AdditionalExpense> monthlyAdditionalExpenses =
                            additionalExpenseRepository.findByCarIdAndDateTimeBetween(carId, startOfMonth, endOfMonth);
                    additionalCost = monthlyAdditionalExpenses.stream()
                            .filter(expense -> expense.getPrice() != null)
                            .mapToDouble(expense -> expense.getPrice())
                            .sum();

                    List<com.example.vkr2.entity.ServiceRecord> monthlyServiceRecords = serviceRecordRepository.findByCarId(carId);
                    serviceCost = monthlyServiceRecords.stream()
                            .filter(record -> record.getStartDateTime() != null && record.getTotalCost() != null)
                            .filter(record -> {
                                LocalDateTime recordDateTime = record.getStartDateTime();
                                return recordDateTime.isAfter(startOfMonth) && recordDateTime.isBefore(endOfMonth);
                            })
                            .mapToDouble(record -> record.getTotalCost())
                            .sum();

                    sparePartsCost = 0; // Для конкретного автомобиля не учитываем запчасти
                } else {
                    // Общие расходы по всем автомобилям
                    List<com.example.vkr2.entity.FuelEntry> monthlyFuelEntries = fuelEntryRepository.findAll();
                    fuelCost = monthlyFuelEntries.stream()
                            .filter(entry -> entry.getDateTime() != null && entry.getTotalCost() != null)
                            .filter(entry -> entry.getDateTime().isAfter(startOfMonth) && entry.getDateTime().isBefore(endOfMonth))
                            .mapToDouble(entry -> entry.getTotalCost())
                            .sum();

                    List<com.example.vkr2.entity.AdditionalExpense> monthlyAdditionalExpenses =
                            additionalExpenseRepository.findByDateTimeBetween(startOfMonth, endOfMonth);
                    additionalCost = monthlyAdditionalExpenses.stream()
                            .filter(expense -> expense.getPrice() != null)
                            .mapToDouble(expense -> expense.getPrice())
                            .sum();

                    List<com.example.vkr2.entity.ServiceRecord> monthlyServiceRecords = serviceRecordRepository.findAll();
                    serviceCost = monthlyServiceRecords.stream()
                            .filter(record -> record.getStartDateTime() != null && record.getTotalCost() != null)
                            .filter(record -> {
                                LocalDateTime recordDateTime = record.getStartDateTime();
                                return recordDateTime.isAfter(startOfMonth) && recordDateTime.isBefore(endOfMonth);
                            })
                            .mapToDouble(record -> record.getTotalCost())
                            .sum();

                    // Запчасти - используем dateTime
                    try {
                        List<com.example.vkr2.entity.SparePart> monthlySpareParts =
                                sparePartRepository.findByDateTimeBetween(startOfMonth, endOfMonth);
                        sparePartsCost = monthlySpareParts.stream()
                                .filter(part -> part.getTotalSum() != null)
                                .mapToDouble(part -> part.getTotalSum())
                                .sum();
                    } catch (Exception e) {
                        logger.warn("Error calculating spare parts for month {}: {}", month.format(formatter), e.getMessage());
                        sparePartsCost = 0;
                    }
                }

                logger.debug("Month {}: Fuel={}, Service={}, Additional={}, SpareParts={}",
                        month.format(formatter), fuelCost, serviceCost, additionalCost, sparePartsCost);

            } catch (Exception e) {
                logger.error("Error calculating monthly expenses for month {}: {}", month.format(formatter), e.getMessage());
                fuelCost = 0;
                serviceCost = 0;
                additionalCost = 0;
                sparePartsCost = 0;
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

        logger.info("Monthly expenses calculated for {} months", monthsBack);
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