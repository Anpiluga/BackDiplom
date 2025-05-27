package com.example.vkr2.services;

import com.example.vkr2.DTO.SparePartRequest;
import com.example.vkr2.DTO.SparePartResponse;
import com.example.vkr2.entity.SparePart;
import com.example.vkr2.repository.SparePartRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SparePartService {

    private static final Logger logger = LoggerFactory.getLogger(SparePartService.class);

    private final SparePartRepository sparePartRepository;

    @Transactional
    public SparePartResponse addSparePart(SparePartRequest request) {
        logger.info("Adding spare part: {}", request.getName());

        try {
            double totalSum = request.getPricePerUnit() * request.getQuantity();

            SparePart sparePart = SparePart.builder()
                    .name(request.getName())
                    .category(request.getCategory())
                    .manufacturer(request.getManufacturer())
                    .pricePerUnit(request.getPricePerUnit())
                    .quantity(request.getQuantity())
                    .unit(request.getUnit())
                    .totalSum(totalSum)
                    .description(request.getDescription())
                    .dateTime(request.getDateTime() != null ? request.getDateTime() : LocalDateTime.now())
                    .build();

            SparePart savedPart = sparePartRepository.save(sparePart);
            logger.info("Spare part added with ID: {} at {}", savedPart.getId(), savedPart.getDateTime());
            return mapToResponse(savedPart);
        } catch (Exception e) {
            logger.error("Error adding spare part: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при добавлении запчасти: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SparePartResponse updateSparePart(Long id, SparePartRequest request) {
        logger.info("Updating spare part with ID: {}", id);

        try {
            SparePart existingPart = sparePartRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Запчасть с ID " + id + " не найдена"));

            double totalSum = request.getPricePerUnit() * request.getQuantity();

            existingPart.setName(request.getName());
            existingPart.setCategory(request.getCategory());
            existingPart.setManufacturer(request.getManufacturer());
            existingPart.setPricePerUnit(request.getPricePerUnit());
            existingPart.setQuantity(request.getQuantity());
            existingPart.setUnit(request.getUnit());
            existingPart.setTotalSum(totalSum);
            existingPart.setDescription(request.getDescription());
            existingPart.setDateTime(request.getDateTime() != null ? request.getDateTime() : LocalDateTime.now());

            SparePart updatedPart = sparePartRepository.save(existingPart);
            logger.info("Spare part updated with ID: {}", updatedPart.getId());
            return mapToResponse(updatedPart);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating spare part: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при обновлении запчасти: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> getAllSpareParts() {
        logger.info("Fetching all spare parts");
        try {
            List<SparePart> spareParts = sparePartRepository.findAll();
            logger.info("Successfully fetched {} spare parts", spareParts.size());
            return spareParts.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching spare parts: {}", e.getMessage(), e);
            // Возвращаем пустой список вместо выброса исключения
            logger.warn("Returning empty list due to database error (table may not exist yet)");
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public SparePartResponse getSparePartById(Long id) {
        logger.info("Fetching spare part with ID: {}", id);
        try {
            SparePart part = sparePartRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Запчасть с ID " + id + " не найдена"));
            return mapToResponse(part);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching spare part by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Ошибка при получении запчасти: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteSparePart(Long id) {
        logger.info("Deleting spare part with ID: {}", id);
        try {
            if (!sparePartRepository.existsById(id)) {
                throw new EntityNotFoundException("Запчасть с ID " + id + " не найдена");
            }
            sparePartRepository.deleteById(id);
            logger.info("Spare part deleted with ID: {}", id);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting spare part: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при удалении запчасти: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> getSparePartsByCategory(SparePart.Category category) {
        logger.info("Fetching spare parts by category: {}", category);
        try {
            return sparePartRepository.findByCategory(category).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching spare parts by category: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> searchSparePartsByName(String name) {
        logger.info("Searching spare parts by name: {}", name);
        try {
            return sparePartRepository.findByNameContainingIgnoreCase(name).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error searching spare parts by name: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> getSparePartsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching spare parts by date range: {} to {}", startDate, endDate);
        try {
            return sparePartRepository.findByDateTimeBetween(startDate, endDate).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Error fetching spare parts by date range (table may not exist): {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private SparePartResponse mapToResponse(SparePart part) {
        SparePartResponse response = new SparePartResponse();
        response.setId(part.getId());
        response.setName(part.getName());
        response.setCategory(part.getCategory());
        response.setManufacturer(part.getManufacturer());
        response.setPricePerUnit(part.getPricePerUnit());
        response.setQuantity(part.getQuantity());
        response.setUnit(part.getUnit());
        response.setTotalSum(part.getTotalSum());
        response.setDescription(part.getDescription());
        response.setDateTime(part.getDateTime() != null ? part.getDateTime() : LocalDateTime.now());
        return response;
    }
}