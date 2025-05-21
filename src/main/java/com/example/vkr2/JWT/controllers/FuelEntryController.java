package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.FuelEntryRequest;
import com.example.vkr2.DTO.FuelEntryResponse;
import com.example.vkr2.services.FuelEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/fuel-entries")
@RequiredArgsConstructor
@Tag(name = "Управление заправками")
public class FuelEntryController {

    private final FuelEntryService fuelEntryService;

    @Operation(summary = "Добавить запись о заправке")
    @PostMapping
    public ResponseEntity<FuelEntryResponse> addFuelEntry(@RequestBody @Valid FuelEntryRequest request) {
        try {
            FuelEntryResponse response = fuelEntryService.addFuelEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Обновить запись о заправке")
    @PutMapping("/{id}")
    public ResponseEntity<FuelEntryResponse> updateFuelEntry(@PathVariable Long id, @RequestBody @Valid FuelEntryRequest request) {
        try {
            FuelEntryResponse response = fuelEntryService.updateFuelEntry(id, request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все записи о заправках")
    @GetMapping
    public ResponseEntity<List<FuelEntryResponse>> getAllFuelEntries() {
        try {
            List<FuelEntryResponse> entries = fuelEntryService.getAllFuelEntries();
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить запись о заправке по ID")
    @GetMapping("/{id}")
    public ResponseEntity<FuelEntryResponse> getFuelEntryById(@PathVariable Long id) {
        try {
            FuelEntryResponse response = fuelEntryService.getFuelEntryById(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удалить запись о заправке")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFuelEntry(@PathVariable Long id) {
        try {
            fuelEntryService.deleteFuelEntry(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}