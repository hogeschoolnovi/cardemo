package nl.novi.cardemo.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brand;
    private String model;
    private int year;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL) // Eén auto kan meerdere reparatienota's hebben
    private List<RepairInvoice> repairInvoices;

    @ManyToMany
    @JoinTable(
            name = "car_accessories", // Naam van de join-tabel
            joinColumns = @JoinColumn(name = "car_id"), // Kolom die verwijst naar de Car
            inverseJoinColumns = @JoinColumn(name = "accessory_id") // Kolom die verwijst naar de Accessory
    )
    private List<Accessory> accessories; // Lijst van accessoires die aan deze auto gekoppeld zijn

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