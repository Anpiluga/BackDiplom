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
                .dateAdded(request.getDateAdded())
                .build();

        SparePart savedPart = sparePartRepository.save(sparePart);
        logger.info("Spare part added with ID: {}", savedPart.getId());
        return mapToResponse(savedPart);
    }

    @Transactional
    public SparePartResponse updateSparePart(Long id, SparePartRequest request) {
        logger.info("Updating spare part with ID: {}", id);
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
        existingPart.setDateAdded(request.getDateAdded());

        SparePart updatedPart = sparePartRepository.save(existingPart);
        logger.info("Spare part updated with ID: {}", updatedPart.getId());
        return mapToResponse(updatedPart);
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> getAllSpareParts() {
        logger.info("Fetching all spare parts");
        try {
            return sparePartRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching spare parts", e);
            throw new RuntimeException("Ошибка при получении запчастей", e);
        }
    }

    @Transactional(readOnly = true)
    public SparePartResponse getSparePartById(Long id) {
        logger.info("Fetching spare part with ID: {}", id);
        SparePart part = sparePartRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запчасть с ID " + id + " не найдена"));
        return mapToResponse(part);
    }

    @Transactional
    public void deleteSparePart(Long id) {
        logger.info("Deleting spare part with ID: {}", id);
        if (!sparePartRepository.existsById(id)) {
            throw new EntityNotFoundException("Запчасть с ID " + id + " не найдена");
        }
        sparePartRepository.deleteById(id);
        logger.info("Spare part deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> getSparePartsByCategory(SparePart.Category category) {
        logger.info("Fetching spare parts by category: {}", category);
        return sparePartRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> searchSparePartsByName(String name) {
        logger.info("Searching spare parts by name: {}", name);
        return sparePartRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SparePartResponse> getSparePartsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching spare parts by date range: {} to {}", startDate, endDate);
        return sparePartRepository.findByDateAddedBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        response.setDateAdded(part.getDateAdded());
        return response;
    }
}