
package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "Ответ с настройками напоминаний")
public class ReminderSettingsResponse {

    @Schema(description = "ID настройки", example = "1")
    private Long id;

    @Schema(description = "ID автомобиля", example = "1")
    private Long carId;

    @Schema(description = "Марка и модель автомобиля", example = "Honda Civic")
    private String carDetails;

    @Schema(description = "Интервал ТО в км", example = "15000")
    private Integer serviceIntervalKm;

    @Schema(description = "За сколько км до ТО предупреждать", example = "500")
    private Integer notificationThresholdKm;

    @Schema(description = "Включены ли уведомления", example = "true")
    private Boolean notificationsEnabled;
}