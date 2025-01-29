package nl.novi.cardemo.repositories;

import nl.novi.cardemo.models.Car;
import nl.novi.cardemo.models.CarRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByBrand(String brand);
    List<Car> findByBrandAndModel(String brand, String model);
    List<Car> findByModel(String model);
}




