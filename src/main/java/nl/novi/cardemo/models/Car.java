package nl.novi.cardemo.models;

public class Car {
    private Long id;
    private String brand;
    private String model;

    public Long getId() {
        return id;
    }

    public Car(String brand, String model) {
        this.brand = brand;
        this.model = model;
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