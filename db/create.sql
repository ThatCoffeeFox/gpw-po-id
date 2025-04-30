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
    order_expiration_date  TIMESTAMP,
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

INSERT INTO accounts (email, password, role, last_name, town, town_postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
  ('admin@example.com',   'adminpass', 'admin', 'Duda',       'Warszawa',     '00-001', 'Zarządcza',   1,  1, '+48100000000', '44051401359'),
  ('user1@example.com',   'pass1',     'user',  'Kowalski',   'Kraków',       '31-002', 'Krótka',      2,  2, '+48123123123', '85062000117'),
  ('user2@example.com',   'pass2',     'user',  'Nowak',      'Łódź',         '90-003', 'Zielona',     3,  3, '+48123123124', '70120500218'),
  ('user3@example.com',   'pass3',     'user',  'Wiśniewski', 'Wrocław',      '50-004', 'Czerwona',    4,  4, '+48123123125', '92031400319'),
  ('user4@example.com',   'pass4',     'user',  'Wójcik',     'Poznań',       '60-005', 'Niebieska',   5,  5, '+48123123126', '80103000415'),
  ('user5@example.com',   'pass5',     'user',  'Kamiński',   'Gdańsk',       '80-006', 'Szara',       6,  6, '+48123123127', '75072200519'),
  ('user6@example.com',   'pass6',     'user',  'Lewandowski','Szczecin',     '70-007', 'Biała',       7,  7, '+48123123128', '65111100610'),
  ('user7@example.com',   'pass7',     'user',  'Zieliński',  'Lublin',       '20-008', 'Czarna',      8,  8, '+48123123129', '99082500715'),
  ('user8@example.com',   'pass8',     'user',  'Szymański',  'Białystok',    '15-009', 'Złota',       9,  9, '+48123123130', '00210100813'),
  ('user9@example.com',   'pass9',     'user',  'Woźniak',    'Katowice',     '40-010', 'Srebrna',     10, 10, '+48123123131', '88041700918'),
  ('user10@example.com',  'pass10',    'user',  'Mazur',      'Rzeszów',      '35-011', 'Bronowicka', 11, 11, '+48123123132', '95090901016');

INSERT INTO accounts_names (account_id, name, name_order) VALUES
(1, 'Adam', 1),
(2, 'Jan', 1), (2, 'Paweł', 2),
(3, 'Anna', 1), (3, 'Maria', 2),
(4, 'Piotr', 1), (4, 'Tomasz', 2),
(5, 'Katarzyna', 1), (5, 'Joanna', 2),
(6, 'Michał', 1), (6, 'Robert', 2),
(7, 'Agnieszka', 1), (7, 'Ewa', 2),
(8, 'Tomasz', 1), (8, 'Jakub', 2),
(9, 'Barbara', 1), (9, 'Dorota', 2),
(10, 'Grzegorz', 1), (10, 'Krzysztof', 2),
(11, 'Natalia', 1), (11, 'Zuzanna', 2);

INSERT INTO wallets (account_id, name, funds, locked_funds) VALUES
(2, 'Portfel Jan Główny', 20000.00, 0),
(2, 'Portfel Jan Zapasowy', 15000.00, 0),
(3, 'Portfel Anna', 15000.00, 0),
(4, 'Portfel Piotr', 8000.00, 0),
(5, 'Portfel Katarzyna', 12000.00, 0),
(6, 'Portfel Michał', 20000.00, 0),
(7, 'Portfel Agnieszka', 9000.00, 1000),
(8, 'Portfel Tomasz', 11000.00, 500),
(9, 'Portfel Barbara', 7000.00, 0),
(10, 'Portfel Grzegorz', 13000.00, 0);


INSERT INTO tradable_companies (name, code, town, town_postal_code, street, street_number, apartment_number, total_shares, owner_wallet_id, ipo_price, subscription_start, subscription_end, tradable)
VALUES
    ('Zakłady Elektromet', 'ZEL', 'Warszawa', '00-001', 'ul. Jerozolimskie', 10, 1, 1000, 1, 100.00, '2025-04-01 00:00:00'::TIMESTAMP, '2025-04-29 23:59:59'::TIMESTAMP, true),
    ('Polska Fabryka Samochodów', 'PFS', 'Kraków', '31-001', 'ul. Piłsudskiego', 20, 2, 1500, 1, 10.00, '2025-04-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Tartak Nowak i Spółka', 'TNS', 'Gdańsk', '80-001', 'ul. Słowiańska', 30, 3, 2000, 1, 200.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Warszawskie Zakłady Chemiczne', 'WZC', 'Warszawa', '01-001', 'ul. Chemiczna', 40, 4, 2500, 1, 250.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Elektrownia Słowianin', 'ESL', 'Poznań', '60-001', 'ul. Wolsztyńska', 50, 5, 3000, 1, 300.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Zespół Przemysłowy Stalowa', 'ZPS', 'Wrocław', '50-001', 'ul. Pięciomorgowa', 60, 6, 3500, 1, 350.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Zakład Produkcji Budowlanej', 'ZPB', 'Łódź', '90-001', 'ul. Wylotowa', 70, 7, 4000, 1, 400.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Polska Fabryka Wózek', 'PFW', 'Szczecin', '70-001', 'ul. Fabryczna', 80, 8, 4500, 1, 450.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Kopalnia Węgla Kamiennego', 'KWK', 'Katowice', '40-001', 'ul. Kopalniana', 90, 9, 5000, 1, 500.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false),
    ('Produkcja Rur i Stali', 'PRS', 'Rzeszów', '35-001', 'ul. Stalowa', 100, 10, 5500, 1, 550.00, '2025-05-01 00:00:00'::TIMESTAMP, '2025-05-10 23:59:59'::TIMESTAMP, false);

INSERT INTO wallets_tradable_companies (wallet_id, company_id, shares_amount, locked_shares)
VALUES
  (3,1,90,0),
  (4,1,200,0),
  (5,1,300,0),
  (6,1,270,200),
  (9,1,10,0),
  (10,1,130,0);

INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id)
VALUES
    ('buy', 10, '2025-04-30 14:30:00'::TIMESTAMP, '2025-04-30 14:33:00'::TIMESTAMP, 105, 9, 1),
    ('sell', 10, '2025-04-30 14:30:00'::TIMESTAMP, '2025-04-30 14:33:00'::TIMESTAMP, 105, 3, 1),
    ('buy', 130, '2025-04-30 14:30:00'::TIMESTAMP, '2025-04-30 14:36:00'::TIMESTAMP, NULL, 10, 1),
    ('sell', 150, '2025-04-30 14:35:00'::TIMESTAMP, '2025-04-30 14:50:00'::TIMESTAMP, 110, 6, 1),
    ('sell', 200, '2025-04-30 15:30:00'::TIMESTAMP, NULL::TIMESTAMP, NULL, 6, 1);

INSERT INTO transactions (sell_order_id, buy_order_id, date, shares_amount, share_price)
VALUES
    (2,1, '2025-04-30 14:30:01'::TIMESTAMP, 10, 105),
    (4,3, '2025-04-30 14:35:00'::TIMESTAMP, 130, 110);

INSERT INTO subscriptions (company_id, wallet_id, date, shares_amount, shares_assigned)
VALUES
    (1, 3, '2025-04-02 00:00:00'::TIMESTAMP, 100, 100),
    (1, 4, '2025-04-03 00:00:00'::TIMESTAMP, 200, 100),
    (1, 5, '2025-04-04 00:00:00'::TIMESTAMP, 300, 100),
    (1, 6, '2025-04-05 00:00:00'::TIMESTAMP, 400, 100),
    (2, 7, '2025-04-30 00:00:00'::TIMESTAMP, 100, NULL),
    (2, 8, '2025-04-30 00:00:00'::TIMESTAMP, 50, NULL);
COMMIT;
