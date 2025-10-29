package com.barrisense.backend.complaint.repository;


import com.barrisense.backend.complaint.entity.Neighborhood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long> {
}
