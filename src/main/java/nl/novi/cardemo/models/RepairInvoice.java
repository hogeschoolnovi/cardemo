package nl.novi.cardemo.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "repair_invoices")
public class RepairInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // De primaire sleutel van de reparatienota
    private LocalDate repairDate; // De datum waarop de reparatie plaatsvond
    private Double totalCost; // De totale kosten van de reparatie

    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id") // Koppelt de reparatienota aan een auto via de primaire sleutel van de auto
    private Car car; // Verwijzing naar de auto die gerepareerd is

    // Constructor
    public RepairInvoice() {}

    public RepairInvoice(LocalDate repairDate, Double totalCost, Car car) {
        this.repairDate = repairDate;
        this.totalCost = totalCost;
        this.car = car;
    }

    // Getters en Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(LocalDate repairDate) {
        this.repairDate = repairDate;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}