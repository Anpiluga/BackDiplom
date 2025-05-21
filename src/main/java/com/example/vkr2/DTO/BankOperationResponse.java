package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Ответ с данными об операции в банке")
public class BankOperationResponse {

    @Schema(description = "ID операции", example = "1")
    private Long id;

    @Schema(description = "Тип операции", example = "Замена масла")
    private String operationType;

    @Schema(description = "Стоимость операции (руб.)", example = "5000")
    private Double cost;

    @Schema(description = "Дата операции", example = "2025-05-01")
    private LocalDate operationDate;
}