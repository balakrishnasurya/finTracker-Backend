package com.example.backend.repositories;


import com.example.backend.entities.GymRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRecordRepositories extends JpaRepository<GymRecord,Long> {


}
