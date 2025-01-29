package nl.novi.cardemo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.util.List;


@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Brand cannot be empty")
    private String brand;

    @NotBlank(message = "Model cannot be empty")
    private String model;

    @NotNull(message = "Year cannot be null")
    @Min(value = 1886, message = "Year must be after 1886")
    @Max(value = 2024, message = "Year must be before or equal to 2024")
    @Column(name = "production_year")
    private Integer year;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL) // Eén auto kan meerdere reparatienota's hebben
    private List<RepairInvoice> repairInvoices;

    @ManyToMany
    @JoinTable(
            name = "car_accessories", // Naam van de join-tabel
            joinColumns = @JoinColumn(name = "car_id"), // Kolom die verwijst naar de Car
            inverseJoinColumns = @JoinColumn(name = "accessory_id") // Kolom die verwijst naar de Accessory
    )
    private List<Accessory> accessories; // Lijst van accessoires die aan deze auto gekoppeld zijn

    public Car(){
        brand = "not set";
        model = "not set";
        year = 1886;
    }
    public Car(String brand, String model, int year) {

        this.brand = brand;
        this.model = model;
        this.year = year;
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}