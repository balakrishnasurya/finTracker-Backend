# Split-Wise Feature - API Endpoints

## 1. Create a New Group/Split

**Endpoint:** `POST /groups`

**Description:** Creates a new group with specified members for tracking shared expenses. Members can be linked to registered users or exist as standalone names.

**Sample Request:**
```bash
curl -X POST http://localhost:8080/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Goa Trip 2026",
    "description": "Beach vacation expenses",
    "members": [
      { "userId": 101, "name": "Arun" },
      { "userId": 102, "name": "Ram" },
      { "userId": null, "name": "Laxman" },
      { "userId": null, "name": "Sita" }
    ]
  }'
```

**Sample Response:**
```json
{
  "groupId": 1,
  "name": "Goa Trip 2026",
  "description": "Beach vacation expenses",
  "members": [
    { "memberId": 11, "name": "Arun", "userId": 101 },
    { "memberId": 12, "name": "Ram", "userId": 102 },
    { "memberId": 13, "name": "Laxman", "userId": null },
    { "memberId": 14, "name": "Sita", "userId": null }
  ]
}
```

---

## 2. Get All Groups

**Endpoint:** `GET /groups`

**Description:** Retrieves a list of all active groups/splits with basic information including member count and creation date.

**Sample Request:**
```bash
curl -X GET http://localhost:8080/groups
```

**Sample Response:**
```json
[
  {
    "groupId": 1,
    "name": "Goa Trip 2026",
    "description": "Beach vacation expenses",
    "memberCount": 4,
    "createdAt": "2026-02-08T10:30:00"
  },
  {
    "groupId": 2,
    "name": "Roommates",
    "description": "Monthly shared expenses",
    "memberCount": 3,
    "createdAt": "2026-02-05T09:10:00"
  }
]
```

---

## 3. Get Group Details

**Endpoint:** `GET /groups/{groupId}`

**Description:** Retrieves detailed information about a specific group including all member details.

**Sample Request:**
```bash
curl -X GET http://localhost:8080/groups/1
```

**Sample Response:**
```json
{
  "groupId": 1,
  "name": "Goa Trip 2026",
  "description": "Beach vacation expenses",
  "members": [
    { "memberId": 11, "name": "Arun", "userId": 101 },
    { "memberId": 12, "name": "Ram", "userId": 102 },
    { "memberId": 13, "name": "Laxman", "userId": null },
    { "memberId": 14, "name": "Sita", "userId": null }
  ]
}
```

---

## 4. Add Transaction/Expense

**Endpoint:** `POST /groups/{groupId}/transactions`

**Description:** Records a new expense in the group. The amount is automatically divided equally among all included participants. The payer can optionally be included or excluded from the participants list.

**Sample Request:**
```bash
curl -X POST http://localhost:8080/groups/1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Hotel booking",
    "amount": 12000.00,
    "paidByMemberId": 11,
    "includedMemberIds": [11, 12, 13, 14]
  }'
```

**Sample Response:**
```json
{
  "transactionId": 101,
  "description": "Hotel booking",
  "amount": 12000.00,
  "paidByMemberName": "Arun",
  "paidByMemberId": 11,
  "participantNames": ["Arun", "Ram", "Laxman", "Sita"],
  "transactionDate": "2026-02-08T14:30:00"
}
```

---

## 5. Get All Transactions

**Endpoint:** `GET /groups/{groupId}/transactions`

**Description:** Retrieves all expenses recorded in the group, showing who paid, the amount, and which members participated in each expense.

**Sample Request:**
```bash
curl -X GET http://localhost:8080/groups/1/transactions
```

**Sample Response:**
```json
[
  {
    "transactionId": 101,
    "description": "Hotel booking",
    "amount": 12000.00,
    "paidByMemberName": "Arun",
    "paidByMemberId": 11,
    "participantNames": ["Arun", "Ram", "Laxman", "Sita"],
    "transactionDate": "2026-02-08T14:30:00"
  },
  {
    "transactionId": 102,
    "description": "Dinner at restaurant",
    "amount": 2400.00,
    "paidByMemberName": "Ram",
    "paidByMemberId": 12,
    "participantNames": ["Arun", "Ram", "Laxman"],
    "transactionDate": "2026-02-08T20:15:00"
  }
]
```

---

## 6. Calculate Settlement

**Endpoint:** `GET /groups/{groupId}/settlement`

**Description:** Computes the minimal settlement transactions showing who owes money to whom. Uses a greedy algorithm to minimize the number of payment transactions required to settle all debts within the group.

**Sample Request:**
```bash
curl -X GET http://localhost:8080/groups/1/settlement
```

**Sample Response:**
```json
[
  {
    "fromMember": "Ram",
    "toMember": "Arun",
    "amount": 2200.00
  },
  {
    "fromMember": "Laxman",
    "toMember": "Arun",
    "amount": 3800.00
  },
  {
    "fromMember": "Sita",
    "toMember": "Arun",
    "amount": 3000.00
  }
]
```

---

## 7. Get Member Balances

**Endpoint:** `GET /groups/{groupId}/balances`

**Description:** Shows detailed balance breakdown for each member including total amount paid, total share of expenses, and net balance. Positive balance means the member should receive money, negative means they owe money.

**Sample Request:**
```bash
curl -X GET http://localhost:8080/groups/1/balances
```

**Sample Response:**
```json
[
  {
    "memberId": 11,
    "memberName": "Arun",
    "totalPaid": 12000.00,
    "totalShare": 3800.00,
    "balance": 8200.00
  },
  {
    "memberId": 12,
    "memberName": "Ram",
    "totalPaid": 2400.00,
    "totalShare": 4600.00,
    "balance": -2200.00
  },
  {
    "memberId": 13,
    "memberName": "Laxman",
    "totalPaid": 0.00,
    "totalShare": 3800.00,
    "balance": -3800.00
  },
  {
    "memberId": 14,
    "memberName": "Sita",
    "totalPaid": 0.00,
    "totalShare": 3000.00,
    "balance": -3000.00
  }
]
```

---

## 8. Delete Group

**Endpoint:** `DELETE /groups/{groupId}`

**Description:** Soft deletes a group by marking it as inactive. The group data is preserved in the database but will no longer appear in active group listings.

**Sample Request:**
```bash
curl -X DELETE http://localhost:8080/groups/1
```

**Sample Response:**
```
HTTP/1.1 204 No Content
```

---

## Features Summary

- **Equal Split**: All expenses are automatically divided equally among participants
- **Partial Participation**: Not all members need to be included in every expense
- **Flexible Payer**: The person who paid can be included or excluded from the expense split
- **Non-Registered Members**: Members don't need user accounts
- **Minimal Settlements**: Optimized algorithm to minimize number of payment transactions
- **Real-time Calculations**: Balances and settlements calculated on-demand
- **Soft Delete**: Groups can be deleted without losing historical data

---

## Business Logic

### Balance Calculation
- **Total Paid**: Sum of all amounts paid by the member
- **Total Share**: Sum of all expense shares for the member
- **Balance**: Total Paid - Total Share
  - `Positive` = Should receive money (creditor)
  - `Negative` = Owes money (debtor)
  - `Zero` = All settled

### Settlement Algorithm
1. Calculate net balance for each member
2. Separate into creditors (positive balance) and debtors (negative balance)
3. Use greedy algorithm to match debtors with creditors
4. Minimize the total number of payment transactions
5. Ignore amounts less than ₹0.01

---

## Example Workflow

### Scenario: 4 friends on Goa Trip

1. **Create Group**: Arun, Ram, Laxman, Sita
2. **Add Expenses**:
   - Hotel ₹12,000 (paid by Arun, all 4 share) → ₹3,000 each
   - Dinner ₹2,400 (paid by Ram, 3 people share) → ₹800 each
3. **Check Balances**:
   - Arun: Paid ₹12,000, Share ₹3,800 → Balance +₹8,200
   - Ram: Paid ₹2,400, Share ₹4,600 → Balance -₹2,200
   - Laxman: Paid ₹0, Share ₹3,800 → Balance -₹3,800
   - Sita: Paid ₹0, Share ₹3,000 → Balance -₹3,000
4. **Settlement** (3 transactions instead of potential 6):
   - Ram pays Arun ₹2,200
   - Laxman pays Arun ₹3,800
   - Sita pays Arun ₹3,000

---

## Error Responses

### 400 Bad Request - Validation Error
```json
{
  "name": "Group name is required",
  "members": "At least one member is required"
}
```

### 404 Not Found - Resource Not Found
```json
{
  "message": "Group not found with id: 999"
}
```

### 400 Bad Request - Business Logic Error
```json
{
  "message": "Member with id 99 is not a member of this group"
}
```
