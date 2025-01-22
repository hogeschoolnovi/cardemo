# **Bouw je eigen `CarController`**

In deze opdracht ga je stap voor stap een `CarController` bouwen in **Spring Boot**. Je leert hoe je een **REST API** maakt met CRUD-functionaliteit (**Create, Read, Update, Delete**) voor auto’s.

**Uitdaging**: Probeer elke stap zelf te implementeren voordat je de uitwerking bekijkt. Gebruik de hints als je vastzit.

---

## **Stap 1: Basis setup**
1. Maak een nieuw **Spring Boot-project** in IntelliJ met de **spring-web dependency**.
2. Maak een package genaamd **models** voor de **Car**-klasse.
3. Maak een package genaamd **controllers** voor de **CarController**.

### **Maak een `Car` modelklasse**
Maak een nieuwe klasse `Car` met de volgende velden:
- `id` (Long)
- `brand` (String)
- `model` (String)

**Hint:** Voeg getters en setters toe.

<details>
<summary>Uitwerking</summary>

```java
public class Car {
    private Long id;
    private String brand;
    private String model;

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
}
```
</details>

---

## **Stap 2: De Controller opzetten**
Maak een nieuwe klasse **`CarController`** en voeg de juiste annotaties toe om het een REST-controller te maken.

**Hint:**
- Gebruik `@RestController`
- Gebruik `@RequestMapping("/cars")`
- Maak een **lijst (`List<Car>`)** om auto’s in op te slaan.

<details>
<summary>Uitwerking</summary>

```java
@RestController
@RequestMapping("/cars")
public class CarController {
    private List<Car> carList = new ArrayList<>();
    private Long currentId = 1L;
}
```
</details>

---

## **Stap 3: Create (Auto toevoegen)**
Voeg een **POST**-methode toe om een auto aan de lijst toe te voegen.

**Hint:**
- Gebruik `@PostMapping`
- Gebruik `@RequestBody` om een auto uit de request op te halen.
- Geef de auto een ID (`currentId++`).
- Retourneer een `ResponseEntity` met de nieuwe auto.

Schrijf de methode en test deze met Postman of een andere API-tool.

<details>
<summary>Uitwerking</summary>

```java
@PostMapping
public ResponseEntity<Car> createCar(@RequestBody Car car) {
    car.setId(currentId++);
    carList.add(car);
    return ResponseEntity.status(HttpStatus.CREATED).body(car);
}
```
</details>

---

## **Stap 4: Read (Eén auto ophalen)**
Voeg een **GET**-methode toe om een auto op te halen op basis van ID.

**Hint:**
- Gebruik `@GetMapping("/{id}")`
- Gebruik `@PathVariable` om de ID uit de URL te halen.
- Zoek de auto op in de lijst.
- Retourneer een `ResponseEntity` met de auto, of een `404 Not Found` als de auto niet bestaat.

<details>
<summary>Uitwerking</summary>

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
</details>

**Helper methode:**  
Voeg een methode toe om een auto te zoeken in de lijst.

<details>
<summary>Uitwerking</summary>

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
</details>

---

## **Stap 5: Read (Alle auto’s ophalen)**
Voeg een **GET**-methode toe om alle auto’s op te halen. Maak het mogelijk om te filteren op merk.

**Hint:**
- Gebruik `@GetMapping`
- Gebruik `@RequestParam(name = "brand", required = false)`
- Controleer of `brand` is meegegeven en filter de lijst.
- Retourneer een `ResponseEntity` met de resultaten.

<details>
<summary>Uitwerking</summary>

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
</details>

**Helper methode voor filtering:**

<details>
<summary>Uitwerking</summary>

```java
private List<Car> getFilteredCars(String brand) {
    List<Car> filteredCars = new ArrayList<>();
    for (Car car : carList) {
        if (car.getBrand().equalsIgnoreCase(brand)) {
            filteredCars.add(car);
        }
    }
    return filteredCars;
}
```
</details>

---

## **Stap 6: Update (Auto bijwerken)**
Voeg een **PUT**-methode toe om een bestaande auto bij te werken.

**Hint:**
- Gebruik `@PutMapping("/{id}")`
- Zoek de auto op in de lijst.
- Werk de velden `brand` en `model` bij.
- Retourneer een `ResponseEntity` met de geüpdatete auto.

<details>
<summary>Uitwerking</summary>

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
</details>

---

## **Stap 7: Delete (Auto verwijderen)**
Voeg een **DELETE**-methode toe om een auto uit de lijst te verwijderen.

**Hint:**
- Gebruik `@DeleteMapping("/{id}")`
- Zoek de auto op in de lijst.
- Verwijder de auto en retourneer een `204 No Content`.

<details>
<summary>Uitwerking</summary>

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
</details>

---

## **Stap 8: Testen**
- Test alle endpoints met **Postman** of een andere API-tool.
- Controleer of de CRUD-functionaliteit correct werkt.

Als alles goed werkt, heb je nu een volledig functionele **`CarController`** in **Spring Boot** gebouwd!