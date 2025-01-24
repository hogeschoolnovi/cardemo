package nl.novi.cardemo.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "accessories")
public class Accessory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // De primaire sleutel van het accessoire
    private String name; // De naam van het accessoire
    private Double price; // De prijs van het accessoire

    @ManyToMany(mappedBy = "accessories") // Omgekeerde kant van de veel-op-veel relatie
    private List<Car> cars;

    // Constructor
    public Accessory() {}

    public Accessory(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    // Getters en Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }
}