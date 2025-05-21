package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Запрос для создания или обновления водителя")
public class DriverRequest {

    @Schema(description = "Имя водителя", example = "Иван")
    @Size(max = 50, message = "Имя должно содержать до 50 символов")
    private String firstName;

    @Schema(description = "Фамилия водителя", example = "Иванов")
    @Size(max = 50, message = "Фамилия должна содержать до 50 символов")
    private String lastName;

    @Schema(description = "Отчество водителя", example = "Иванович")
    @Size(max = 50, message = "Отчество должно содержать до 50 символов")
    private String middleName;

    @Schema(description = "Номер телефона", example = "+79991234567")
    @NotBlank(message = "Номер телефона не может быть пустым")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Некорректный формат номера телефона")
    private String phoneNumber;
}