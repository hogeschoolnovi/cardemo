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