package com.example.backend.repositories;

import com.example.backend.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    List<Group> findByIsActiveTrue();
    
    Optional<Group> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id = :id AND g.isActive = true")
    Optional<Group> findByIdWithMembers(Long id);
}
