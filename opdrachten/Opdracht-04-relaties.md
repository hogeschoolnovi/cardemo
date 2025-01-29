### Opdracht voor Developer

Voer de volgende stappen uit in de gegeven volgorde om de nieuwe functionaliteit toe te voegen en bestaande functionaliteiten uit te breiden.

**Refactoring voor een overzichtelijke Codebase**

Om de codebase overzichtelijk en onderhoudbaar te houden, is het belangrijk om refactoring door te voeren terwijl we nieuwe functionaliteiten toevoegen. Dit helpt om een duidelijke structuur te behouden en voorkomt onnodige duplicatie of complexe afhankelijkheden.

### **Huidige projectstructuur**
De voorbeeld eindstructuur van het project ziet er als volgt uit:

```
C:.
├───main
│   ├───java
│   │   └───nl
│   │       └───novi
│   │           └───cardemo
│   │               │   CardemoApplication.java
│   │               │
│   │               ├───config
│   │               │       GlobalExceptionHandler.java
│   │               │
│   │               ├───controllers
│   │               │       CarController.java
│   │               │       CarRegistrationController.java
│   │               │
│   │               ├───dtos
│   │               │   ├───car
│   │               │   │       CarInputDTO.java
│   │               │   │       CarResponseDTO.java
│   │               │   │
│   │               │   └───carRegistrations
│   │               │           CarRegistrationCreateDTO.java
│   │               │           CarRegistrationResponseDTO.java
│   │               │           CarRegistrationUpdateDTO.java
│   │               │
│   │               ├───mappers
│   │               │       CarMapper.java
│   │               │       CarRegistrationMapper.java
│   │               │
│   │               ├───models
│   │               │       Accessory.java
│   │               │       Car.java
│   │               │       CarRegistration.java
│   │               │       RepairInvoice.java
│   │               │
│   │               ├───repositories
│   │               │       CarRegistrationRepository.java
│   │               │       CarRepository.java
│   │               │
│   │               └───services
│   │                       CarRegistrationService.java
│   │                       CarService.java
│   │
│   └───resources
│           application.properties
│           data.sql
```

### **1. Aanmaken van nieuwe entiteiten**

#### **Stap 1: Maak een model `Accessory`**
Maak een model `Accessory` aan met de volgende velden:
- `id`: een unieke identificatie, automatisch gegenereerd.
- `name`: de naam van de accessoire, dit mag niet leeg zijn.

```java
@Entity
public class Accessory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;
    
    // Getters en setters
}
```

#### **Stap 2: Maak een model `CarRegistration`**
Maak een model `CarRegistration` aan met de volgende velden:
- `id`: een unieke identificatie, automatisch gegenereerd.
- `registrationNumber`: een unieke registratiecode, mag niet leeg zijn.
- `car`: een verwijzing naar het `Car`-model.

```java
@Entity
public class CarRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Registration number cannot be empty")
    private String registrationNumber;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
    
    // Getters en setters
}
```

#### **Stap 3: Maak een model `RepairInvoice`**
Maak een model `RepairInvoice` aan met de volgende velden:
- `id`: een unieke identificatie, automatisch gegenereerd.
- `description`: een beschrijving van de reparatie, mag niet leeg zijn.
- `car`: een verwijzing naar het `Car`-model.

```java
@Entity
public class RepairInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
    
    // Getters en setters
}
```

### **2. Relaties toevoegen aan de `Car` klasse**

Breid de bestaande `Car` klasse uit met de juiste relaties om deze te koppelen aan andere entiteiten. Dit zorgt ervoor dat een auto kan worden verbonden met meerdere reparatiefacturen en accessoires.

#### **Wat moet je doen?**
1. **Voeg een een-op-veel relatie toe met `RepairInvoice`**
    - Een auto kan meerdere reparatiefacturen hebben.
    - Gebruik de annotatie `@OneToMany` om deze relatie correct te definiëren.
    - Zorg ervoor dat de facturen correct verwijzen naar de betreffende auto.

2. **Voeg een veel-op-veel relatie toe met `Accessory`**
    - Een auto kan meerdere accessoires hebben en accessoires kunnen bij meerdere auto's horen.
    - Gebruik de annotatie `@ManyToMany` en definieer een koppelingsentiteit (`car_accessories`).

3. **Pas de `Car` klasse aan met de volgende code:**

```java
@OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
private List<RepairInvoice> repairInvoices;

@ManyToMany
@JoinTable(
        name = "car_accessories",
        joinColumns = @JoinColumn(name = "car_id"),
        inverseJoinColumns = @JoinColumn(name = "accessory_id")
)
private List<Accessory> accessories;
```

#### **Verwachting na implementatie**
- De `Car` klasse heeft een correcte relatie met `RepairInvoice` en `Accessory`.
- Het toevoegen en ophalen van reparatiefacturen en accessoires werkt zonder problemen.
- De database kan de juiste relaties hanteren dankzij de annotaties.

Controleer na implementatie of de datastructuur correct wordt opgeslagen in de database.


### **3. Repositories aanmaken**

Om de applicatie in staat te stellen gegevens op te slaan en op te halen uit de database, moeten we repositories aanmaken voor de verschillende entiteiten.

#### **Wat moet je doen?**
1. **Maak nieuwe repository interfaces aan in de map**
    - Plaats de nieuwe bestanden in `src/main/java/nl/novi/cardemo/repositories/`.
    - Zorg ervoor dat deze interfaces de juiste entiteiten en `JpaRepository` implementeren.

2. **Implementeer de volgende repositories:**
    - `CarRegistrationRepository` voor het beheren van autoregistraties.
    - `AccessoryRepository` voor het beheren van accessoires.
    - `RepairInvoiceRepository` voor het beheren van reparatiefacturen.

3. **Voeg de volgende code toe aan elk bestand:**

##### **CarRegistrationRepository.java**
```java
package nl.novi.cardemo.repositories;

import nl.novi.cardemo.models.CarRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRegistrationRepository extends JpaRepository<CarRegistration, Long> {
}
```

##### **AccessoryRepository.java**
```java
package nl.novi.cardemo.repositories;

import nl.novi.cardemo.models.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessoryRepository extends JpaRepository<Accessory, Long> {
}
```

##### **RepairInvoiceRepository.java**
```java
package nl.novi.cardemo.repositories;

import nl.novi.cardemo.models.RepairInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairInvoiceRepository extends JpaRepository<RepairInvoice, Long> {
}
```

### **4. Car Registration Service aanmaken**

Om de logica voor het registreren van auto’s te scheiden van de controller, maken we een serviceklasse aan voor `CarRegistration`.

#### **Wat moet je doen?**
1. **Maak een nieuwe klasse aan in de map:**
    - **Locatie:** `src/main/java/nl/novi/cardemo/services/`
    - **Naam:** `CarRegistrationService.java`

2. **Implementeer de serviceklasse met de volgende verantwoordelijkheden:**
    - Ophalen van een auto op basis van het `carId`.
    - Koppelen van een `CarRegistration` aan de juiste `Car`.
    - Opslaan van de registratie in de database.

<details>
<summary>Hint: Voeg de volgende klasse toe aan `src/main/java/nl/novi/cardemo/services/`</summary>

```java
package nl.novi.cardemo.services;

import jakarta.persistence.EntityNotFoundException;
import nl.novi.cardemo.mappers.CarRegistrationMapper;
import nl.novi.cardemo.models.Car;
import nl.novi.cardemo.models.CarRegistration;
import nl.novi.cardemo.repositories.CarRegistrationRepository;
import nl.novi.cardemo.repositories.CarRepository;
import org.springframework.stereotype.Service;

@Service
public class CarRegistrationService {

   private final CarRegistrationRepository carRegistrationRepository;
   private final CarRepository carRepository;

   public CarRegistrationService(CarRegistrationRepository carRegistrationRepository, CarRepository carRepository) {
      this.carRegistrationRepository = carRegistrationRepository;
      this.carRepository = carRepository;
   }

   // Methode om een nieuwe CarRegistration aan te maken
   public CarRegistration createCarRegistration(Long carId, CarRegistration carRegistration) {
      // Controleer of de Car bestaat
      Car car = carRepository.findById(carId)
              .orElseThrow(() -> new RuntimeException("Car not found with id " + carId));
      //zet relatie
      carRegistration.setCar(car);

      // Sla de registratie op
      return carRegistrationRepository.save(carRegistration);
   }

   // Methode om een bestaande CarRegistration bij te werken
   public CarRegistration updateCarRegistration(Long carId, Long registrationId, CarRegistration carRegistrationUpdate) {
      // Haal de bestaande registratie op
      CarRegistration carRegistration = carRegistrationRepository.findById(registrationId)
              .orElseThrow(() -> new RuntimeException("CarRegistration not found with id " + registrationId));

      // Controleer of de Car bestaat
      Car car = carRepository.findById(carId)
              .orElseThrow(() -> new RuntimeException("Car not found with id " + carId));

      // Update de registratie en koppel de juiste Car
      carRegistration.setPlateNumber(carRegistrationUpdate.getPlateNumber());
      carRegistration.setRegistrationDate(carRegistrationUpdate.getRegistrationDate());
      carRegistration.setCar(car);

      // Sla de bijgewerkte registratie op
      return carRegistrationRepository.save(carRegistration);
   }

   // Methode om een CarRegistration op te halen
   public CarRegistration getCarRegistration(Long carId, Long registrationId) {
      return carRegistrationRepository.findByIdAndCarId(registrationId, carId)
              .orElseThrow(() -> new EntityNotFoundException("CarRegistration not found with id " + registrationId));
   }

   // Methode om een CarRegistration te verwijderen
   public boolean deleteCarRegistration(Long carId, Long registrationId) {
      if (carRegistrationRepository.existsById(registrationId)) {
         carRegistrationRepository.deleteById(registrationId);
         return true;
      }
      return false;
   }
}
```
</details>

---

### 5. Aanmaken van de DTO's en Mappers
Maak de benodigde DTO's en mappers voor `CarRegistration`.

<details>
<summary>Hint: Voeg de volgende klassen toe aan `src/main/java/nl/novi/cardemo/dtos/carRegistrations/` en `src/main/java/nl/novi/cardemo/mappers/`</summary>

- CarRegistrationCreateDTO.java
- CarRegistrationResponseDTO.java
- CarRegistrationUpdateDTO.java

```java
public class CarRegistrationCreateDTO {
    private String registrationNumber;
    private Long carId;

    // Getters and setters
}
```

```java
public class CarRegistrationResponseDTO {
    private Long id;
    private String registrationNumber;
    private CarResponseDTO car;

    // Getters and setters
}
```

```java
public class CarRegistrationUpdateDTO {
    private String registrationNumber;

    // Getters and setters
}
```

```java
package nl.novi.cardemo.mappers;
import nl.novi.cardemo.dtos.carRegistrations.CarRegistrationCreateDTO;
import nl.novi.cardemo.dtos.carRegistrations.CarRegistrationResponseDTO;
import nl.novi.cardemo.dtos.carRegistrations.CarRegistrationUpdateDTO;
import nl.novi.cardemo.models.CarRegistration;

import java.util.List;
import java.util.stream.Collectors;

public class CarRegistrationMapper {

   // Zet een CarRegistration entiteit om naar een CarRegistrationResponseDTO
   public static CarRegistrationResponseDTO toResponseDTO(CarRegistration carRegistration) {
      CarRegistrationResponseDTO dto = new CarRegistrationResponseDTO();
      dto.setId(carRegistration.getId());
      dto.setPlateNumber(carRegistration.getPlateNumber());
      dto.setRegistrationDate(carRegistration.getRegistrationDate());
      return dto;
   }

   // Zet een CarRegistrationCreateDTO om naar een CarRegistration entiteit
   public static CarRegistration toEntity(CarRegistrationCreateDTO carRegistrationCreateDTO) {
      CarRegistration carRegistration = new CarRegistration();
      carRegistration.setPlateNumber(carRegistrationCreateDTO.getPlateNumber());
      carRegistration.setRegistrationDate(carRegistrationCreateDTO.getRegistrationDate());
      return carRegistration;
   }

   // Zet een CarRegistrationUpdateDTO om naar een CarRegistration entiteit
   public static CarRegistration toEntity(CarRegistrationUpdateDTO carRegistrationUpdateDTO) {
      CarRegistration carRegistration = new CarRegistration();
      carRegistration.setPlateNumber(carRegistrationUpdateDTO.getPlateNumber());
      carRegistration.setRegistrationDate(carRegistrationUpdateDTO.getRegistrationDate());
      return carRegistration;
   }

   // Zet een lijst van CarRegistration entiteiten om naar een lijst van CarRegistrationResponseDTO's
   public static List<CarRegistrationResponseDTO> toResponseDTOList(List<CarRegistration> carRegistrations) {
      return carRegistrations.stream()
              .map(CarRegistrationMapper::toResponseDTO) // Zet elke CarRegistration om naar een CarRegistrationResponseDTO
              .collect(Collectors.toList());
   }
}
```
</details>

---

### 6. Aanmaken van de Controller
Maak een controller voor `CarRegistration`.

<details>
<summary>Hint: Voeg de volgende klasse toe aan `src/main/java/nl/novi/cardemo/controllers/`</summary>

```java
package nl.novi.cardemo.controllers;

import nl.novi.cardemo.dtos.carRegistrations.CarRegistrationCreateDTO;
import nl.novi.cardemo.dtos.carRegistrations.CarRegistrationResponseDTO;
import nl.novi.cardemo.dtos.carRegistrations.CarRegistrationUpdateDTO;
import nl.novi.cardemo.mappers.CarRegistrationMapper;
import nl.novi.cardemo.services.CarRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cars/{carId}/carregistrations")
public class CarRegistrationController {

   private final CarRegistrationService carRegistrationService;

   public CarRegistrationController(CarRegistrationService carRegistrationService) {
      this.carRegistrationService = carRegistrationService;
   }

   // Endpoint om een nieuwe CarRegistration aan te maken voor een specifieke Car
   @PostMapping
   public ResponseEntity<CarRegistrationResponseDTO> createCarRegistration(
           @PathVariable Long carId,
           @RequestBody CarRegistrationCreateDTO carRegistrationCreateDTO) {

      // Roep de service aan om de registratie aan te maken
      var createdCar = carRegistrationService.createCarRegistration(carId, CarRegistrationMapper.toEntity(carRegistrationCreateDTO));

      return ResponseEntity.status(HttpStatus.CREATED).body(CarRegistrationMapper.toResponseDTO(createdCar));
   }

   // Endpoint om een specifieke CarRegistration op te halen
   @GetMapping("/{registrationId}")
   public ResponseEntity<CarRegistrationResponseDTO> getCarRegistration(
           @PathVariable Long carId, @PathVariable Long registrationId) {
      // Roep de service aan om de registratie op te halen
      return ResponseEntity.ok(CarRegistrationMapper.toResponseDTO(carRegistrationService.getCarRegistration(carId, registrationId)));
   }

   // Endpoint om een CarRegistration bij te werken, gekoppeld aan een specifieke Car
   @PutMapping("/{registrationId}")
   public ResponseEntity<CarRegistrationResponseDTO> updateCarRegistration(
           @PathVariable Long carId, @PathVariable Long registrationId,
           @RequestBody CarRegistrationUpdateDTO carRegistrationUpdateDTO) {

      return ResponseEntity.ok(CarRegistrationMapper.toResponseDTO(carRegistrationService.updateCarRegistration(carId, registrationId, CarRegistrationMapper.toEntity(carRegistrationUpdateDTO))));
   }

   // Endpoint om een CarRegistration te verwijderen
   @DeleteMapping("/{registrationId}")
   public ResponseEntity<Void> deleteCarRegistration(
           @PathVariable Long carId, @PathVariable Long registrationId) {

      // Roep de service aan om de registratie te verwijderen
      boolean isDeleted = carRegistrationService.deleteCarRegistration(carId, registrationId);

      if (isDeleted) {
         return ResponseEntity.noContent().build(); // Succesvol verwijderd
      } else {
         return ResponseEntity.notFound().build(); // Niet gevonden
      }
   }
}
```
</details>

---

### 7. Uitbreidingen om ook andere entiteiten
Breid de applicatie uit om ook mappers voor `Accessory` en `RepairInvoice` te bevatten.
