package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ с данными о сервисной задаче")
public class ServiceTaskResponse {

    @Schema(description = "ID задачи", example = "1")
    private Long id;

    @Schema(description = "ID сервисной записи", example = "1")
    private Long serviceRecordId;

    @Schema(description = "Информация о сервисной записи", example = "Сервисная запись по автомобилю Honda Civic А123БВ45")
    private String serviceRecordDetails;

    @Schema(description = "Название задачи", example = "Замена масла")
    private String taskName;

    @Schema(description = "Описание задачи", example = "Заменить моторное масло и масляный фильтр")
    private String taskDescription;
}