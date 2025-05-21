package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.BankOperationRequest;
import com.example.vkr2.DTO.BankOperationResponse;
import com.example.vkr2.services.BankOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cars/{carId}/bank-operations")
@RequiredArgsConstructor
@Tag(name = "Управление банковскими операциями")
public class BankOperationController {

    private final BankOperationService bankOperationService;

    @Operation(summary = "Добавить банковскую операцию для автомобиля")
    @PostMapping
    public ResponseEntity<BankOperationResponse> addBankOperation(
            @PathVariable Long carId,
            @RequestBody @Valid BankOperationRequest request) {
        BankOperationResponse response = bankOperationService.addBankOperation(carId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить историю банковских операций для автомобиля")
    @GetMapping
    public ResponseEntity<List<BankOperationResponse>> getBankOperations(@PathVariable Long carId) {
        List<BankOperationResponse> operations = bankOperationService.getBankOperationsByCarId(carId);
        return ResponseEntity.ok(operations);
    }
}