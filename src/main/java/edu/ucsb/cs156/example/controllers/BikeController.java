package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.Bike;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.BikeRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(description = "Bike")
@RequestMapping("/api/bikes")
@RestController
@Slf4j
public class BikeController extends ApiController {

    @Autowired
    BikeRepository bikeRepository;

    @ApiOperation(value = "List all bikes")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Bike> allBikes() {
        return bikeRepository.findAll();
    }

    @ApiOperation(value = "Get a single bike")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Bike getById(
            @ApiParam("id") @RequestParam Long id) {

        return bikeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Bike.class, id));
    }

    @ApiOperation(value = "Create a new bike")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Bike postBike(
            @ApiParam("manufacturer") @RequestParam String manufacturer,
            @ApiParam("model") @RequestParam String model,
            @ApiParam("number of gears") @RequestParam int numGears)
            throws JsonProcessingException {

        Bike bike = new Bike();
        bike.setManufacturer(manufacturer);
        bike.setModel(model);
        bike.setNumGears(numGears);

        return bikeRepository.save(bike);
    }

    @ApiOperation(value = "Update a single bike")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Bike bike(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Bike incoming) {

        Bike bike = bikeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Bike.class, id));

        bike.setManufacturer(incoming.getManufacturer());
        bike.setModel(incoming.getModel());
        bike.setNumGears(incoming.getNumGears());

        bikeRepository.save(bike);

        return bike;
    }

    @ApiOperation(value = "Delete a Bike")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteBike(
            @ApiParam("code") @RequestParam Long id) {
        Bike bike = bikeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Bike.class, id));

        bikeRepository.delete(bike);
        return genericMessage("Bike with id %s deleted".formatted(id));
    }
}
