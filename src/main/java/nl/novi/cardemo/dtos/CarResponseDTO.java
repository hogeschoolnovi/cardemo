package nl.novi.cardemo.dtos;

import java.time.Year;

public class CarResponseDTO {
    private Long id;
    private String brand;
    private String model;
    private int year;
    private int age;

    public int getAge() {
        return  Year.now().getValue() -year;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
