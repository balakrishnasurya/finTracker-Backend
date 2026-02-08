package com.example.backend.repositories;

import com.example.backend.entities.GroupTransactionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupTransactionParticipantRepository extends JpaRepository<GroupTransactionParticipant, Long> {
}
