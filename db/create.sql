/* SELECT
  'superadmin'        AS username,
  md5(random()::text) AS password
\gset

\pset format csv
\pset tuples_only
\pset fieldsep ';'
\o admincredentials.csv

SELECT :'username', :'password';

\o
\pset format aligned
\pset tuples_only off
\pset fieldsep ' '

\connect postgres

DROP DATABASE IF EXISTS stock_market;
DROP USER     IF EXISTS :username;

CREATE ROLE :username
  LOGIN
  PASSWORD :'password'
  CREATEDB
  SUPERUSER;

CREATE DATABASE stock_market
  WITH 
  OWNER   = :username
  ENCODING = 'UTF8';

\connect stock_market postgres

*/

BEGIN;
  
  --SET SESSION AUTHORIZATION :username;
  
  CREATE TYPE user_role AS ENUM ('admin','user');

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

  CREATE TABLE accounts (
    account_id        SERIAL PRIMARY KEY,
    created_at        TIMESTAMP    DEFAULT current_timestamp,
    email             VARCHAR(256) UNIQUE NOT NULL
                        CHECK (email ~ '.+@.+\.[a-z]+'),
    password          VARCHAR(256) NOT NULL,
    role              user_role    NOT NULL,
    last_name         VARCHAR(256) NOT NULL,
    town              VARCHAR(128) NOT NULL,
    town_postal_code  VARCHAR(6) NOT NULL,
    street            VARCHAR(128),
    street_number     INTEGER CHECK(street_number > 0),
    apartment_number  INTEGER CHECK(apartment_number > 0),
    phone_number      VARCHAR(16)  NOT NULL
                        CHECK (phone_number ~ '\+[0-9]{10,13}'),
    pesel             VARCHAR(11)  UNIQUE NOT NULL
                        CHECK (is_valid_pesel(pesel))
  );

  CREATE TABLE accounts_names (
    account_id INTEGER REFERENCES accounts,
    name       VARCHAR(256) NOT NULL,
    name_order INTEGER        NOT NULL CHECK (name_order > 0),
    PRIMARY KEY (account_id, name_order)
  );

  CREATE TABLE wallets (
    wallet_id    SERIAL PRIMARY KEY,
    account_id   INTEGER REFERENCES accounts,
    name         VARCHAR(128) NOT NULL,
    funds        NUMERIC(17,2) NOT NULL DEFAULT 0 CHECK (funds >= 0),
    locked_funds NUMERIC(17,2) NOT NULL DEFAULT 0 CHECK(locked_funds >= 0),

    CHECK(locked_funds <= funds)
  );

  CREATE TABLE tradable_companies (
    company_id        SERIAL PRIMARY KEY,
    name              VARCHAR(256) NOT NULL,
    code              VARCHAR(3) NOT NULL UNIQUE CHECK(code ~ '[A-Z]{3}'),
    town              VARCHAR(128) NOT NULL,
    town_postal_code  VARCHAR(6) NOT NULL,
    street            VARCHAR(128),
    street_number     INTEGER CHECK(street_number > 0),
    apartment_number  INTEGER CHECK(apartment_number > 0),
    total_shares      INTEGER        NOT NULL CHECK (total_shares > 0),
    owner_wallet_id   INTEGER        REFERENCES wallets(wallet_id),
    ipo_price         NUMERIC(17,2)  NOT NULL CHECK (ipo_price > 0),
    subscription_start TIMESTAMP     NOT NULL,
    subscription_end   TIMESTAMP     NOT NULL,
    tradable          BOOLEAN        NOT NULL DEFAULT false,
    CHECK (subscription_start < subscription_end)
  );

  CREATE TABLE wallets_tradable_companies (
    wallet_id     INTEGER REFERENCES wallets,
    company_id    INTEGER REFERENCES tradable_companies,
    shares_amount INTEGER NOT NULL DEFAULT 0 CHECK (shares_amount > 0),
    locked_shares INTEGER NOT NULL DEFAULT 0 CHECK(locked_shares >= 0),

    PRIMARY KEY (wallet_id, company_id),
    CHECK(locked_shares <= shares_amount)
  );

  CREATE TABLE order_types (
    order_type VARCHAR(32) PRIMARY KEY
  );
  INSERT INTO order_types VALUES ('sell'), ('buy');

  CREATE TABLE orders (
    order_id               SERIAL PRIMARY KEY,
    order_type             VARCHAR(32) REFERENCES order_types,
    shares_amount          INTEGER        NOT NULL CHECK (shares_amount > 0),
    order_start_date       TIMESTAMP      NOT NULL DEFAULT current_timestamp,
    order_expiration_date  TIMESTAMP      NOT NULL,
    share_price            NUMERIC(17,2),
    wallet_id              INTEGER REFERENCES wallets,
    company_id             INTEGER REFERENCES tradable_companies,
    CHECK (order_start_date < order_expiration_date)
  );

  CREATE OR REPLACE FUNCTION check_order_type(order_id INT, type VARCHAR)
    RETURNS BOOLEAN
    LANGUAGE SQL AS $$
      SELECT (order_type = $2) FROM orders WHERE order_id = $1;
    $$;

  CREATE TABLE transactions (
    sell_order_id INT REFERENCES orders(order_id),
    buy_order_id  INT REFERENCES orders(order_id),
    date          TIMESTAMP NOT NULL DEFAULT current_date,
    shares_amount INTEGER   NOT NULL CHECK (shares_amount > 0),
    share_price   NUMERIC(17,2) NOT NULL,
    PRIMARY KEY (sell_order_id, buy_order_id),
    CHECK (check_order_type(sell_order_id, 'sell')),
    CHECK (check_order_type(buy_order_id,  'buy'))
  );

  CREATE TABLE subscriptions (
    subscription_id SERIAL PRIMARY KEY,
    company_id      INTEGER REFERENCES tradable_companies,
    wallet_id       INTEGER REFERENCES wallets,
    date            TIMESTAMP NOT NULL DEFAULT current_date,
    shares_amount   INTEGER   NOT NULL CHECK (shares_amount > 0),
    shares_assigned INTEGER   CHECK (COALESCE(shares_assigned,0) >= 0)
  );

  --RESET SESSION AUTHORIZATION;

COMMIT;
