package com.example.backend.controllers;

import com.example.backend.dtos.*;
import com.example.backend.services.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "Split-Wise Groups", description = "APIs for managing group expenses and settlements")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Create a new split/group", description = "Creates a new group with specified members for tracking shared expenses")
    public ResponseEntity<GroupDetailResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupDetailResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieves a list of all active groups/splits")
    public ResponseEntity<List<GroupListResponse>> getAllGroups() {
        List<GroupListResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group details", description = "Retrieves detailed information about a specific group including all members")
    public ResponseEntity<GroupDetailResponse> getGroupById(@PathVariable Long groupId) {
        GroupDetailResponse response = groupService.getGroupById(groupId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/transactions")
    @Operation(summary = "Add transaction to group", description = "Records a new expense in the group with specified payer and participants")
    public ResponseEntity<GroupTransactionDto> addTransaction(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupTransactionRequest request) {
        GroupTransactionDto response = groupService.addTransaction(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/transactions")
    @Operation(summary = "Get all transactions", description = "Retrieves all expenses recorded in the group")
    public ResponseEntity<List<GroupTransactionDto>> getGroupTransactions(@PathVariable Long groupId) {
        List<GroupTransactionDto> transactions = groupService.getGroupTransactions(groupId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{groupId}/settlement")
    @Operation(summary = "Calculate settlement", description = "Computes who owes whom and how much using minimal transactions algorithm")
    public ResponseEntity<List<SettlementDto>> calculateSettlement(@PathVariable Long groupId) {
        List<SettlementDto> settlement = groupService.calculateGroupSettlement(groupId);
        return ResponseEntity.ok(settlement);
    }

    @GetMapping("/{groupId}/balances")
    @Operation(summary = "Get member balances", description = "Shows each member's total paid, total share, and net balance")
    public ResponseEntity<List<MemberBalanceDto>> getGroupBalances(@PathVariable Long groupId) {
        List<MemberBalanceDto> balances = groupService.getGroupBalances(groupId);
        return ResponseEntity.ok(balances);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete group", description = "Soft deletes a group (marks as inactive)")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}
