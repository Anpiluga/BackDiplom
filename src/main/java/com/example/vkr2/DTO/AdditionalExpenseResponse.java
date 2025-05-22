// AdditionalExpenseResponse.java
package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с данными о дополнительном расходе")
public class AdditionalExpenseResponse {

    @Schema(description = "ID записи", example = "1")
    private Long id;

    @Schema(description = "Марка и модель автомобиля", example = "Honda Civic")
    private String carDetails;

    @Schema(description = "Тип расхода", example = "Штраф")
    private String type;

    @Schema(description = "Цена (руб)", example = "1500.0")
    private Double price;

    @Schema(description = "Дата и время", example = "2025-05-20T14:30:00")
    private LocalDateTime dateTime;

    @Schema(description = "Описание", example = "Штраф за превышение скорости")
    private String description;
}