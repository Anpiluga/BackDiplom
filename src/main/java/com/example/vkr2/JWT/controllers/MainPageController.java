package com.example.vkr2.JWT.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/main")
@Tag(name = "Главная страница")
public class MainPageController {

    @Operation(summary = "Получить данные для главной страницы")
    @GetMapping
    public ResponseEntity<Map<String, String>> getMainPageData() {
        Map<String, String> response = new HashMap<>();
        response.put("leftTopContent", "Наш веб-сервис предоставляет автоматизацию учёта затрат на плановое ТО автомобилей в таксопарках.");
        response.put("rightContent", "Наши возможности: ...");
        return ResponseEntity.ok(response);
    }
}