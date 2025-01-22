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