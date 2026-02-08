package com.example.backend.services;

import com.example.backend.dtos.MemberBalanceDto;
import com.example.backend.dtos.SettlementDto;
import com.example.backend.entities.GroupMember;
import com.example.backend.entities.GroupTransaction;
import com.example.backend.entities.GroupTransactionParticipant;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    /**
     * Calculate member balances from all transactions
     * Balance = Total Paid - Total Share
     * Positive balance = should receive money
     * Negative balance = owes money
     */
    public List<MemberBalanceDto> calculateMemberBalances(List<GroupMember> members, 
                                                          List<GroupTransaction> transactions) {
        
        Map<Long, MemberBalanceDto> balanceMap = new HashMap<>();
        
        // Initialize all members with zero balances
        for (GroupMember member : members) {
            balanceMap.put(member.getId(), new MemberBalanceDto(
                member.getId(),
                member.getMemberName(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            ));
        }
        
        // Calculate paid amounts and shares
        for (GroupTransaction transaction : transactions) {
            Long payerId = transaction.getPaidBy().getId();
            BigDecimal amount = transaction.getAmount();
            
            // Add to total paid for payer
            MemberBalanceDto payerBalance = balanceMap.get(payerId);
            if (payerBalance != null) {
                payerBalance.setTotalPaid(payerBalance.getTotalPaid().add(amount));
            }
            
            // Calculate equal share among participants
            List<GroupTransactionParticipant> participants = transaction.getParticipants();
            if (!participants.isEmpty()) {
                BigDecimal sharePerPerson = amount.divide(
                    BigDecimal.valueOf(participants.size()), 
                    2, 
                    RoundingMode.HALF_UP
                );
                
                // Add share to each participant
                for (GroupTransactionParticipant participant : participants) {
                    Long memberId = participant.getMember().getId();
                    MemberBalanceDto memberBalance = balanceMap.get(memberId);
                    if (memberBalance != null) {
                        memberBalance.setTotalShare(
                            memberBalance.getTotalShare().add(sharePerPerson)
                        );
                    }
                }
            }
        }
        
        // Calculate final balance for each member
        for (MemberBalanceDto balance : balanceMap.values()) {
            BigDecimal finalBalance = balance.getTotalPaid().subtract(balance.getTotalShare());
            balance.setBalance(finalBalance.setScale(2, RoundingMode.HALF_UP));
        }
        
        return new ArrayList<>(balanceMap.values());
    }
    
    /**
     * Generate minimal settlement transactions using greedy algorithm
     * This minimizes the number of transactions needed to settle all debts
     */
    public List<SettlementDto> calculateSettlement(List<MemberBalanceDto> balances) {
        List<SettlementDto> settlements = new ArrayList<>();
        
        // Separate creditors (positive balance) and debtors (negative balance)
        List<MemberBalanceDto> creditors = balances.stream()
            .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .sorted((a, b) -> b.getBalance().compareTo(a.getBalance())) // Descending order
            .collect(Collectors.toList());
        
        List<MemberBalanceDto> debtors = balances.stream()
            .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
            .sorted(Comparator.comparing(MemberBalanceDto::getBalance)) // Ascending order (most negative first)
            .collect(Collectors.toList());
        
        // Use two pointers to match creditors and debtors
        int i = 0, j = 0;
        
        while (i < creditors.size() && j < debtors.size()) {
            MemberBalanceDto creditor = creditors.get(i);
            MemberBalanceDto debtor = debtors.get(j);
            
            BigDecimal creditorAmount = creditor.getBalance();
            BigDecimal debtorAmount = debtor.getBalance().abs();
            
            // Take minimum of what creditor is owed and what debtor owes
            BigDecimal settlementAmount = creditorAmount.min(debtorAmount);
            
            // Only create settlement if amount is significant (> 0.01)
            if (settlementAmount.compareTo(new BigDecimal("0.01")) > 0) {
                settlements.add(new SettlementDto(
                    debtor.getMemberName(),
                    creditor.getMemberName(),
                    settlementAmount.setScale(2, RoundingMode.HALF_UP)
                ));
            }
            
            // Update balances
            creditor.setBalance(creditorAmount.subtract(settlementAmount));
            debtor.setBalance(debtor.getBalance().add(settlementAmount));
            
            // Move pointers
            if (creditor.getBalance().compareTo(new BigDecimal("0.01")) < 0) {
                i++; // Creditor is fully paid
            }
            if (debtor.getBalance().abs().compareTo(new BigDecimal("0.01")) < 0) {
                j++; // Debtor has fully paid
            }
        }
        
        return settlements;
    }
    
    /**
     * Main method to calculate settlement from transactions
     */
    public List<SettlementDto> calculateSettlementFromTransactions(
            List<GroupMember> members, 
            List<GroupTransaction> transactions) {
        
        List<MemberBalanceDto> balances = calculateMemberBalances(members, transactions);
        return calculateSettlement(balances);
    }
}
