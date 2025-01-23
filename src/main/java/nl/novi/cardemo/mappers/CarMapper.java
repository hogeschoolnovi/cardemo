package nl.novi.cardemo.mappers;

import nl.novi.cardemo.dtos.CarInputDTO;
import nl.novi.cardemo.dtos.CarResponseDTO;
import nl.novi.cardemo.models.Car;

import java.util.List;
import java.util.stream.Collectors;

public class CarMapper {
    public static CarResponseDTO toResponseDTO(Car car) {
        var result = new CarResponseDTO();
        result.setBrand(car.getBrand());
        result.setModel(car.getModel());
        result.setYear(car.getYear());
        result.setId(car.getId());
        return result;
    }

    public static Car toEntity(CarInputDTO carCreateDTO) {
        Car car = new Car();
        car.setBrand(carCreateDTO.getBrand());
        car.setModel(carCreateDTO.getModel());
        car.setYear(carCreateDTO.getYear());
        return car;
    }

    public static List<CarResponseDTO> toResponseDTOList(List<Car> cars) {
        return cars.stream().map(CarMapper::toResponseDTO).collect(Collectors.toList());
    }
}
