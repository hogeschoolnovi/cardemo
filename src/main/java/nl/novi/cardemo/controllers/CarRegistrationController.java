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