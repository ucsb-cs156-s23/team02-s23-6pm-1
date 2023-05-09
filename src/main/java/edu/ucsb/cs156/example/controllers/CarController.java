package edu.ucsb.cs156.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.Car;
import edu.ucsb.cs156.example.repositories.CarRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Api(description = "cars")
@RequestMapping("/api/cars")
@RestController
@Slf4j
public class CarController extends ApiController {

    @Autowired
    CarRepository carRepository;

    @ApiOperation(value = "List all cars")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Car> allCars() {
        Iterable<Car> cars = carRepository.findAll();
        return cars;
    }

    @ApiOperation(value = "Create a new car")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Car postCar(
        @ApiParam("description") @RequestParam String description,
        @ApiParam("horsepower") @RequestParam String horsepower,
        @ApiParam("model") @RequestParam String model)
        throws JsonProcessingException {
            Car car = new Car();
            car.setDescription(description);
            car.setHorsepower(horsepower);
            car.setModel(model);

            Car savedCar = carRepository.save(car);
            
            return savedCar;
    }
   
}
