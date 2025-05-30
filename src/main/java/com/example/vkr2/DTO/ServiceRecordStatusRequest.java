package com.example.vkr2.DTO;

import com.example.vkr2.entity.ServiceRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос для изменения статуса сервисной записи")
public class ServiceRecordStatusRequest {

    @Schema(description = "Новый статус", example = "COMPLETED")
    @NotNull(message = "Статус не может быть пустым")
    private ServiceRecord.ServiceStatus status;
}