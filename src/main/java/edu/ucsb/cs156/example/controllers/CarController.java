package edu.ucsb.cs156.example.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucsb.cs156.example.entities.Car;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.CarRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description = "cars")
@RequestMapping("/api/cars")
@RestController
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

    @ApiOperation(value = "Get a single car")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Car getById(
            @ApiParam("id") @RequestParam Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Car.class, id));

        return car;
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

    @ApiOperation(value = "Update a single car")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Car updateCar(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Car incoming) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Car.class, id));

        car.setDescription(incoming.getDescription());
        car.setHorsepower(incoming.getHorsepower());
        car.setModel(incoming.getModel());

        carRepository.save(car);

        return car;
    }

    @ApiOperation(value = "Delete a car")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCar(
            @ApiParam("id") @RequestParam Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Car.class, id));

        carRepository.delete(car);
        return genericMessage("Car with id %s deleted".formatted(id));
    }
   
}
