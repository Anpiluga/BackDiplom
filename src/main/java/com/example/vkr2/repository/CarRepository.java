package com.example.vkr2.repository;

import com.example.vkr2.entity.Car;
import com.example.vkr2.entity.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByVin(String vin);
    Optional<Car> findByLicensePlate(String licensePlate);

    // Фильтры для поиска
    @Query("SELECT c FROM Car c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.vin) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:yearFrom IS NULL OR c.year >= :yearFrom) AND " +
            "(:yearTo IS NULL OR c.year <= :yearTo)")
    List<Car> findCarsWithFilters(@Param("search") String search,
                                  @Param("status") CarStatus status,
                                  @Param("yearFrom") Integer yearFrom,
                                  @Param("yearTo") Integer yearTo);

    List<Car> findByStatus(CarStatus status);
    List<Car> findByYear(Integer year);
    List<Car> findByYearBetween(Integer yearFrom, Integer yearTo);
}