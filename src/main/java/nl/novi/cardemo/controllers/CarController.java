package nl.novi.cardemo.controllers;

import nl.novi.cardemo.models.Car;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

@RestController
@RequestMapping("/cars")
public class CarController {
    private List<Car> carList = new ArrayList<>();
    private Long currentId = 1L;

    // CREATE: Voeg een nieuwe auto toe
    @PostMapping
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        car.setId(currentId++);
        carList.add(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }

    // READ: Haal een specifieke auto op op basis van ID
    @GetMapping("/{id}")
    public ResponseEntity<Car> getCar(@PathVariable Long id) {
        var optionalCar = findCarById(id);
        if (optionalCar == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalCar);
    }

    // READ: Haal alle auto's op
    @GetMapping
    public ResponseEntity<List<Car>> getAllCars(@RequestParam(name = "brand", required = false) String brand) {
        if (brand != null) {
            List<Car> filteredCars = getFilteredCars(brand);
            return ResponseEntity.ok(filteredCars);
        } else {
            return ResponseEntity.ok(carList);
        }
    }

    private List<Car> getFilteredCars(String brand) {
        List<Car> filteredCars = new ArrayList<>();
        for (Car car : carList) {
            if (car.getBrand().equalsIgnoreCase(brand)) {
                filteredCars.add(car);
            }
        }
        return filteredCars;
    }

    // UPDATE: Werk een bestaande auto bij
    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car car) {
        var existingCar = findCarById(id);
        if (existingCar == null) {
            return ResponseEntity.notFound().build();
        }
        existingCar.setBrand(car.getBrand());
        existingCar.setModel(car.getModel());
        return ResponseEntity.ok(existingCar);
    }

    // DELETE: Verwijder een auto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        var optionalCar = getCar(id);
        if (optionalCar == null) {
            return ResponseEntity.notFound().build();
        }
        carList.remove(optionalCar);
        return ResponseEntity.noContent().build();
    }

    private Car findCarById(Long id) {
        for (Car car : carList) {
            if (car.getId().equals(id)) {
                return car;
            }
        }
        return null;
    }
}