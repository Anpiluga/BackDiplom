package com.example.vkr2.JWT.controllers;

import com.example.vkr2.DTO.AssignDriverRequest;
import com.example.vkr2.DTO.CarRequest;
import com.example.vkr2.DTO.CarResponse;
import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.CarStatus;
import com.example.vkr2.services.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cars")
@RequiredArgsConstructor
@Tag(name = "Управление автомобилями")
public class CarController {

    private static final Logger logger = LoggerFactory.getLogger(CarController.class);
    private final CarService carService;

    @Operation(summary = "Добавить авто")
    @PostMapping
    public ResponseEntity<CarResponse> addCar(@RequestBody @Valid CarRequest request) {
        logger.info("Adding car: {}", request);
        try {
            Car car = Car.builder()
                    .vin(request.getVin())
                    .licensePlate(request.getLicensePlate())
                    .brand(request.getBrand())
                    .model(request.getModel())
                    .year(request.getYear())
                    .odometr(request.getOdometr())
                    .fuelConsumption(request.getFuelConsumption())
                    .status(request.getStatus())
                    .counterType(request.getCounterType())
                    .secondaryCounterEnabled(request.getSecondaryCounterEnabled())
                    .fuelTankVolume(request.getFuelTankVolume())
                    .fuelType(request.getFuelType())
                    .description(request.getDescription())
                    .build();
            CarResponse savedCar = carService.addCar(car);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
        } catch (IllegalArgumentException e) {
            logger.error("Error adding car: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error adding car: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить все авто")
    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        logger.info("Fetching all cars");
        try {
            List<CarResponse> cars = carService.getAllCars();
            return ResponseEntity.ok(cars);
        } catch (Exception e) {
            logger.error("Error fetching cars: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить автомобили с фильтрами")
    @GetMapping("/filter")
    public ResponseEntity<List<CarResponse>> getCarsWithFilters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo) {
        logger.info("Fetching cars with filters - search: {}, status: {}, yearFrom: {}, yearTo: {}",
                search, status, yearFrom, yearTo);
        try {
            List<CarResponse> cars = carService.getCarsWithFilters(search, status, yearFrom, yearTo);
            return ResponseEntity.ok(cars);
        } catch (Exception e) {
            logger.error("Error fetching cars with filters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить автомобиль по ID")
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        logger.info("Fetching car with ID: {}", id);
        try {
            CarResponse car = carService.getCarById(id);
            return ResponseEntity.ok(car);
        } catch (EntityNotFoundException e) {
            logger.error("Car not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error fetching car: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Изменение информации об авто")
    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(@PathVariable Long id, @RequestBody @Valid CarRequest request) {
        logger.info("Updating car with ID: {}", id);
        try {
            Car car = Car.builder()
                    .id(id)
                    .vin(request.getVin())
                    .licensePlate(request.getLicensePlate())
                    .brand(request.getBrand())
                    .model(request.getModel())
                    .year(request.getYear())
                    .odometr(request.getOdometr())
                    .fuelConsumption(request.getFuelConsumption())
                    .status(request.getStatus())
                    .counterType(request.getCounterType())
                    .secondaryCounterEnabled(request.getSecondaryCounterEnabled())
                    .fuelTankVolume(request.getFuelTankVolume())
                    .fuelType(request.getFuelType())
                    .description(request.getDescription())
                    .build();
            CarResponse updatedCar = carService.updateCar(id, car);
            return ResponseEntity.ok(updatedCar);
        } catch (EntityNotFoundException e) {
            logger.error("Car not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid car data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error updating car: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Удаление авто")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        logger.info("Deleting car with ID: {}", id);
        try {
            carService.deleteCar(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("Car not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Unexpected error deleting car: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Привязать водителя к автомобилю")
    @PostMapping("/{id}/assign-driver")
    public ResponseEntity<CarResponse> assignDriver(@PathVariable Long id, @RequestBody @Valid AssignDriverRequest request) {
        logger.info("Assigning driver {} to car {}", request.getDriverId(), id);
        try {
            CarResponse updatedCar = carService.assignDriver(id, request.getDriverId());
            return ResponseEntity.ok(updatedCar);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error assigning driver: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Отвязать водителя от автомобиля")
    @PostMapping("/{id}/unassign-driver")
    public ResponseEntity<CarResponse> unassignDriver(@PathVariable Long id) {
        logger.info("Unassigning driver from car {}", id);
        try {
            CarResponse updatedCar = carService.unassignDriver(id);
            return ResponseEntity.ok(updatedCar);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid unassignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error unassigning driver: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}