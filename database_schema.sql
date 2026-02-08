-- Split-Wise Database Schema
-- Spring Boot will auto-create these tables with spring.jpa.hibernate.ddl-auto=update
-- This is for reference only

-- Groups table (Splits/Events)
CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Group Members table
CREATE TABLE group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id),
    user_id BIGINT,
    member_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

-- Group Transactions table
CREATE TABLE group_transactions (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id),
    description TEXT,
    amount DECIMAL(12, 2) NOT NULL,
    paid_by_member_id BIGINT NOT NULL REFERENCES group_members(id),
    transaction_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_group_transactions_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_transactions_member FOREIGN KEY (paid_by_member_id) REFERENCES group_members(id)
);

-- Group Transaction Participants table
CREATE TABLE group_transaction_participants (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES group_transactions(id),
    member_id BIGINT NOT NULL REFERENCES group_members(id),
    share_amount DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_participants_transaction FOREIGN KEY (transaction_id) REFERENCES group_transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_participants_member FOREIGN KEY (member_id) REFERENCES group_members(id)
);

-- Indexes for performance
CREATE INDEX idx_group_members_group_id ON group_members(group_id);
CREATE INDEX idx_group_members_user_id ON group_members(user_id);
CREATE INDEX idx_group_transactions_group_id ON group_transactions(group_id);
CREATE INDEX idx_group_transactions_date ON group_transactions(transaction_date);
CREATE INDEX idx_participants_transaction_id ON group_transaction_participants(transaction_id);
CREATE INDEX idx_participants_member_id ON group_transaction_participants(member_id);
