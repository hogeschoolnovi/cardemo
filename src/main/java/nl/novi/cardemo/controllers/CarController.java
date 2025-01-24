package nl.novi.cardemo.controllers;

import jakarta.validation.Valid;
import nl.novi.cardemo.dtos.car.CarInputDTO;
import nl.novi.cardemo.dtos.car.CarResponseDTO;
import nl.novi.cardemo.mappers.CarMapper;
import nl.novi.cardemo.models.Car;
import nl.novi.cardemo.services.CarService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@Valid
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping
    public ResponseEntity<?> createCar(@Valid @RequestBody CarInputDTO car) {

        Car savedCar = carService.save(CarMapper.toEntity(car));
        return ResponseEntity.status(HttpStatus.CREATED).body(CarMapper.toResponseDTO(savedCar));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponseDTO> updateCar(@PathVariable Long id, @Valid @RequestBody CarInputDTO carDetails) {
        Optional<Car> updatedCar = carService.updateCar(id, CarMapper.toEntity(carDetails));
        if (updatedCar.isPresent()) {
            return ResponseEntity.ok(CarMapper.toResponseDTO(updatedCar.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        var result = carService.delete(id);
        if (result) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponseDTO> getCarById(@PathVariable Long id) {
        Optional<Car> car = carService.getById(id);
        if (car.isPresent()) {
            return ResponseEntity.ok(CarMapper.toResponseDTO(car.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CarResponseDTO>> getCars(
            @RequestParam(required = false) String brand) {
        return ResponseEntity.ok(CarMapper.toResponseDTOList(carService.getAll(brand)));
    }
}

