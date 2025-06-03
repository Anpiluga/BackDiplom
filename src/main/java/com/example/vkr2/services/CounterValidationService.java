package com.example.vkr2.services;

import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.FuelEntry;
import com.example.vkr2.entity.ServiceRecord;
import com.example.vkr2.repository.FuelEntryRepository;
import com.example.vkr2.repository.ServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CounterValidationService {

    private static final Logger logger = LoggerFactory.getLogger(CounterValidationService.class);

    private final FuelEntryRepository fuelEntryRepository;
    private final ServiceRecordRepository serviceRecordRepository;

    /**
     * Получает минимально допустимое показание счетчика для автомобиля
     */
    @Transactional(readOnly = true)
    public Long getMinimumAllowedCounter(Long carId) {
        logger.debug("Getting minimum allowed counter for car ID: {}", carId);

        Long maxFromFuelEntries = getMaxCounterFromFuelEntries(carId);
        Long maxFromServiceRecords = getMaxCounterFromServiceRecords(carId);

        Long result = Math.max(
                maxFromFuelEntries != null ? maxFromFuelEntries : 0L,
                maxFromServiceRecords != null ? maxFromServiceRecords : 0L
        );

        logger.debug("Minimum allowed counter for car ID {}: {}", carId, result);
        return result;
    }

    /**
     * Валидирует показание счетчика для заправки
     */
    @Transactional(readOnly = true)
    public void validateFuelEntryCounter(Long carId, Long counterReading, LocalDateTime dateTime) {
        Long minAllowed = getMinimumAllowedCounter(carId);

        if (counterReading < minAllowed) {
            throw new IllegalArgumentException(
                    String.format("Показание счетчика (%d км) не может быть меньше последнего зафиксированного значения (%d км)",
                            counterReading, minAllowed)
            );
        }

        // Дополнительная проверка: нет ли записей с более поздней датой и меньшим пробегом
        validateCounterConsistency(carId, counterReading, dateTime, "fuel");
    }

    /**
     * Валидирует показание счетчика для сервисной записи
     */
    @Transactional(readOnly = true)
    public void validateServiceRecordCounter(Long carId, Long counterReading, LocalDateTime dateTime) {
        Long minAllowed = getMinimumAllowedCounter(carId);

        if (counterReading < minAllowed) {
            throw new IllegalArgumentException(
                    String.format("Показание счетчика (%d км) не может быть меньше последнего зафиксированного значения (%d км)",
                            counterReading, minAllowed)
            );
        }

        validateCounterConsistency(carId, counterReading, dateTime, "service");
    }

    /**
     * Получает информацию о последних записях счетчика для автомобиля
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCounterInfo(Long carId) {
        Long minAllowed = getMinimumAllowedCounter(carId);

        // Получаем последние записи
        List<CounterRecord> allRecords = getAllCounterRecords(carId);
        allRecords.sort(Comparator.comparing(CounterRecord::getDateTime).reversed());

        CounterRecord lastRecord = allRecords.isEmpty() ? null : allRecords.get(0);

        return Map.of(
                "minAllowedCounter", minAllowed,
                "lastRecord", lastRecord != null ? Map.of(
                        "counter", lastRecord.getCounter(),
                        "dateTime", lastRecord.getDateTime(),
                        "type", lastRecord.getType(),
                        "description", lastRecord.getDescription()
                ) : Map.of(),
                "totalRecords", allRecords.size()
        );
    }

    private Long getMaxCounterFromFuelEntries(Long carId) {
        try {
            List<FuelEntry> fuelEntries = fuelEntryRepository.findByCarId(carId);
            return fuelEntries.stream()
                    .map(FuelEntry::getOdometerReading)
                    .max(Long::compareTo)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting max counter from fuel entries for car {}: {}", carId, e.getMessage());
            return null;
        }
    }

    private Long getMaxCounterFromServiceRecords(Long carId) {
        try {
            List<ServiceRecord> serviceRecords = serviceRecordRepository.findByCarId(carId);
            return serviceRecords.stream()
                    .map(ServiceRecord::getCounterReading)
                    .max(Long::compareTo)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting max counter from service records for car {}: {}", carId, e.getMessage());
            return null;
        }
    }

    private void validateCounterConsistency(Long carId, Long counterReading, LocalDateTime dateTime, String type) {
        List<CounterRecord> allRecords = getAllCounterRecords(carId);

        for (CounterRecord record : allRecords) {
            // Если есть запись с более поздней датой, но меньшим пробегом - это ошибка
            if (record.getDateTime().isAfter(dateTime) && record.getCounter() < counterReading) {
                throw new IllegalArgumentException(
                        String.format("Обнаружена несогласованность: запись от %s имеет пробег %d км, " +
                                        "что меньше указанного вами значения %d км для более ранней даты %s",
                                record.getDateTime(), record.getCounter(), counterReading, dateTime)
                );
            }

            // Если есть запись с более ранней датой, но большим пробегом - это тоже ошибка
            if (record.getDateTime().isBefore(dateTime) && record.getCounter() > counterReading) {
                throw new IllegalArgumentException(
                        String.format("Показание счетчика (%d км) не может быть меньше записи от %s (%d км)",
                                counterReading, record.getDateTime(), record.getCounter())
                );
            }
        }
    }

    private List<CounterRecord> getAllCounterRecords(Long carId) {
        List<CounterRecord> records = new ArrayList<>();

        try {
            // Добавляем записи из заправок
            List<FuelEntry> fuelEntries = fuelEntryRepository.findByCarId(carId);
            for (FuelEntry entry : fuelEntries) {
                records.add(new CounterRecord(
                        entry.getOdometerReading(),
                        entry.getDateTime(),
                        "fuel",
                        "Заправка на " + entry.getGasStation()
                ));
            }
        } catch (Exception e) {
            logger.error("Error getting fuel entries for car {}: {}", carId, e.getMessage());
        }

        try {
            // Добавляем записи из сервисных работ
            List<ServiceRecord> serviceRecords = serviceRecordRepository.findByCarId(carId);
            for (ServiceRecord record : serviceRecords) {
                LocalDateTime dateTime = record.getStartDateTime() != null ?
                        record.getStartDateTime() : record.getCreatedAt();

                records.add(new CounterRecord(
                        record.getCounterReading(),
                        dateTime,
                        "service",
                        "Сервисная запись: " + (record.getDetails() != null ?
                                record.getDetails().substring(0, Math.min(50, record.getDetails().length())) + "..." :
                                "без описания")
                ));
            }
        } catch (Exception e) {
            logger.error("Error getting service records for car {}: {}", carId, e.getMessage());
        }

        return records;
    }

    // Внутренний класс для хранения информации о записи счетчика
    private static class CounterRecord {
        private final Long counter;
        private final LocalDateTime dateTime;
        private final String type;
        private final String description;

        public CounterRecord(Long counter, LocalDateTime dateTime, String type, String description) {
            this.counter = counter;
            this.dateTime = dateTime;
            this.type = type;
            this.description = description;
        }

        public Long getCounter() { return counter; }
        public LocalDateTime getDateTime() { return dateTime; }
        public String getType() { return type; }
        public String getDescription() { return description; }
    }
}