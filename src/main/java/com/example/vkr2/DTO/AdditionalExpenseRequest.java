// AdditionalExpenseRequest.java
package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Запрос для создания или обновления дополнительного расхода")
public class AdditionalExpenseRequest {

    @Schema(description = "ID автомобиля", example = "1")
    @NotNull(message = "ID автомобиля не может быть пустым")
    private Long carId;

    @Schema(description = "Тип расхода", example = "Штраф")
    @NotBlank(message = "Тип расхода не может быть пустым")
    private String type;

    @Schema(description = "Цена (руб)", example = "1500.0")
    @NotNull(message = "Цена не может быть пустой")
    @Positive(message = "Цена должна быть положительной")
    private Double price;

    @Schema(description = "Дата и время", example = "2025-05-20T14:30:00")
    @NotNull(message = "Дата и время не могут быть пустыми")
    private LocalDateTime dateTime;

    @Schema(description = "Описание", example = "Штраф за превышение скорости")
    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    private String description;
}

