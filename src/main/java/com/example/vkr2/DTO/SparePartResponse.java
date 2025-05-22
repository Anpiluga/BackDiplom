package com.example.vkr2.DTO;

import com.example.vkr2.entity.SparePart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с данными о запчасти")
public class SparePartResponse {

    @Schema(description = "ID запчасти", example = "1")
    private Long id;

    @Schema(description = "Название запчасти", example = "Масляный фильтр")
    private String name;

    @Schema(description = "Категория", example = "CONSUMABLES")
    private SparePart.Category category;

    @Schema(description = "Производитель", example = "Bosch")
    private String manufacturer;

    @Schema(description = "Цена за единицу (руб)", example = "450.0")
    private Double pricePerUnit;

    @Schema(description = "Количество", example = "10.0")
    private Double quantity;

    @Schema(description = "Единица измерения", example = "PIECES")
    private SparePart.Unit unit;

    @Schema(description = "Общая сумма (руб)", example = "4500.0")
    private Double totalSum;

    @Schema(description = "Описание", example = "Фильтр для замены масла")
    private String description;
}