package com.example.vkr2.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о водителе")
public class DriverResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phoneNumber;
    private boolean hasCar;
}