-- CampusCart schema (PostgreSQL). Idempotent: safe to run on every startup.

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(80)  NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    hostel        VARCHAR(80),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS listings (
    id           BIGSERIAL PRIMARY KEY,
    seller_id    BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id  INT           NOT NULL REFERENCES categories(id),
    title        VARCHAR(120)  NOT NULL,
    description  TEXT,
    price        NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    listing_type VARCHAR(4)    NOT NULL CHECK (listing_type IN ('SELL','RENT')),
    rent_period  VARCHAR(5)    CHECK (rent_period IN ('DAY','WEEK','MONTH')),
    hostel_quick BOOLEAN       NOT NULL DEFAULT FALSE,
    image_url    VARCHAR(500),
    status       VARCHAR(10)   NOT NULL DEFAULT 'AVAILABLE'
                 CHECK (status IN ('AVAILABLE','RESERVED','SOLD')),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    id          BIGSERIAL PRIMARY KEY,
    listing_id  BIGINT      NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status      VARCHAR(10) NOT NULL DEFAULT 'REQUESTED',
    offer_price NUMERIC(10,2),
    message     VARCHAR(300),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Upgrade path for databases created by the first version (bargaining support).
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS offer_price NUMERIC(10,2);
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_status_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_status_check
    CHECK (status IN ('REQUESTED','COUNTERED','ACCEPTED','REJECTED','COMPLETED','CANCELLED'));

CREATE TABLE IF NOT EXISTS conversations (
    id         BIGSERIAL PRIMARY KEY,
    listing_id BIGINT      NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id   BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (listing_id, buyer_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT        NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    body            VARCHAR(1000) NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_listings_category ON listings(category_id);
CREATE INDEX IF NOT EXISTS idx_listings_seller   ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status   ON listings(status);
CREATE INDEX IF NOT EXISTS idx_tx_listing        ON transactions(listing_id);
CREATE INDEX IF NOT EXISTS idx_tx_buyer          ON transactions(buyer_id);
CREATE INDEX IF NOT EXISTS idx_conv_buyer        ON conversations(buyer_id);
CREATE INDEX IF NOT EXISTS idx_msg_conv          ON messages(conversation_id, id);
