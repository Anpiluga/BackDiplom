package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Запрос для создания операции в банке")
public class BankOperationRequest {

    @Schema(description = "Тип операции", example = "Замена масла")
    @NotBlank(message = "Тип операции не может быть пустым")
    private String operationType;

    @Schema(description = "Стоимость операции (руб.)", example = "5000")
    @NotNull(message = "Стоимость не может быть пустой")
    @Positive(message = "Стоимость должна быть положительной")
    private Double cost;

    @Schema(description = "Дата операции", example = "2025-05-01")
    @NotNull(message = "Дата операции не может быть пустой")
    private LocalDate operationDate;
}