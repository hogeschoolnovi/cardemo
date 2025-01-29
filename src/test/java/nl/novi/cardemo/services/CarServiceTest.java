package nl.novi.cardemo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import nl.novi.cardemo.models.Car;
import nl.novi.cardemo.repositories.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

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

    @Test
    void testGetCarsByBrandAndModel() {
        when(carRepository.findByBrandAndModel("Toyota", "Corolla")).thenReturn(mockCars.subList(0, 2));

        List<Car> result = carService.getCars("Toyota", "Corolla");

        assertEquals(2, result.size());
        assertEquals("Toyota", result.get(0).getBrand());
        assertEquals("Corolla", result.get(0).getModel());
    }

    @Test
    void testGetCarsByBrandOnly() {
        when(carRepository.findByBrand("Toyota")).thenReturn(mockCars.subList(0, 2));

        List<Car> result = carService.getCars("Toyota", null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(car -> car.getBrand().equals("Toyota")));
    }

    @Test
    void testGetCarsByModelOnly() {
        when(carRepository.findByModel("Fiesta")).thenReturn(List.of(mockCars.get(2)));

        List<Car> result = carService.getCars(null, "Fiesta");

        assertEquals(1, result.size());
        assertEquals("Fiesta", result.get(0).getModel());
    }

    @Test
    void testGetAllCars() {
        when(carRepository.findAll()).thenReturn(mockCars);

        List<Car> result = carService.getCars(null, null);

        assertEquals(4, result.size());
    }

    @Test
    void testFindCarById() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(mockCars.get(0)));

        Optional<Car> car = carService.findById(1L);

        assertTrue(car.isPresent());
        assertEquals("Toyota", car.get().getBrand());
        assertEquals("Corolla", car.get().getModel());
    }

    @Test
    void testDeleteCarExists() {
        when(carRepository.existsById(1L)).thenReturn(true);
        doNothing().when(carRepository).deleteById(1L);

        boolean result = carService.delete(1L);

        assertTrue(result);
        verify(carRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCarNotExists() {
        when(carRepository.existsById(99L)).thenReturn(false);

        boolean result = carService.delete(99L);

        assertFalse(result);
        verify(carRepository, never()).deleteById(99L);
    }
}

