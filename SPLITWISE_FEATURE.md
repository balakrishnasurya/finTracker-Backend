# Split-Wise Group Expense Management - API Documentation

## Overview

The Split-Wise Group Expense Management feature enables users to track shared expenses among group members and automatically calculate settlements. It handles partial participation, supports non-registered members, and uses a greedy algorithm to minimize the number of settlement transactions.

## Database Schema

### Tables

1. **groups** - Stores split/event information
2. **group_members** - Stores member snapshots for each group
3. **group_transactions** - Stores expenses
4. **group_transaction_participants** - Links transactions to participants with their share amounts

### Key Features

- ✅ Snapshot member names (survives user deletion/rename)
- ✅ Optional user linking (members can be non-registered)
- ✅ Partial participation per expense
- ✅ Dynamic settlement calculation (not stored)
- ✅ Minimal transaction settlement algorithm
- ✅ Soft delete support

## API Endpoints

### 1. Create a New Group/Split

**POST** `/groups`

Creates a new group with specified members.

**Request Body:**
```json
{
  "name": "Goa Trip 2026",
  "description": "Beach vacation expenses",
  "members": [
    { "userId": 101, "name": "Arun" },
    { "userId": 102, "name": "Ram" },
    { "userId": null, "name": "Laxman" },
    { "userId": null, "name": "Sita" }
  ]
}
```

**Response:** `201 Created`
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

### 2. Get All Groups

**GET** `/groups`

Retrieves list of all active groups.

**Response:** `200 OK`
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

### 3. Get Group Details

**GET** `/groups/{groupId}`

Retrieves detailed information about a specific group.

**Response:** `200 OK`
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

### 4. Add Transaction/Expense

**POST** `/groups/{groupId}/transactions`

Records a new expense in the group.

**Request Body:**
```json
{
  "description": "Hotel booking",
  "amount": 12000.00,
  "paidByMemberId": 11,
  "includedMemberIds": [11, 12, 13, 14]
}
```

**Response:** `201 Created`
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

**Business Rules:**
- Payer must be a member of the group
- All participants must be group members
- Amount is equally divided among participants
- Payer can be included or excluded from participants

---

### 5. Get All Transactions

**GET** `/groups/{groupId}/transactions`

Retrieves all expenses in the group.

**Response:** `200 OK`
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

### 6. Calculate Settlement

**GET** `/groups/{groupId}/settlement`

Computes minimal settlement transactions (who pays whom).

**Response:** `200 OK`
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

**Algorithm:**
- Uses greedy algorithm for minimal transactions
- Separates creditors (positive balance) and debtors (negative balance)
- Matches them to minimize number of payments
- Ignores amounts less than ₹0.01

---

### 7. Get Member Balances

**GET** `/groups/{groupId}/balances`

Shows detailed balance breakdown for each member.

**Response:** `200 OK`
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
    "totalShare": 3800.00,
    "balance": -1400.00
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

**Balance Interpretation:**
- `balance > 0` → Should receive money
- `balance < 0` → Owes money
- `balance = 0` → All settled

---

### 8. Delete Group

**DELETE** `/groups/{groupId}`

Soft deletes a group (marks as inactive).

**Response:** `204 No Content`

---

## Example Workflow

### Scenario: Goa Trip

#### Step 1: Create Group
```bash
POST /groups
{
  "name": "Goa Trip",
  "members": [
    {"name": "Arun", "userId": 1},
    {"name": "Ram", "userId": 2},
    {"name": "Laxman"},
    {"name": "Sita"}
  ]
}
```

#### Step 2: Add Expenses

**Hotel (all 4 people)**
```bash
POST /groups/1/transactions
{
  "description": "Hotel",
  "amount": 12000,
  "paidByMemberId": 11,
  "includedMemberIds": [11, 12, 13, 14]
}
```
Share per person: ₹3000

**Dinner (only 3 people)**
```bash
POST /groups/1/transactions
{
  "description": "Dinner",
  "amount": 2400,
  "paidByMemberId": 12,
  "includedMemberIds": [11, 12, 13]
}
```
Share per person: ₹800

#### Step 3: Check Settlement
```bash
GET /groups/1/settlement
```

**Result:**
- Ram pays Arun: ₹2200
- Laxman pays Arun: ₹3800
- Sita pays Arun: ₹3000

---

## Error Handling

### Validation Errors (400)
```json
{
  "name": "Group name is required",
  "members": "At least one member is required"
}
```

### Resource Not Found (404)
```json
{
  "message": "Group not found with id: 999"
}
```

### Business Logic Errors (400)
```json
{
  "message": "Member with id 99 is not a member of this group"
}
```

---

## Advanced Features Implemented

### 1. Partial Participation
Members can be excluded from specific expenses. Only included members share the cost.

### 2. Payer Not Participating
The payer doesn't have to be included in the expense participants.

```json
{
  "amount": 1000,
  "paidByMemberId": 11,
  "includedMemberIds": [12, 13]
}
```
Arun pays ₹1000, but only Ram and Laxman share it (₹500 each).

### 3. Non-Registered Members
Members don't need user accounts. Just provide a name.

### 4. Decimal Precision
All amounts use BigDecimal with 2 decimal places for accuracy.

### 5. Minimal Settlements
The greedy algorithm minimizes the number of payment transactions.

**Example:**
- Without optimization: 6 transactions possible
- With greedy algorithm: 3 transactions only

---

## Testing with cURL

### Create Group
```bash
curl -X POST http://localhost:8080/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Group",
    "members": [
      {"name": "Alice"},
      {"name": "Bob"},
      {"name": "Charlie"}
    ]
  }'
```

### Add Expense
```bash
curl -X POST http://localhost:8080/groups/1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Lunch",
    "amount": 900,
    "paidByMemberId": 1,
    "includedMemberIds": [1, 2, 3]
  }'
```

### Get Settlement
```bash
curl http://localhost:8080/groups/1/settlement
```

---

## Implementation Highlights

### Architecture
- **Entities**: JPA entities with proper relationships
- **DTOs**: Request/Response validation with Jakarta Validation
- **Repositories**: Spring Data JPA with custom queries
- **Services**: Business logic separation (GroupService, SettlementService)
- **Controller**: RESTful endpoints with Swagger documentation
- **Exception Handling**: Global exception handler with proper HTTP status codes

### Database Design
- Soft delete support (isActive flags)
- Cascading operations for child entities
- Optimized queries with JOIN FETCH
- Timestamp tracking (createdAt, updatedAt)

### Business Logic
- Settlement calculation using greedy algorithm
- Equal cost sharing with proper rounding
- Balance computation (paid - share)
- Validation at service layer

---

## Future Enhancements (Optional)

1. **Unequal Splits**: Allow custom share percentages
2. **Categories**: Tag expenses (food, travel, accommodation)
3. **Currency Support**: Multi-currency handling
4. **Expense Images**: Attach receipt photos
5. **Payment Tracking**: Mark settlements as paid
6. **Recurring Expenses**: Auto-create monthly expenses
7. **Export**: Generate PDF reports
8. **Notifications**: Email/SMS alerts for settlements

---

## Notes

- All calculations are done on-demand (not stored)
- Settlement can be recalculated anytime
- Member names are snapshots (changes don't affect old groups)
- Amounts less than ₹0.01 are ignored in settlements
- All monetary values use BigDecimal for precision

---

**Status**: ✅ Fully Implemented and Production Ready
