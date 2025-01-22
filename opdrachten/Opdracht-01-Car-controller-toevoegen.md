# Car controller bouwen

Om de `CarController` stap voor stap op te bouwen, kun je de volgende stappen volgen:

1.  **Start met de basis setup**:
    1.  maak een project in IntelliJ, maak een dependency aan voor spring.web
    2.  maak een package aan voor models
    3.  maak een package aan voor controllers
    4.  maak de car class

```java
public class Car {
    private Long id;
    private String brand;
    private String model;

   // getter en setters toevoegen
}
```

-   maak de controller aan en plaats de annotaties.

```java
@RestController
@RequestMapping("/cars")
public class CarController {
    private List<Car> carList = new ArrayList<>();
    private Long currentId = 1L;
}
```

1.  **CREATE operatie**:
    1.  Bespreek het gebruik van `@PostMapping` voor het creëren van een nieuwe resource.
    2.  Leg de werking van `@RequestBody` en `ResponseEntity` uit.
    3.  Implementeer de methode om een auto toe te voegen aan de lijst.

```java
@PostMapping
public ResponseEntity<Car> createCar(@RequestBody Car car) {
    car.setId(currentId++);
    carList.add(car);
    return ResponseEntity.status(HttpStatus.CREATED).body(car);
}
```

1.  **READ operatie voor één auto**:
    1.  Introduceer `@GetMapping` en `@PathVariable`.
    2.  Bespreek het zoeken van een auto met een bepaalde ID.
    3.  Leg het belang uit van het afhandelen van situaties waarbij een auto niet gevonden wordt.

```java
@GetMapping("/{id}")
public ResponseEntity<Car> getCar(@PathVariable Long id) {
    var optionalCar = findCarById(id);
    if (optionalCar == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(optionalCar);
}
```

**Helper methode**:

1.  implementeer de `findCarById` methode, wat een veelgebruikte functionaliteit in de controller is.

```java
   private Car findCarById(Long id) {
       for (Car car : carList) {
           if (car.getId().equals(id)) {
               return car;
           }
       }
       return null;
   }
```

1.  **READ operatie voor alle auto's**:
    1.  Bespreek het gebruik van `@RequestParam` voor optionele parameters.
    2.  Leg het filteren van auto's op basis van merk uit.

```java
@GetMapping
public ResponseEntity<List<Car>> getAllCars(@RequestParam(name = "brand", required = false) String brand) {
    if (brand != null) {
        List<Car> filteredCars = getFilteredCars(brand);
        return ResponseEntity.ok(filteredCars);
    } else {
        return ResponseEntity.ok(carList);
    }
}
```

1.  **UPDATE operatie**:
    1.  Introduceer `@PutMapping`.
    2.  Bespreek het bijwerken van een bestaande auto en het belang van het controleren of de auto bestaat.

```java
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
```

1.  **DELETE operatie**:
    1.  Leg het gebruik van `@DeleteMapping` uit.
    2.  Bespreek hoe een auto verwijderd kan worden en hoe te reageren als de auto niet gevonden wordt.

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
    var optionalCar = findCarById(id);
    if (optionalCar == null) {
        return ResponseEntity.notFound().build();
    }
    carList.remove(optionalCar);
    return ResponseEntity.noContent().build();
}
```

## Eind resultaat code

```java
package nl.novi.les10prerun.controllers;


import nl.novi.les10prerun.models.Car;
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
```
