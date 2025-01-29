package nl.novi.cardemo.integration;

import nl.novi.cardemo.models.Car;
import nl.novi.cardemo.repositories.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                new Car( "Toyota", "Corolla", 2018),
                new Car( "Toyota", "Yaris", 2020),
                new Car( "Ford", "Fiesta", 2019)
        );
        carRepository.saveAll(testCars);
    }

    @Test

    void testGetAllCars() throws Exception {
        mockMvc.perform(get("/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // Controleer of alle 3 de auto's worden opgehaald
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[1].brand").value("Toyota"))
                .andExpect(jsonPath("$[2].brand").value("Ford"));
    }

    @Test
    void testGetCarsByBrand() throws Exception {
        mockMvc.perform(get("/cars?brand=Toyota")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Alleen Toyota's moeten worden opgehaald
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[1].brand").value("Toyota"));
    }

    @Test
    void testGetCarsByModel() throws Exception {
        mockMvc.perform(get("/cars?model=Fiesta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Alleen de Ford Fiesta moet worden opgehaald
                .andExpect(jsonPath("$[0].model").value("Fiesta"));
    }

    @Test
    void testGetCarsByBrandAndModel() throws Exception {
        mockMvc.perform(get("/cars?brand=Toyota&model=Corolla")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Alleen Toyota Corolla moet worden opgehaald
                .andExpect(jsonPath("$[0].brand").value("Toyota"))
                .andExpect(jsonPath("$[0].model").value("Corolla"));
    }

    @Test
    void testGetCarsNoResults() throws Exception {
        mockMvc.perform(get("/cars?brand=Honda")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Geen Honda's in testdata
    }
}
