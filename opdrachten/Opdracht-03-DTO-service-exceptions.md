## Opdracht: Uitbreiding en Refactoring van de Car Entiteit

### Toevoegen van Dependencies aan `pom.xml`
Om de benodigde functionaliteiten toe te voegen, moeten de volgende dependencies in de `pom.xml` van je project worden opgenomen:

Hiermee voeg je ondersteuning toe voor  validatie voor invoergegevens.

<details><summary>Bekijk de dependencies</summary>

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
</details>

### Uitbreiding van de `Car` Entiteit
#### Stap 1: Toevoegen van het `year` Veld
We voegen een nieuw veld `year` toe aan de `Car` klasse om het bouwjaar te ondersteunen.

<details><summary>Bekijk de code</summary>

```java
private int year;
```
</details>

#### Stap 2: Implementeren van Getters en Setters
Getters en setters worden toegevoegd om toegang te krijgen tot het `year` veld en het bij te werken.

De rest van de klasse blijft hetzelfde.

<details><summary>Bekijk de getters en setters</summary>

```java
public int getYear() {
    return year;
}

public void setYear(int year) {
    this.year = year;
}
```
</details>

### Aanmaken van `CarInputDTO`
Deze klasse wordt gebruikt om invoergegevens te ontvangen en te valideren voordat deze worden opgeslagen in de database.

#### Validaties:
- `@NotNull`: Zorgt ervoor dat het bouwjaar niet leeg mag zijn.
- `@Min(1886)`: Voorkomt invoer van een bouwjaar vóór 1886 (eerste auto ooit gemaakt).
- `@Max(2024)`: Voorkomt een toekomstig bouwjaar.
- `brand` en `model` mogen niet blank zijn.

<details><summary>Bekijk de code</summary>

```java
package nl.novi.cardemo.dtos;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CarInputDTO {

    @NotBlank(message = "Brand cannot be empty")
    private String brand;

    @NotBlank(message = "Model cannot be empty")
    private String model;

    @NotNull(message = "Year cannot be null")
    @Min(value = 1886, message = "Year must be after 1886")
    @Max(value = 2024, message = "Year must be before or equal to 2024")
    private Integer year;
}
```
</details>

### Aanmaken van `CarResponseDTO`
Deze klasse wordt gebruikt om de gegevens van een auto terug te sturen naar de client, inclusief de berekening van de leeftijd van de auto.

<details><summary>Bekijk de code</summary>

```java
import java.time.Year;

public class CarResponseDTO {
    private Long id;
    private String brand;
    private String model;
    private int year;
    private int age;

    public int getAge() {
        return  Year.now().getValue() -year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
```
</details>

### Aanmaken van `CarMapper`
De `CarMapper` klasse helpt bij het omzetten van `Car` entiteiten naar DTO's en andersom.

<details><summary>Bekijk de code</summary>

```java
package nl.novi.cardemo.mappers;

import nl.novi.cardemo.dtos.CarInputDTO;
import nl.novi.cardemo.dtos.CarResponseDTO;
import nl.novi.cardemo.models.Car;

import java.util.List;
import java.util.stream.Collectors;

public class CarMapper {
    public static CarResponseDTO toResponseDTO(Car car) {
        var result = new CarResponseDTO();
        result.setBrand(car.getBrand());
        result.setModel(car.getModel());
        result.setYear(car.getYear());
        result.setId(car.getId());
        return result;
    }

    public static Car toEntity(CarInputDTO carCreateDTO) {
        Car car = new Car();
        car.setBrand(carCreateDTO.getBrand());
        car.setModel(carCreateDTO.getModel());
        car.setYear(carCreateDTO.getYear());
        return car;
    }

    public static List<CarResponseDTO> toResponseDTOList(List<Car> cars) {
        return cars.stream().map(CarMapper::toResponseDTO).collect(Collectors.toList());
    }
}
```
</details>

### Verplaatsen van Business Logica naar `CarService`
De `CarService` klasse bevat de logica voor het ophalen, opslaan en bewerken van auto's, zodat de controller schoon blijft.

<details><summary>Bekijk de code</summary>

```java
package nl.novi.cardemo.services;

import nl.novi.cardemo.models.Car;
import nl.novi.cardemo.repositories.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {
    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car save(Car
                            car) {
        return carRepository.save(car);
    }

    public List<Car> getAll(String brand) {
        return (brand == null) ? carRepository.findAll() : carRepository.findByBrand(brand);
    }

    public Optional<Car> getById(Long id) {
        return carRepository.findById(id);
    }

    public Optional<Car> updateCar(Long id, Car carDetails) {
        Optional<Car> carOptional = carRepository.findById(id);
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            car.setBrand(carDetails.getBrand());
            car.setModel(carDetails.getModel());
            car.setYear(carDetails.getYear());
            return Optional.of(carRepository.save(car));
        }
        return Optional.empty();
    }

    public boolean delete(Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
```
</details>

## Opdracht: Wijzigingen doorvoeren in de `CarController`

Hieronder volgen de stappen om wijzigingen aan te brengen in de `CarController`. Dit gebeurt in twee fasen: eerst passen we de controller aan om de service (`CarService`) te gebruiken, en daarna activeren we de DTO’s in de controller.

---

### Stap 1: Controller Aanpassen om de Service te Gebruiken

#### 1. Open de `CarController` klasse
- Vervang de directe interactie met `CarRepository` door `CarService`.
- Pas de constructor aan om `CarService` te injecteren.

#### 2. Pas de `createCar` methode aan
- Roep `carService.save()` aan in plaats van `carRepository.save()`.

<details><summary>Bekijk de code</summary>

```java
@PostMapping
public ResponseEntity<Car> createCar(@RequestBody Car car) {
    Car savedCar = carService.save(car);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
}
```
</details>

#### 3. Pas de `getCars` methode aan
- Gebruik `carService.getAll()` in plaats van `carRepository.findAll()`.

<details><summary>Bekijk de code</summary>

```java
@GetMapping
public ResponseEntity<List<Car>> getCars(@RequestParam(required = false) String brand) {
    return ResponseEntity.ok(carService.getAll(brand));
}
```
</details>

#### 4. Pas de `getCarById` methode aan
- Gebruik `carService.getById()` in plaats van `carRepository.findById()`.

<details><summary>Bekijk de code</summary>

```java
@GetMapping("/{id}")
public ResponseEntity<Car> getCarById(@PathVariable Long id) {
    return carService.getById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```
</details>

#### 5. Pas de `updateCar` methode aan
- Gebruik `carService.updateCar()` in plaats van `carRepository.save()`.

<details><summary>Bekijk de code</summary>

```java
@PutMapping("/{id}")
public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car carDetails) {
    Optional<Car> updatedCar = carService.updateCar(id, carDetails);
    return updatedCar.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
}
```
</details>

#### 6. Pas de `deleteCar` methode aan
- Gebruik `carService.delete()` in plaats van `carRepository.deleteById()`.

<details><summary>Bekijk de code</summary>

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
    if (carService.delete(id)) {
        return ResponseEntity.noContent().build();
    } else {
        return ResponseEntity.notFound().build();
    }
}
```
</details>

---

### Stap 2: DTO’s Activeren in `CarController`

#### 1. Importeer de benodigde DTO’s en Mapper
- Voeg `CarInputDTO`, `CarResponseDTO` en `CarMapper` toe.
- Gebruik `@Valid` voor validatie van de invoer.

#### 2. Pas de `createCar` methode aan
- Accepteer een `CarInputDTO` in plaats van een `Car` object.
- Gebruik `CarMapper` om de DTO om te zetten naar een entiteit en vice versa.

<details><summary>Bekijk de code</summary>

```java
@PostMapping
public ResponseEntity<?> createCar(@Valid @RequestBody CarInputDTO car) {
    Car savedCar = carService.save(CarMapper.toEntity(car));
    return ResponseEntity.status(HttpStatus.CREATED).body(CarMapper.toResponseDTO(savedCar));
}
```
</details>

#### 3. Pas de `updateCar` methode aan
- Gebruik DTO’s voor de invoer en uitvoer.

<details><summary>Bekijk de code</summary>

```java
@PutMapping("/{id}")
public ResponseEntity<CarResponseDTO> updateCar(@PathVariable Long id, @Valid @RequestBody CarInputDTO carDetails) {
    Optional<Car> updatedCar = carService.updateCar(id, CarMapper.toEntity(carDetails));
    return updatedCar.map(car -> ResponseEntity.ok(CarMapper.toResponseDTO(car)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
}
```
</details>

#### 4. Pas de `getCarById` methode aan
- Retourneer een `CarResponseDTO` in plaats van een `Car`.

<details><summary>Bekijk de code</summary>

```java
@GetMapping("/{id}")
public ResponseEntity<CarResponseDTO> getCarById(@PathVariable Long id) {
    return carService.getById(id)
        .map(car -> ResponseEntity.ok(CarMapper.toResponseDTO(car)))
        .orElseGet(() -> ResponseEntity.notFound().build());
}
```
</details>

#### 5. Pas de `getCars` methode aan
- Retourneer een lijst van `CarResponseDTO` in plaats van `Car` objecten.

<details><summary>Bekijk de code</summary>

```java
@GetMapping
public ResponseEntity<List<CarResponseDTO>> getCars(
        @RequestParam(required = false) String brand) {
    return ResponseEntity.ok(CarMapper.toResponseDTOList(carService.getAll(brand)));
}
```
</details>

---

Met deze wijzigingen gebruik je eerst de service in de controller en activeer je vervolgens de DTO’s. Dit zorgt voor een duidelijke scheiding van verantwoordelijkheden en een robuustere applicatie.




## Toevoegen van Global Exception Handling in Spring Boot

Om de validatiefouten in je Spring Boot applicatie correct af te handelen, voeg je een `GlobalExceptionHandler` klasse toe. Dit zorgt ervoor dat foutmeldingen netjes worden geretourneerd in een gestandaardiseerd formaat.

### Maak een nieuwe package `config`
- In je projectstructuur, maak een nieuwe package aan genaamd `nl.novi.cardemo.config`.
- Dit helpt om configuratie-gerelateerde klassen gescheiden te houden van de rest van je code.

### Maak de `GlobalExceptionHandler` klasse aan
- Voeg een nieuwe Java-klasse toe binnen de `config` package met de naam `GlobalExceptionHandler`.

### Implementeer de foutafhandeling
- Annotatie `@RestControllerAdvice` wordt gebruikt om globale foutafhandeling toe te passen op alle controllers.
- Gebruik `@ExceptionHandler(MethodArgumentNotValidException.class)` om validatiefouten op te vangen.
- Maak een `Map<String, String>` om foutmeldingen te verzamelen en terug te sturen.

<details><summary>Bekijk de volledige code</summary>

```java
package nl.novi.cardemo.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
```
</details>

### Test de validatie-afhandeling
- Zorg ervoor dat je DTO’s validatieannotaties bevatten, zoals `@NotNull` en `@Min`.
- Probeer een `POST`-aanvraag naar je API te sturen met ongeldige gegevens.
- Je zou een gestructureerde JSON-respons moeten krijgen met foutmeldingen per veld.

Na deze stappen heb je een werkende globale foutafhandelaar die validatiefouten netjes afhandelt en als JSON-response retourneert.

