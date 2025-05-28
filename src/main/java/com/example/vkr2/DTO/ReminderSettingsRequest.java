package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



// Request для настроек напоминаний
@Data
@Schema(description = "Запрос для настройки напоминаний")
public class ReminderSettingsRequest {

    @Schema(description = "ID автомобиля", example = "1")
    @NotNull(message = "ID автомобиля не может быть пустым")
    private Long carId;

    @Schema(description = "Интервал ТО в км", example = "15000")
    @NotNull(message = "Интервал ТО не может быть пустым")
    @Min(value = 1000, message = "Интервал ТО должен быть не менее 1000 км")
    private Integer serviceIntervalKm;

    @Schema(description = "За сколько км до ТО предупреждать", example = "500")
    @Min(value = 100, message = "Порог уведомления должен быть не менее 100 км")
    private Integer notificationThresholdKm = 500;

    @Schema(description = "Включены ли уведомления", example = "true")
    private Boolean notificationsEnabled = true;
}