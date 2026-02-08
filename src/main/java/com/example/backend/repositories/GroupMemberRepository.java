package com.example.backend.repositories;

import com.example.backend.entities.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    List<GroupMember> findByGroupIdAndIsActiveTrue(Long groupId);
    
    Optional<GroupMember> findByIdAndIsActiveTrue(Long id);
    
    Optional<GroupMember> findByIdAndGroupIdAndIsActiveTrue(Long id, Long groupId);
}
