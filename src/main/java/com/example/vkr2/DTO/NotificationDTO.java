package com.example.vkr2.DTO;

import com.example.vkr2.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Уведомление о необходимости ТО")
public class NotificationDTO {

    @Schema(description = "ID уведомления", example = "1")
    private Long id;

    @Schema(description = "ID автомобиля", example = "1")
    private Long carId;

    @Schema(description = "Информация об автомобиле", example = "Honda Civic А123БВ45")
    private String carDetails;

    @Schema(description = "Текст уведомления", example = "До следующего ТО осталось 500 км")
    private String message;

    @Schema(description = "Тип уведомления", example = "WARNING")
    private Notification.NotificationType type;

    @Schema(description = "Км до следующего ТО", example = "500")
    private Integer kmToNextService;

    @Schema(description = "Количество выполненных ТО", example = "3")
    private Integer serviceCount;

    @Schema(description = "Дата создания уведомления", example = "2025-05-30T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Прочитано ли уведомление", example = "false")
    private boolean read;

    @Schema(description = "Активно ли уведомление", example = "true")
    private boolean active;
}