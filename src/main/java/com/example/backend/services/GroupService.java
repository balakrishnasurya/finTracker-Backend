package com.example.backend.services;

import com.example.backend.dtos.*;
import com.example.backend.entities.Group;
import com.example.backend.entities.GroupMember;
import com.example.backend.entities.GroupTransaction;
import com.example.backend.entities.GroupTransactionParticipant;
import com.example.backend.exceptions.ResourceNotFoundException;
import com.example.backend.repositories.GroupMemberRepository;
import com.example.backend.repositories.GroupRepository;
import com.example.backend.repositories.GroupTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupTransactionRepository groupTransactionRepository;
    private final SettlementService settlementService;

    /**
     * Create a new group with members
     */
    @Transactional
    public GroupDetailResponse createGroup(CreateGroupRequest request) {
        // Create group
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setIsActive(true);
        
        // Save group first to get ID
        group = groupRepository.save(group);
        
        // Create members
        List<GroupMember> members = new ArrayList<>();
        for (GroupMemberDto memberDto : request.getMembers()) {
            if (memberDto.getName() == null || memberDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Member name cannot be empty");
            }
            
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUserId(memberDto.getUserId());
            member.setMemberName(memberDto.getName().trim());
            member.setIsActive(true);
            members.add(member);
        }
        
        members = groupMemberRepository.saveAll(members);
        group.setMembers(members);
        
        return mapToGroupDetailResponse(group);
    }

    /**
     * Get all active groups
     */
    public List<GroupListResponse> getAllGroups() {
        List<Group> groups = groupRepository.findByIsActiveTrue();
        
        return groups.stream()
            .map(group -> new GroupListResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                (int) group.getMembers().stream().filter(GroupMember::getIsActive).count(),
                group.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Get group details with members
     */
    public GroupDetailResponse getGroupById(Long groupId) {
        Group group = groupRepository.findByIdWithMembers(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        return mapToGroupDetailResponse(group);
    }

    /**
     * Add a transaction to a group
     */
    @Transactional
    public GroupTransactionDto addTransaction(Long groupId, CreateGroupTransactionRequest request) {
        // Validate group exists
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        // Validate payer is a member of this group
        GroupMember payer = groupMemberRepository.findByIdAndGroupIdAndIsActiveTrue(
            request.getPaidByMemberId(), groupId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Payer with id " + request.getPaidByMemberId() + " is not a member of this group"));
        
        // Validate all participants are members of this group
        List<GroupMember> participants = new ArrayList<>();
        for (Long memberId : request.getIncludedMemberIds()) {
            GroupMember member = groupMemberRepository.findByIdAndGroupIdAndIsActiveTrue(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Member with id " + memberId + " is not a member of this group"));
            participants.add(member);
        }
        
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required");
        }
        
        // Create transaction
        GroupTransaction transaction = new GroupTransaction();
        transaction.setGroup(group);
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setPaidBy(payer);
        
        transaction = groupTransactionRepository.save(transaction);
        
        // Calculate equal share per participant
        BigDecimal sharePerPerson = request.getAmount().divide(
            BigDecimal.valueOf(participants.size()), 
            2, 
            RoundingMode.HALF_UP
        );
        
        // Create participant entries
        List<GroupTransactionParticipant> transactionParticipants = new ArrayList<>();
        for (GroupMember member : participants) {
            GroupTransactionParticipant participant = new GroupTransactionParticipant();
            participant.setTransaction(transaction);
            participant.setMember(member);
            participant.setShareAmount(sharePerPerson);
            transactionParticipants.add(participant);
        }
        
        transaction.setParticipants(transactionParticipants);
        transaction = groupTransactionRepository.save(transaction);
        
        return mapToGroupTransactionDto(transaction);
    }

    /**
     * Get all transactions for a group
     */
    public List<GroupTransactionDto> getGroupTransactions(Long groupId) {
        // Validate group exists
        groupRepository.findByIdAndIsActiveTrue(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        List<GroupTransaction> transactions = groupTransactionRepository.findByGroupIdWithDetails(groupId);
        
        return transactions.stream()
            .map(this::mapToGroupTransactionDto)
            .collect(Collectors.toList());
    }

    /**
     * Calculate settlement for a group
     */
    public List<SettlementDto> calculateGroupSettlement(Long groupId) {
        // Validate group exists
        Group group = groupRepository.findByIdWithMembers(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        // Get all active members
        List<GroupMember> members = group.getMembers().stream()
            .filter(GroupMember::getIsActive)
            .collect(Collectors.toList());
        
        if (members.isEmpty()) {
            throw new IllegalArgumentException("Group has no active members");
        }
        
        // Get all transactions
        List<GroupTransaction> transactions = groupTransactionRepository.findByGroupIdWithDetails(groupId);
        
        if (transactions.isEmpty()) {
            return new ArrayList<>(); // No transactions, no settlements needed
        }
        
        // Calculate settlement
        return settlementService.calculateSettlementFromTransactions(members, transactions);
    }

    /**
     * Get member balances for a group
     */
    public List<MemberBalanceDto> getGroupBalances(Long groupId) {
        // Validate group exists
        Group group = groupRepository.findByIdWithMembers(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        // Get all active members
        List<GroupMember> members = group.getMembers().stream()
            .filter(GroupMember::getIsActive)
            .collect(Collectors.toList());
        
        // Get all transactions
        List<GroupTransaction> transactions = groupTransactionRepository.findByGroupIdWithDetails(groupId);
        
        return settlementService.calculateMemberBalances(members, transactions);
    }

    /**
     * Delete a group (soft delete)
     */
    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        group.setIsActive(false);
        groupRepository.save(group);
    }

    // Helper methods
    
    private GroupDetailResponse mapToGroupDetailResponse(Group group) {
        List<MemberDto> memberDtos = group.getMembers().stream()
            .filter(GroupMember::getIsActive)
            .map(member -> new MemberDto(
                member.getId(),
                member.getMemberName(),
                member.getUserId()
            ))
            .collect(Collectors.toList());
        
        return new GroupDetailResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            memberDtos
        );
    }
    
    private GroupTransactionDto mapToGroupTransactionDto(GroupTransaction transaction) {
        List<String> participantNames = transaction.getParticipants().stream()
            .map(p -> p.getMember().getMemberName())
            .collect(Collectors.toList());
        
        return new GroupTransactionDto(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getPaidBy().getMemberName(),
            transaction.getPaidBy().getId(),
            participantNames,
            transaction.getTransactionDate()
        );
    }
}
