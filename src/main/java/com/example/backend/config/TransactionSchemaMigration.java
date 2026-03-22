package com.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateTransactionTypeColumns() {
        String migrationSql = """
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.tables
                        WHERE table_schema = current_schema() AND table_name = 'transactions'
                    ) THEN

                        IF NOT EXISTS (
                            SELECT 1 FROM information_schema.columns
                            WHERE table_schema = current_schema() AND table_name = 'transactions' AND column_name = 'payment_type'
                        ) THEN
                            ALTER TABLE transactions ADD COLUMN payment_type VARCHAR(255);
                        END IF;

                        UPDATE transactions
                        SET payment_type = transaction_type
                        WHERE payment_type IS NULL;

                        UPDATE transactions
                        SET transaction_type = CASE
                            WHEN UPPER(COALESCE(payment_type, '')) IN ('INCOME', 'CREDIT') THEN 'CREDIT'
                            ELSE 'DEBIT'
                        END
                        WHERE transaction_type IS NULL
                           OR UPPER(transaction_type) NOT IN ('CREDIT', 'DEBIT');

                        ALTER TABLE transactions
                        ALTER COLUMN transaction_type SET DEFAULT 'DEBIT';

                        UPDATE transactions
                        SET transaction_type = 'DEBIT'
                        WHERE transaction_type IS NULL;

                        ALTER TABLE transactions
                        ALTER COLUMN transaction_type SET NOT NULL;
                    END IF;
                END $$;
                """;

        try {
            jdbcTemplate.execute(migrationSql);
            log.info("Transaction schema migration completed for payment_type and transaction_type.");
        } catch (Exception ex) {
            log.error("Transaction schema migration failed", ex);
        }
    }
}
