\encoding UTF8

BEGIN;

CREATE TYPE user_role AS ENUM ('admin','user');
CREATE TYPE transfer_type AS ENUM ('deposit','withdrawal');

CREATE OR REPLACE FUNCTION is_valid_pesel(pesel_input VARCHAR(11))
  RETURNS BOOLEAN
  LANGUAGE SQL AS $$
    SELECT pesel_input ~ '^\d{11}$'
       AND (
         (
           (SUBSTRING(pesel_input FROM 1 FOR 1)::INT * 1) +
           (SUBSTRING(pesel_input FROM 2 FOR 1)::INT * 3) +
           (SUBSTRING(pesel_input FROM 3 FOR 1)::INT * 7) +
           (SUBSTRING(pesel_input FROM 4 FOR 1)::INT * 9) +
           (SUBSTRING(pesel_input FROM 5 FOR 1)::INT * 1) +
           (SUBSTRING(pesel_input FROM 6 FOR 1)::INT * 3) +
           (SUBSTRING(pesel_input FROM 7 FOR 1)::INT * 7) +
           (SUBSTRING(pesel_input FROM 8 FOR 1)::INT * 9) +
           (SUBSTRING(pesel_input FROM 9 FOR 1)::INT * 1) +
           (SUBSTRING(pesel_input FROM 10 FOR 1)::INT * 3)
         ) % 10
       ) = (10 - SUBSTRING(pesel_input FROM 11 FOR 1)::INT) % 10;
$$;


CREATE TABLE postal_codes (
  postal_code VARCHAR(6) PRIMARY KEY
    CHECK (postal_code ~ '[0-9]{2}-[0-9]{3}')
);

CREATE TABLE towns (
  town_id SERIAL PRIMARY KEY,
  name    VARCHAR(128) NOT NULL
);

CREATE TABLE postal_codes_towns (
  postal_code VARCHAR(6)
    REFERENCES postal_codes(postal_code),
  town_id     INTEGER
    REFERENCES towns(town_id),
  PRIMARY KEY (postal_code, town_id)
);

CREATE TABLE accounts (
  account_id        SERIAL PRIMARY KEY,
  role              user_role    NOT NULL
);

CREATE table accounts_info(
  account_id INTEGER  REFERENCES  accounts,
  updated_at TIMESTAMP DEFAULT current_timestamp,
  PRIMARY key(account_id, updated_at),

  email             VARCHAR(256) NOT NULL -- uwaga bo tu bylo unique
  CHECK (email ~ '.+@.+\.[a-z]+'),
  password          VARCHAR(256) NOT NULL,
  first_name        VARCHAR(128) NOT NULL,
  secondary_name    VARCHAR(128),
  last_name         VARCHAR(256) NOT NULL,
  town_id           INTEGER NOT NULL,
  postal_code       VARCHAR(6) NOT NULL,
  street            VARCHAR(128),
  street_number     VARCHAR(8),
  apartment_number  VARCHAR(8),
  phone_number      VARCHAR(16) NOT NULL,
  CHECK (phone_number ~ '\+[0-9]{10,13}'),
  pesel VARCHAR(11), -- uwaga bo tu bylo unique
  CHECK (is_valid_pesel(pesel)),
  FOREIGN KEY (town_id, postal_code)
  REFERENCES postal_codes_towns(town_id, postal_code)
);
  
CREATE TABLE wallets (
  wallet_id    SERIAL PRIMARY KEY,
  account_id   INTEGER NOT NULL
    REFERENCES accounts(account_id) ON DELETE CASCADE,
  name         VARCHAR(128) NOT NULL
);

CREATE TABLE companies (
  company_id         SERIAL PRIMARY KEY
  --FOR FUTURE EXPANSION
);

CREATE TABLE companies_info(
  company_id INTEGER REFERENCES companies,
  updated_at TIMESTAMP DEFAULT current_date,
  PRIMARY KEY(company_id, updated_at),
  name               VARCHAR(256) NOT NULL,
  code               VARCHAR(3) NOT NULL CHECK (code ~ '[A-Z]{3}'),-- uwaga bo tu bylo uniques 
  town_id            INTEGER NOT NULL,
  postal_code        VARCHAR(6) NOT NULL,
  street             VARCHAR(128),
  street_number      VARCHAR(8),
  apartment_number   VARCHAR(8),
  total_shares       INTEGER NOT NULL CHECK (total_shares > 0),
  FOREIGN KEY (town_id, postal_code)
  REFERENCES postal_codes_towns(town_id, postal_code)
);

CREATE TABLE ipo(
  ipo_id SERIAL PRIMARY KEY,
  company_id INTEGER REFERENCES companies,
  payment_wallet_id    INTEGER REFERENCES wallets(wallet_id),
  shares_amount INTEGER NOT NULL CHECK(shares_amount > 0),
  ipo_price          NUMERIC(17,2) NOT NULL CHECK (ipo_price > 0),
  subscription_start TIMESTAMP NOT NULL,
  subscription_end   TIMESTAMP NOT NULL
  CHECK (subscription_start < subscription_end)
);

CREATE TABLE companies_status(
  company_id INTEGER REFERENCES companies,
  date TIMESTAMP DEFAULT current_date,
  PRIMARY KEY(company_id, date),
  tradable BOOLEAN NOT NULL DEFAULT false 
);

CREATE TABLE external_transfers(
  transfer_id SERIAL PRIMARY KEY,
  wallet_id INTEGER NOT NULL REFERENCES wallets,
  type transfer_type NOT NULL,
  date TIMESTAMP NOT NULL DEFAULT current_date,
  amount NUMERIC(17,2) NOT NULL CHECK(amount > 0)
);

CREATE TABLE order_types (
  order_type VARCHAR(32) PRIMARY KEY
);
  
INSERT INTO order_types VALUES ('sell'), ('buy');

CREATE TABLE orders (
  order_id              SERIAL PRIMARY KEY,
  order_type            VARCHAR(32)
                         REFERENCES order_types(order_type),
  shares_amount         INTEGER NOT NULL CHECK (shares_amount > 0),
  order_start_date      TIMESTAMP NOT NULL DEFAULT current_timestamp,
  order_expiration_date TIMESTAMP,
  share_price           NUMERIC(17,2),
  wallet_id             INTEGER REFERENCES wallets(wallet_id),
  company_id            INTEGER REFERENCES companies(company_id),
  CHECK (order_start_date < order_expiration_date)
);

CREATE OR REPLACE FUNCTION check_order_type(order_id INT, type VARCHAR)
  RETURNS BOOLEAN
  LANGUAGE SQL AS $$
    SELECT (order_type = $2) FROM orders WHERE order_id = $1;
$$;

CREATE TABLE order_cancellations(
  order_id INTEGER REFERENCES orders,
  date TIMESTAMP NOT NULL DEFAULT current_timestamp,
  PRIMARY KEY(order_id, date)
);

CREATE TABLE transactions (
  sell_order_id INT REFERENCES orders(order_id),
  buy_order_id  INT REFERENCES orders(order_id),
  date          TIMESTAMP NOT NULL DEFAULT current_date,
  shares_amount INTEGER NOT NULL CHECK (shares_amount > 0),
  share_price   NUMERIC(17,2) NOT NULL,
  PRIMARY KEY (sell_order_id, buy_order_id),
  CHECK (check_order_type(sell_order_id, 'sell')),
  CHECK (check_order_type(buy_order_id,  'buy'))
);

CREATE TABLE subscriptions (
  subscription_id SERIAL PRIMARY KEY,
  ipo_id      INTEGER REFERENCES ipo,
  wallet_id       INTEGER REFERENCES wallets(wallet_id),
  date            TIMESTAMP NOT NULL DEFAULT current_date,
  shares_amount   INTEGER NOT NULL CHECK (shares_amount > 0),
  shares_assigned INTEGER CHECK (shares_assigned >= 0)
);

COMMIT;