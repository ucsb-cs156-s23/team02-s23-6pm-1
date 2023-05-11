package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(description = "Restaurants")
@RequestMapping("/api/restaurants")
@RestController
@Slf4j
public class RestaurantsController extends ApiController {

    @Autowired
    RestaurantRepository restaurantRepository;

    @ApiOperation(value = "List all restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allRestaurants() {
        return restaurantRepository.findAll();
    }


    @ApiOperation(value = "Get a single restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurant getById(
            @ApiParam("id") @RequestParam Long id
    ) {

        return restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));
    }

    @ApiOperation(value = "Create a restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postRestaurant(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("address") @RequestParam String address,
            @ApiParam("description") @RequestParam String description) {

        var restaurant = Restaurant.builder()
                .name(name)
                .address(address)
                .description(description)
                .build();

        return restaurantRepository.save(restaurant);
    }

    @ApiOperation(value = "Delete a restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRestaurant(
            @ApiParam("id") @RequestParam Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        restaurantRepository.delete(restaurant);
        return genericMessage("Restaurant with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurant updateRestaurant(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Restaurant incoming) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, id));

        restaurant.setName(incoming.getName());
        restaurant.setAddress(incoming.getAddress());
        restaurant.setDescription(incoming.getDescription());

        restaurantRepository.save(restaurant);

        return restaurant;
    }
}
