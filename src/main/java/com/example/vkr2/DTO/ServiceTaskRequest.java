package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Запрос для создания или обновления сервисной задачи")
public class ServiceTaskRequest {

    @Schema(description = "ID сервисной записи", example = "1")
    @NotNull(message = "ID сервисной записи не может быть пустым")
    private Long serviceRecordId;

    @Schema(description = "Название задачи", example = "Замена масла")
    @NotBlank(message = "Название задачи не может быть пустым")
    @Size(max = 500, message = "Название задачи не может превышать 500 символов")
    private String taskName;

    @Schema(description = "Описание задачи", example = "Заменить моторное масло и масляный фильтр")
    @Size(max = 2000, message = "Описание задачи не может превышать 2000 символов")
    private String taskDescription;
}