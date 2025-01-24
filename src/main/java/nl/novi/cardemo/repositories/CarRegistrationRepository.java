package nl.novi.cardemo.repositories;

import nl.novi.cardemo.models.CarRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRegistrationRepository extends JpaRepository<CarRegistration, Long> {
    // Je kunt hier eventueel custom queries toevoegen indien nodig
    Optional<CarRegistration> findByIdAndCarId(Long Id, Long carId);
}
