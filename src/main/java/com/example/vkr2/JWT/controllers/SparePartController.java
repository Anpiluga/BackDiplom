package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.SparePartRequest;
import com.example.vkr2.DTO.SparePartResponse;
import com.example.vkr2.entity.SparePart;
import com.example.vkr2.services.SparePartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Управление запчастями")
public class SparePartController {

    private static final Logger logger = LoggerFactory.getLogger(SparePartController.class);
    private final SparePartService sparePartService;

    @Operation(summary = "Добавить запчасть")
    @PostMapping
    public ResponseEntity<SparePartResponse> addSparePart(@RequestBody @Valid SparePartRequest request) {
        try {
            logger.info("Добавление запчасти: {}", request.getName());
            logger.debug("Request data: {}", request);

            SparePartResponse response = sparePartService.addSparePart(request);
            logger.info("Successfully added spare part with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error when adding spare part: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Internal error when adding spare part: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Обновить запчасть")
    @PutMapping("/{id}")
    public ResponseEntity<SparePartResponse> updateSparePart(@PathVariable Long id, @RequestBody @Valid SparePartRequest request) {
        try {
            logger.info("Обновление запчасти с ID {}: {}", id, request.getName());
            SparePartResponse response = sparePartService.updateSparePart(id, request);
            logger.info("Successfully updated spare part with ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Spare part not found for update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error when updating spare part: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Internal error when updating spare part: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все запчасти")
    @GetMapping
    public ResponseEntity<List<SparePartResponse>> getAllSpareParts() {
        try {
            logger.info("Получение всех запчастей");
            List<SparePartResponse> parts = sparePartService.getAllSpareParts();
            logger.info("Successfully retrieved {} spare parts", parts.size());
            return ResponseEntity.ok(parts);
        } catch (Exception e) {
            logger.error("Error retrieving spare parts: {}", e.getMessage(), e);
            // Возвращаем пустой список вместо ошибки, чтобы UI не ломался
            logger.warn("Returning empty list due to error (database may not be ready)");
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Получить запчасть по ID")
    @GetMapping("/{id}")
    public ResponseEntity<SparePartResponse> getSparePartById(@PathVariable Long id) {
        try {
            logger.info("Получение запчасти по ID: {}", id);
            SparePartResponse response = sparePartService.getSparePartById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Spare part not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Internal error when retrieving spare part: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удалить запчасть")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSparePart(@PathVariable Long id) {
        try {
            logger.info("Удаление запчасти с ID: {}", id);
            sparePartService.deleteSparePart(id);
            logger.info("Successfully deleted spare part with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Spare part not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Internal error when deleting spare part: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Получить запчасти по категории")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SparePartResponse>> getSparePartsByCategory(@PathVariable SparePart.Category category) {
        try {
            logger.info("Получение запчастей по категории: {}", category);
            List<SparePartResponse> parts = sparePartService.getSparePartsByCategory(category);
            logger.info("Found {} spare parts for category: {}", parts.size(), category);
            return ResponseEntity.ok(parts);
        } catch (Exception e) {
            logger.error("Error retrieving spare parts by category: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @Operation(summary = "Поиск запчастей по названию")
    @GetMapping("/search")
    public ResponseEntity<List<SparePartResponse>> searchSparePartsByName(@RequestParam String name) {
        try {
            logger.info("Поиск запчастей по названию: {}", name);
            List<SparePartResponse> parts = sparePartService.searchSparePartsByName(name);
            logger.info("Found {} spare parts matching name: {}", parts.size(), name);
            return ResponseEntity.ok(parts);
        } catch (Exception e) {
            logger.error("Error searching spare parts: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}