package com.example.vkr2.DTO;

import com.example.vkr2.entity.SparePart;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Запрос для создания или обновления запчасти")
public class SparePartRequest {

    @Schema(description = "Название запчасти", example = "Масляный фильтр")
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Schema(description = "Категория", example = "CONSUMABLES")
    @NotNull(message = "Категория не может быть пустой")
    private SparePart.Category category;

    @Schema(description = "Производитель", example = "Bosch")
    @NotBlank(message = "Производитель не может быть пустым")
    private String manufacturer;

    @Schema(description = "Цена за единицу (руб)", example = "450.0")
    @NotNull(message = "Цена за единицу не может быть пустой")
    @Positive(message = "Цена должна быть положительной")
    private Double pricePerUnit;

    @Schema(description = "Количество", example = "10.0")
    @NotNull(message = "Количество не может быть пустым")
    @Positive(message = "Количество должно быть положительным")
    private Double quantity;

    @Schema(description = "Единица измерения", example = "PIECES")
    @NotNull(message = "Единица измерения не может быть пустой")
    private SparePart.Unit unit;

    @Schema(description = "Описание", example = "Фильтр для замены масла")
    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    private String description;
}