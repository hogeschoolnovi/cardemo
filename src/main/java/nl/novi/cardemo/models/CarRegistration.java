package nl.novi.cardemo.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "car_registrations")
public class CarRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // De primaire sleutel van de registratie
    private String plateNumber; // Het kenteken van de auto
    private LocalDate registrationDate; // De datum waarop de registratie plaatsvond

    @OneToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id") // Koppelt de registratie aan een auto via de primaire sleutel van de auto
    private Car car; // Verwijzing naar de auto die deze registratie heeft

    // Constructor
    public CarRegistration() {}

    public CarRegistration(String plateNumber, LocalDate registrationDate, Car car) {
        this.plateNumber = plateNumber;
        this.registrationDate = registrationDate;
        this.car = car;
    }

    // Getters en Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}