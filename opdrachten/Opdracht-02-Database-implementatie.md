**Opdracht: Car Repository**

Welkom bij deze opdracht! Je hebt twee opties om deze opdracht uit te voeren:
1. **Bouw de applicatie volledig vanaf scratch** met behulp van de richtlijnen hieronder.
2. **Clone de vorige iteratie van de repository** en breid deze uit. (resultaat iteratie 1)[https://github.com/hogeschoolnovi/cardemo/tree/feature/Iteration-1]

Voor beide opties gelden dezelfde functionele eisen en testscenario’s. Als je een clone van de repository maakt, houd er dan rekening mee dat sommige delen gerefactored moeten worden, zoals de **controller** en het **Car-model**.

### **Fase 1: Projectopzet met Maven**
**Doel:** Creëer een nieuw Spring Boot-project met Maven.


1. **Optie 1: Nieuw project vanaf scratch**
   - Gebruik **Spring Initializr** om een nieuw project te genereren met de volgende afhankelijkheden:
      - **Spring Web** (voor het bouwen van RESTful API's)
      - **Spring Data JPA** (voor database interactie)
      - **PostgreSQL Driver** (voor verbinding met PostgreSQL database)
   - Selecteer **Maven** als build tool en **Java** als programmeertaal.
   - Download het project en importeer het in je IDE.

2. **Optie 2: Clone de bestaande repository**
   - Clone de vorige versie van het project via:
     ```sh
     git clone <repository-url>
     ```
   - Open het project in je IDE en zorg ervoor dat alle dependencies correct zijn geïnstalleerd.
   - **Spring Data JPA** (voor database interactie)
   - **PostgreSQL Driver** (voor verbinding met PostgreSQL database)

   - **Refactor de code** waar nodig, bijvoorbeeld:
      - **Controller aanpassen** om extra functionaliteit of verbeteringen toe te voegen.
      - **Car-model controleren** en uitbreiden indien nodig.
---

### **Fase 2: Database Configuratie**
**Doel:** Stel de verbinding met je **PostgreSQL**-database in.


1. Open het bestand **application.properties** in de map `src/main/resources`.
2. Voeg de volgende configuratie toe (pas de waarden aan naar je eigen database):

<details>
<summary><strong>database settings</strong></summary>

```properties
spring.application.name=cardemo

spring.datasource.url=jdbc:postgresql://localhost:5432/cardemo
spring.datasource.username=${POSTGRESQL_USERNAME}
spring.datasource.password=${POSTGRESQL_PASSWORD}

# JPA instellingen
spring.jpa.show-sql=true
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=create

# Database initialisatie
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
   ```
3. Zorg ervoor dat **PostgreSQL draait** en toegankelijk is op de aangegeven URL.
4. Als de database nog niet bestaat, maak deze dan aan in **pgAdmin**.
</details>

---

### **Fase 3: Entity Definitie**
**Doel:** Definieer een **Car** entiteit voor je applicatie.


1. Maak een package aan voor je entiteiten (bijv. `com.example.carapp.model`).
2. Maak een klasse **Car** met de volgende structuur:

- private Long id;
- private String brand;
- private String model;

3. voeg de juiste annotaties toe.

<details>
<summary><strong>Maak de entiteit aan</strong></summary>

```java
   import jakarta.persistence.*;
   
   @Entity
   @Table(name = "cars")
   public class Car {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       private String brand;
       private String model;
   
       // Getters en setters
   }
   ```
</details>

---

### **Fase 4: Repository Laag**
**Doel:** Implementeer de repository-laag.


1. Maak een package `com.example.carapp.repository`.
2. Voeg een interface toe genaamd **CarRepository** die JpaRepository uitbreidt
3. maak een methode aan die de cars op basis van brand kan filteren.

<details>
<summary><strong>Maak de repository interface</strong></summary>

```java
   import org.springframework.data.jpa.repository.JpaRepository;
   import java.util.List;
   
   public interface CarRepository extends JpaRepository<Car, Long> {
       List<Car> findByBrand(String brand);
   }
   ```
</details>

---

### **Fase 5: Controller Laag**
**Doel:** Implementeer de controller-laag.

Een controller is verantwoordelijk voor het afhandelen van HTTP-verzoeken en het communiceren met de repository laag (uiteindelijk wordt dit vervangen door een servicelaag). Het vertaalt inkomende verzoeken naar methoden die de gewenste acties uitvoeren en retourneert de juiste HTTP-statuscodes en gegevens.

#### **Wat doet de controller?**
1. **Endpoints**
   - **POST /cars** → Voegt een nieuwe auto toe.
   - **GET /cars** → Haalt alle auto's op of filtert op merk.
   - **GET /cars/{id}** → Haalt een specifieke auto op via ID.
   - **PUT /cars/{id}** → Werkt een bestaande auto bij op basis van ID.
   - **DELETE /cars/{id}** → Verwijdert een auto op basis van ID.

2. **Methodes die de service-laag aanroepen**
   - `createCar()` → Roept `save()` aan vanuit `CarService` om een auto op te slaan.
   - `getCars()` → Roept `getAll()` aan om een lijst met auto's op te halen (optioneel gefilterd op merk).
   - `getCarById()` → Roept `getById()` aan om een specifieke auto op te halen.
   - `updateCar()` → Roept `updateCar()` aan om een auto bij te werken.
   - `deleteCar()` → Roept `delete()` aan om een auto te verwijderen.

3. **Gebruik van annotaties**
   - `@RestController` → Geeft aan dat deze klasse een REST-controller is.
   - `@RequestMapping("/cars")` → Definieert de basis-URL voor alle endpoints in deze controller.
   - `@PostMapping`, `@GetMapping`, `@PutMapping`, `@DeleteMapping` → Specificeert de HTTP-methodes voor de endpoints.
   - `@RequestBody` → Geeft aan dat een methode een JSON-body als input verwacht.
   - `@PathVariable` → Haalt de ID-waarde uit de URL op voor de update- en delete-methodes.
   - `@RequestParam` → Optionele filterparameter om auto's op merk te zoeken.

#### **Implementatie van de REST-controller**
<details>
<summary><strong>Maak de REST-controller</strong></summary>

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @PostMapping
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        Car savedCar = carRepository.save(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
    }

    @GetMapping
    public ResponseEntity<List<Car>> getCars(@RequestParam(required = false) String brand) {
        List<Car> cars = (brand == null) ? carRepository.findAll() : carRepository.findByBrand(brand);
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        Optional<Car> car = carRepository.findById(id);
        return car.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car carDetails) {
        return carRepository.findById(id).map(car -> {
            car.setBrand(carDetails.getBrand());
            car.setModel(carDetails.getModel());
            Car updatedCar = carRepository.save(car);
            return ResponseEntity.ok(updatedCar);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```
</details>

---
