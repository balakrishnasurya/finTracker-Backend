# Transactions API Update Guide

This document explains how to use the updated Transactions API after the schema change:

- Old meaning of `transaction_type` is now stored as `payment_type`.
- New `transaction_type` is enum-backed and supports only:
  - `CREDIT`
  - `DEBIT`

## 1) What Changed

### Database model
- `payment_type` (string): holds business label like `INCOME`, `EXPENSE`, etc.
- `transaction_type` (enum): holds direction only (`CREDIT` / `DEBIT`).

### API compatibility
For create transaction requests, backend supports both old and new inputs:
- New field: `paymentType`
- Legacy field (still accepted): `transactionType` (treated as payment type for compatibility)
- New enum field: `transactionDirection` (`CREDIT` / `DEBIT`)

If `transactionDirection` is not sent, backend derives it from payment type:
- `INCOME` or `CREDIT` -> `CREDIT`
- anything else -> `DEBIT`

---

## 2) Endpoints

### A) Create Transaction
`POST /api/v1/transactions`

#### Preferred request body (new format)
```json
{
  "smsId": 0,
  "txnDate": "2026-03-22",
  "amount": 1500.00,
  "merchant": "Salary Account",
  "paymentType": "INCOME",
  "transactionDirection": "CREDIT",
  "categoryId": 1
}
```

#### Legacy-compatible request body (still works)
```json
{
  "smsId": 0,
  "txnDate": "2026-03-22",
  "amount": 250.00,
  "merchant": "Supermarket",
  "transactionType": "EXPENSE",
  "categoryId": 2
}
```

#### cURL
```bash
curl -X POST "http://localhost:8080/api/v1/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "txnDate":"2026-03-22",
    "amount":250.00,
    "merchant":"Supermarket",
    "paymentType":"EXPENSE",
    "transactionDirection":"DEBIT",
    "categoryId":2
  }'
```

---

### B) Get Transactions
`GET /api/v1/transactions`

Optional query params:
- `from` (yyyy-MM-dd)
- `to` (yyyy-MM-dd)
- `categoryId`

Example:
```bash
curl "http://localhost:8080/api/v1/transactions?from=2026-03-01&to=2026-03-31&categoryId=2"
```

---

### C) Update Transaction
`PUT /api/v1/transactions/{id}`

Current update endpoint supports:
- `categoryId`
- `notes`

Example:
```json
{
  "categoryId": 3,
  "notes": "Updated note"
}
```

---

### D) Delete Transaction (Soft Delete)
`DELETE /api/v1/transactions/{id}`

Marks transaction as deleted (`isDeleted = true`).

---

### E) Bulk Import Transactions via CSV
`POST /api/v1/import/transactions/csv`

Consumes: `multipart/form-data`

Form field name must be: `file`

#### Supported CSV headers
- Required:
  - `txnDate`
  - `amount`
- Optional:
  - `merchant`
  - `paymentType` (preferred)
  - `transactionType` (legacy fallback -> treated like paymentType)
  - `transactionDirection` (`CREDIT` / `DEBIT`)
  - `categoryName`
  - `notes`

#### CSV example
```csv
txnDate,amount,merchant,paymentType,transactionDirection,categoryName,notes
2026-03-20,145.50,Supermarket,EXPENSE,DEBIT,Groceries,Weekly groceries
2026-03-21,3000.00,Employer,INCOME,CREDIT,Salary,Monthly salary
```

#### cURL
```bash
curl -X POST "http://localhost:8080/api/v1/import/transactions/csv" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@sample-transactions.csv"
```

---

## 3) Response Fields You Will See

Transaction responses include:
- `paymentType` (new business label)
- `transactionType` (legacy compatibility mirror of `paymentType`)
- `transactionDirection` (enum `CREDIT`/`DEBIT`)

This allows old clients to keep working while new clients move to the new contract.

---

## 4) Notes for Swagger UI

- For CSV import endpoint, select `multipart/form-data` and upload a file in field `file`.
- If you still see JSON body for upload, refresh Swagger page and restart app once.
