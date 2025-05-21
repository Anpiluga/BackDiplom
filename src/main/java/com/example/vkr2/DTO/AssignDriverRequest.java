package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос для привязки водителя к автомобилю")
public class AssignDriverRequest {

    @Schema(description = "ID водителя", example = "1")
    @NotNull(message = "ID водителя не может быть пустым")
    private Long driverId;
}