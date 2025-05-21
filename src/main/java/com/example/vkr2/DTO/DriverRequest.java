package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Запрос для создания или обновления водителя")
public class DriverRequest {

    @Schema(description = "ФИО водителя", example = "Иванов Иван Иванович")
    @NotBlank(message = "ФИО не может быть пустым")
    @Size(min = 2, max = 100, message = "ФИО должно содержать от 2 до 100 символов")
    private String fullName;

    @Schema(description = "Номер телефона", example = "+79991234567")
    @NotBlank(message = "Номер телефона не может быть пустым")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Некорректный формат номера телефона")
    private String phoneNumber;
}