package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Bike;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BikeRepository extends CrudRepository<Bike, Long> {
}