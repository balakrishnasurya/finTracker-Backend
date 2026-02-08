package com.example.backend.repositories;

import com.example.backend.entities.GroupTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupTransactionRepository extends JpaRepository<GroupTransaction, Long> {
    
    List<GroupTransaction> findByGroupIdOrderByTransactionDateDesc(Long groupId);
    
    @Query("SELECT gt FROM GroupTransaction gt " +
           "LEFT JOIN FETCH gt.paidBy " +
           "LEFT JOIN FETCH gt.participants p " +
           "LEFT JOIN FETCH p.member " +
           "WHERE gt.group.id = :groupId " +
           "ORDER BY gt.transactionDate DESC")
    List<GroupTransaction> findByGroupIdWithDetails(Long groupId);
}
