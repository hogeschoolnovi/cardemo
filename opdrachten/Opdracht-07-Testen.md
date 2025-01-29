## Stap 1: Projectvoorbereiding

1. Clone of open het bestaande project.
2. Maak een resources-map aan in `src/test/resources/` voor testconfiguraties.
3. Kopieer `application.properties` van `src/main/resources/` naar `src/test/resources/`.

### Toevoegen van een in-memory database

Om een in-memory database te gebruiken voor de tests, voeg de volgende configuratie toe aan `src/test/resources/application.properties`:

<details>
<summary>Hints</summary>  

````properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=false
spring.jpa.properties.hibernate.format_sql=true
````  

</details>  

Deze configuratie stelt de applicatie in staat om een H2 in-memory database te gebruiken. Hiermee worden testgegevens alleen tijdens de testfase bewaard en automatisch verwijderd na afloop van de tests.

Daarnaast moet de H2-database afhankelijkheid worden toegevoegd aan de `pom.xml` van het project:

<details>
<summary>Hints</summary>  

````xml
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
````  

</details>  

### Wat doet de `scope` in de dependency?

De `scope` in Maven bepaalt in welke fase en context een dependency beschikbaar is.

- `compile` (standaardwaarde): De dependency is beschikbaar tijdens de compilatie en runtime.
- `provided`: De dependency is nodig voor compilatie maar wordt niet meegeleverd bij de uiteindelijke build.
- `runtime`: De dependency is niet nodig voor compilatie, maar wel bij runtime.
- `test`: De dependency is alleen nodig tijdens het uitvoeren van tests.

Omdat de H2-database alleen tijdens de tests gebruikt wordt, is de scope hier ingesteld op `test`. Dit voorkomt dat de H2-dependency wordt meegenomen in de productie-build.

---

## Stap 2: Aanmaken van een test

Ga naar de `CarService` en genereer een test voor de `getCars`-methode.

De testklasse wordt toegevoegd in de package:  
`nl.novi.cardemo.services` (in de test-branch).

De methode `getCars` heeft verschillende uitvoeringen, afhankelijk van de parameters die worden meegegeven:

````java
  public List<Car> getCars(
             String brand,
             String model)
    {
        List<Car> cars;

        if (brand != null && model != null) {
            cars = carRepository.findByBrandAndModel(brand, model);
        } else if (brand != null) {
            cars = carRepository.findByBrand(brand);
        } else if (model != null) {
            cars = carRepository.findByModel(model);
        } else {
            cars = carRepository.findAll();
        }
        return cars;
    }
````  

Om deze methode goed te testen, beginnen we met het opzetten van de testklasse.

---

### Mocking van de repository

Om te voorkomen dat de unit test afhankelijk wordt van een daadwerkelijke database en de gegevens daarin, moet de repository worden gemockt. Dit doen we met Mockito:

````java
@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;
    
   // rest van de class
}
````  

### Wat doet `@ExtendWith(MockitoExtension.class)` en `@Mock`?

- `@ExtendWith(MockitoExtension.class)`  
  Dit is een JUnit 5-extensie die ervoor zorgt dat Mockito-functionaliteiten, zoals `@Mock` en `@InjectMocks`, correct worden geïnitialiseerd voordat de tests worden uitgevoerd.

- `@Mock`  
  Deze annotatie geeft aan dat de `CarRepository` moet worden gemockt. Dit betekent dat in plaats van de echte repository een gesimuleerde versie wordt gebruikt, zodat we kunnen controleren hoe de `CarService` ermee omgaat zonder afhankelijk te zijn van een echte database.

---

### Dependency Injection van de service

Om een instantie van `CarService` te krijgen die de gemockte repository gebruikt, wordt `@InjectMocks` gebruikt:

````java
    @InjectMocks
    private CarService carService;
````  

Deze annotatie zorgt ervoor dat de `carService` automatisch de gemockte `carRepository` krijgt geïnjecteerd.

---

### Voorbereiden van testdata

Om de tests uit te voeren, wordt een lijst met testdata aangemaakt. Deze lijst simuleert de data die normaal uit de database zou komen.

De testdata moet voor elke test opnieuw opgebouwd worden, zodat de tests onafhankelijk van elkaar blijven. Dit wordt gedaan met `@BeforeEach`:

````java
 private List<Car> mockCars;

    @BeforeEach
    void setUp() {
        mockCars = Arrays.asList(
                new Car( "Toyota", "Corolla", 2015),
                new Car( "Toyota", "Corolla", 2018),
                new Car( "Ford", "Fiesta", 2020),
                new Car( "Ford", "Focus", 2012)
        );
    }
````  

Met deze setup is er een lijst met auto’s beschikbaar die gebruikt kan worden in de tests.

---

### Schrijven van de test

De `getCars`-methode moet worden getest op verschillende scenario’s. Bijvoorbeeld, als er gefilterd wordt op merk, moet de repository worden aangesproken met de methode `.findByBrand()`.

Met Mockito kunnen we aangeven welke respons de repository moet teruggeven:

````java
 @Test
    void testGetCarsByBrandOnly() {
        when(carRepository.findByBrand("Toyota")).thenReturn(mockCars.subList(0, 2));

        List<Car> result = carService.getCars("Toyota", null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(car -> car.getBrand().equals("Toyota")));
    }
````  

### Toelichting

1. Mocking van de repository:
    - `when(carRepository.findByBrand("Toyota"))`
    - Dit zegt dat als de `carRepository.findByBrand("Toyota")` methode wordt aangeroepen, deze een lijst met twee auto’s moet teruggeven (de eerste twee uit de `mockCars` lijst).

2. Aanroepen van de service-methode:
    - `List<Car> result = carService.getCars("Toyota", null);`
    - Dit roept de `getCars`-methode van `CarService` aan, waarbij alleen `brand` is meegegeven en `model` `null` is.

3. Controleren van het resultaat:
    - `assertEquals(2, result.size());`
    - Controleert of de lijst daadwerkelijk 2 auto’s bevat.
    - `assertTrue(result.stream().allMatch(car -> car.getBrand().equals("Toyota")));`
    - Controleert of alle auto’s in de lijst het merk `"Toyota"` hebben.

---

Met deze test is gecontroleerd of de `getCars`-methode correct werkt als er alleen op merk wordt gefilterd. Soortgelijke tests kunnen worden geschreven voor filtering op model of beide parameters samen.

## Stap 3: Uitbreiding van Unit Tests

Nu de basisunit tests zijn opgezet, gaan we de testdekking verder uitbreiden door extra scenario’s te testen. Hieronder volgen extra tests die aansluiten bij de bestaande testcases.

### 1. Test het ophalen van auto’s op basis van merk én model

Deze test controleert of de `getCars`-methode correct werkt wanneer zowel merk als model worden opgegeven. De repository moet dan `findByBrandAndModel()` aanroepen.

- Assertions:
    - Controleert of de geretourneerde lijst 2 auto’s bevat.
    - Controleert of het eerste resultaat het juiste merk en model heeft.

<details>
<summary>Uitgewerkte test</summary>  

````java
@Test
void testGetCarsByBrandAndModel() {
    when(carRepository.findByBrandAndModel("Toyota", "Corolla")).thenReturn(mockCars.subList(0, 2));

    List<Car> result = carService.getCars("Toyota", "Corolla");

    assertEquals(2, result.size());
    assertEquals("Toyota", result.get(0).getBrand());
    assertEquals("Corolla", result.get(0).getModel());
}
````  

</details>  

---

### 2. Test het ophalen van auto’s op basis van alleen model

Deze test valideert of de service correct werkt als er enkel een model wordt opgegeven. De repository moet dan `findByModel()` aanroepen.

- Assertions:
    - Controleert of slechts 1 auto wordt teruggegeven.
    - Controleert of het model van de auto overeenkomt met de verwachte waarde.

<details>
<summary>Uitgewerkte test</summary>  

````java
@Test
void testGetCarsByModelOnly() {
    when(carRepository.findByModel("Fiesta")).thenReturn(List.of(mockCars.get(2)));

    List<Car> result = carService.getCars(null, "Fiesta");

    assertEquals(1, result.size());
    assertEquals("Fiesta", result.get(0).getModel());
}
````  

</details>  

---

### 3. Test het ophalen van alle auto’s

Deze test controleert of de `getCars`-methode zonder parameters de volledige lijst retourneert.

- Assertions:
    - Controleert of de volledige lijst wordt teruggegeven (4 auto’s in dit geval).

<details>
<summary>Uitgewerkte test</summary>  

````java
@Test
void testGetAllCars() {
    when(carRepository.findAll()).thenReturn(mockCars);

    List<Car> result = carService.getCars(null, null);

    assertEquals(4, result.size());
}
````  

</details>  

---

### 4. Test het zoeken van een auto op ID

Deze test controleert of een specifieke auto correct wordt opgehaald wanneer een ID wordt meegegeven.

- Assertions:
    - Controleert of de auto aanwezig is (`isPresent()`).
    - Controleert of de juiste auto wordt teruggegeven op basis van merk en model.

<details>
<summary>Uitgewerkte test</summary>  

````java
@Test
void testFindCarById() {
    when(carRepository.findById(1L)).thenReturn(Optional.of(mockCars.get(0)));

    Optional<Car> car = carService.findById(1L);

    assertTrue(car.isPresent());
    assertEquals("Toyota", car.get().getBrand());
    assertEquals("Corolla", car.get().getModel());
}
````  

</details>  

---

### 5. Test het verwijderen van een auto die bestaat

Deze test controleert of een auto correct wordt verwijderd als deze bestaat.

- Assertions:
    - Controleert of de `delete()`-methode `true` retourneert.
    - Controleert of `deleteById()` exact één keer is aangeroepen via `verify()`.

<details>
<summary>Uitgewerkte test</summary>  

````java
@Test
void testDeleteCarExists() {
    when(carRepository.existsById(1L)).thenReturn(true);
    doNothing().when(carRepository).deleteById(1L);

    boolean result = carService.delete(1L);

    assertTrue(result);
    verify(carRepository, times(1)).deleteById(1L);
}
````  

</details>  

---

### 6. Test het verwijderen van een auto die niet bestaat

Deze test valideert dat de service correct omgaat met het verwijderen van een niet-bestaande auto.

- Assertions:
    - Controleert of de `delete()`-methode `false` retourneert als de auto niet bestaat.
    - Controleert dat `deleteById()` nooit is aangeroepen (`verify(carRepository, never())`).

<details>
<summary>Uitgewerkte test</summary>  

````java
@Test
void testDeleteCarNotExists() {
    when(carRepository.existsById(99L)).thenReturn(false);

    boolean result = carService.delete(99L);

    assertFalse(result);
    verify(carRepository, never()).deleteById(99L);
}
````  

</details>  

---

### Toelichting op `assertions` en `verify`

#### Assertions (`assertEquals`, `assertTrue`, `assertFalse`)

- `assertEquals(expected, actual)`: Controleert of de verwachte waarde gelijk is aan de daadwerkelijke waarde.
- `assertTrue(condition)`: Controleert of een bepaalde voorwaarde waar is.
- `assertFalse(condition)`: Controleert of een bepaalde voorwaarde onwaar is.

#### Mockito `verify()`

- `verify(carRepository, times(1)).deleteById(1L)`: Controleert of `deleteById(1L)` exact één keer is aangeroepen.
- `verify(carRepository, never()).deleteById(99L)`: Controleert of `deleteById(99L)` nooit is aangeroepen.

Hier is de uitgewerkte versie van jouw instructies, inclusief uitleg van de annotaties en `JsonPath`.

---

## Stap 4: Integratietesten voor CarController

### Opzetten van de integratietestklasse

1. Maak een package aan onder `nl.novi.cardemo` in de test-branch met de naam `integration`.
2. Maak een klasse aan met de naam `CarControllerIntegrationTest`.

#### Basisimplementatie van de testklasse

```java
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class CarControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll(); // Zorgt ervoor dat de database leeg is

        // Voeg testdata toe
        List<Car> testCars = List.of(
                new Car("Toyota", "Corolla", 2018),
                new Car("Toyota", "Yaris", 2020),
                new Car("Ford", "Fiesta", 2019)
        );
        carRepository.saveAll(testCars);
    }
}
```

---

### Uitleg van de annotaties

- `@SpringBootTest`
    - Start een volledige Spring Boot-context voor de test.
    - Zorgt ervoor dat alle beans (zoals services, repositories en controllers) beschikbaar zijn.

- `@AutoConfigureMockMvc`
    - Configureert MockMvc, waarmee HTTP-verzoeken kunnen worden gesimuleerd zonder daadwerkelijk een webserver te starten.

- `@WithMockUser(username = "testuser", roles = {"USER"})`
    - Simuleert een ingelogde gebruiker met de rol `"USER"`.
    - Dit is nodig voor controllers die beveiligd zijn met Spring Security.

- `@Autowired`
    - Injecteert de benodigde componenten (`MockMvc` en `CarRepository`) in de testklasse.

- `@BeforeEach`
    - Wordt voor elke test uitgevoerd om de database te resetten en testdata in te voegen.

---

### Toevoegen van de benodigde dependency

Je merkt dat `@WithMockUser` niet wordt herkend. Dit komt omdat de Spring Security test dependency nog niet is toegevoegd aan `pom.xml`.

Voeg de volgende dependency toe:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

### Schrijven van een integratietest

We voegen nu een integratietest toe om te controleren of de GET `/cars` endpoint correct functioneert.

#### Test voor het ophalen van alle auto's

```java
@Test
void testGetAllCars() throws Exception {
    mockMvc.perform(get("/cars")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3))) // Controleert of alle 3 auto's worden opgehaald
            .andExpect(jsonPath("$[0].brand").value("Toyota"))
            .andExpect(jsonPath("$[1].brand").value("Toyota"))
            .andExpect(jsonPath("$[2].brand").value("Ford"));
}
```



#### Uitleg over `JsonPath` in de test

`JsonPath` wordt gebruikt om specifieke velden in de JSON-response van een API te valideren.

- `jsonPath("$", hasSize(3))`
    - Controleert of de JSON-array 3 elementen bevat (oftewel, of er 3 auto’s in de response zitten).

- `jsonPath("$[0].brand").value("Toyota")`
    - Controleert of de eerste auto in de lijst het merk `"Toyota"` heeft.

- `jsonPath("$[1].brand").value("Toyota")`
    - Controleert of de tweede auto in de lijst ook `"Toyota"` is.

- `jsonPath("$[2].brand").value("Ford")`
    - Controleert of de derde auto `"Ford"` is.


## Stap 5: Uitbreiding van integratietests voor CarController

Hier breiden we de integratietests verder uit om meer scenario’s te dekken. Deze tests controleren of de `CarController` correct werkt bij verschillende queryparameters.

---

### 1. Test het ophalen van auto's op basis van merk

Deze test controleert of de `GET /cars?brand=Toyota`-aanroep alleen auto's van het merk Toyota retourneert.

- Wat wordt getest?
    - De response bevat exact 2 auto's.
    - Beide auto's hebben het merk `"Toyota"`.

<details>
<summary>Uitgewerkte test</summary>  

```java
@Test
void testGetCarsByBrand() throws Exception {
    mockMvc.perform(get("/cars?brand=Toyota")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2))) // Alleen Toyota's moeten worden opgehaald
            .andExpect(jsonPath("$[0].brand").value("Toyota"))
            .andExpect(jsonPath("$[1].brand").value("Toyota"));
}
```
</details>  

---

### 2. Test het ophalen van auto's op basis van model

Deze test controleert of de `GET /cars?model=Fiesta`-aanroep alleen auto's met het model `"Fiesta"` retourneert.

- Wat wordt getest?
    - De response bevat exact 1 auto.
    - Het model van deze auto is `"Fiesta"`.

<details>
<summary>Uitgewerkte test</summary>  

```java
@Test
void testGetCarsByModel() throws Exception {
    mockMvc.perform(get("/cars?model=Fiesta")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1))) // Alleen de Ford Fiesta moet worden opgehaald
            .andExpect(jsonPath("$[0].model").value("Fiesta"));
}
```
</details>  

---

### 3. Test het ophalen van auto's op basis van merk én model

Deze test controleert of de `GET /cars?brand=Toyota&model=Corolla`-aanroep alleen de auto `"Toyota Corolla"` retourneert.

- Wat wordt getest?
    - De response bevat exact 1 auto.
    - Het merk is `"Toyota"` en het model is `"Corolla"`.

<details>
<summary>Uitgewerkte test</summary>  

```java
@Test
void testGetCarsByBrandAndModel() throws Exception {
    mockMvc.perform(get("/cars?brand=Toyota&model=Corolla")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1))) // Alleen Toyota Corolla moet worden opgehaald
            .andExpect(jsonPath("$[0].brand").value("Toyota"))
            .andExpect(jsonPath("$[0].model").value("Corolla"));
}
```
</details>  

---

### 4. Test wanneer er geen resultaten worden gevonden

Deze test controleert of de `GET /cars?brand=Honda`-aanroep correct omgaat met een merk dat niet in de testdata voorkomt.

- Wat wordt getest?
    - De response bevat geen auto's (lege array).

<details>
<summary>Uitgewerkte test</summary>  

```java
@Test
void testGetCarsNoResults() throws Exception {
    mockMvc.perform(get("/cars?brand=Honda")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0))); // Geen Honda's in testdata
}
```
</details>