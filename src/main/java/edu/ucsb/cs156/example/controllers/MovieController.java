package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MovieRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(description = "Movie")
@RequestMapping("/api/movies")
@RestController
@Slf4j
public class MovieController extends ApiController {

    @Autowired
    MovieRepository movieRepository;

    @ApiOperation(value = "List all movies")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Movie> allMovies() {
        return movieRepository.findAll();
    }

    @ApiOperation(value = "Get a single movie")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Movie getById(
            @ApiParam("id") @RequestParam Long id) {

        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));
    }

    @ApiOperation(value = "Create a new movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Movie postMovie(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("synopsis") @RequestParam String synopsis,
            @ApiParam("castMembers") @RequestParam int castMembers)
            throws JsonProcessingException {

        Movie movie = new Movie();
        movie.setName(name);
        movie.setSynopsis(synopsis);
        movie.setCastMambers(castMembers);

        return movieRepository.save(movie);
    }

    @ApiOperation(value = "Update a single movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Movie movie(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Movie incoming) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));

        movie.setName(incoming.getName());
        movie.setSynopsis(incoming.getSynopsis());
        movie.setCastMambers(incoming.getCastMemebers());

        movieRepository.save(movie);

        return movie;
    }

    @ApiOperation(value = "Delete a Movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMovie(
            @ApiParam("code") @RequestParam Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));

        movieRepository.delete(movie);
        return genericMessage("Movie with id %s deleted".formatted(id));
    }
}