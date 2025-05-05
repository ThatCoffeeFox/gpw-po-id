\encoding UTF8

BEGIN;

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
  created_at        TIMESTAMP NOT NULL DEFAULT current_timestamp,
  email             VARCHAR(256) UNIQUE NOT NULL
                      CHECK (email ~ '.+@.+\.[a-z]+'),
  password          VARCHAR(256) NOT NULL,
  role              user_role    NOT NULL,
  first_name        VARCHAR(128) NOT NULL,
  secondary_name    VARCHAR(128),
  last_name         VARCHAR(256) NOT NULL,
  town_id           INTEGER NOT NULL,
  postal_code       VARCHAR(6) NOT NULL,
  street            VARCHAR(128),
  street_number     INTEGER CHECK (street_number > 0),
  apartment_number  VARCHAR(8),
  phone_number      VARCHAR(16) NOT NULL
                      CHECK (phone_number ~ '\+[0-9]{10,13}'),
  pesel             VARCHAR(11) UNIQUE
                      CHECK (is_valid_pesel(pesel)),
  FOREIGN KEY (town_id, postal_code)
    REFERENCES postal_codes_towns(town_id, postal_code)
);

CREATE TABLE wallets (
  wallet_id    SERIAL PRIMARY KEY,
  account_id   INTEGER NOT NULL
    REFERENCES accounts(account_id) ON DELETE CASCADE,
  name         VARCHAR(128) NOT NULL,
  funds        NUMERIC(17,2) NOT NULL DEFAULT 0 CHECK (funds >= 0),
  locked_funds NUMERIC(17,2) NOT NULL DEFAULT 0 CHECK (locked_funds >= 0),
  CHECK (locked_funds <= funds)
);

CREATE TABLE tradable_companies (
  company_id         SERIAL PRIMARY KEY,
  name               VARCHAR(256) NOT NULL,
  code               VARCHAR(3) NOT NULL UNIQUE
                      CHECK (code ~ '[A-Z]{3}'),
  town_id            INTEGER NOT NULL,
  postal_code        VARCHAR(6) NOT NULL,
  street             VARCHAR(128),
  street_number      INTEGER CHECK (street_number > 0),
  apartment_number   VARCHAR(8),
  total_shares       INTEGER NOT NULL CHECK (total_shares > 0),
  owner_wallet_id    INTEGER REFERENCES wallets(wallet_id),
  ipo_price          NUMERIC(17,2) NOT NULL CHECK (ipo_price > 0),
  subscription_start TIMESTAMP NOT NULL,
  subscription_end   TIMESTAMP NOT NULL,
  tradable           BOOLEAN NOT NULL DEFAULT false,
  CHECK (subscription_start < subscription_end),
  FOREIGN KEY (town_id, postal_code)
    REFERENCES postal_codes_towns(town_id, postal_code)
);

CREATE TABLE wallets_tradable_companies (
  wallet_id     INTEGER REFERENCES wallets(wallet_id),
  company_id    INTEGER REFERENCES tradable_companies(company_id),
  shares_amount INTEGER NOT NULL DEFAULT 0 CHECK (shares_amount > 0),
  locked_shares INTEGER NOT NULL DEFAULT 0 CHECK (locked_shares >= 0),
  PRIMARY KEY (wallet_id, company_id),
  CHECK (locked_shares <= shares_amount)
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
  company_id            INTEGER REFERENCES tradable_companies(company_id),
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
  shares_amount INTEGER NOT NULL CHECK (shares_amount > 0),
  share_price   NUMERIC(17,2) NOT NULL,
  PRIMARY KEY (sell_order_id, buy_order_id),
  CHECK (check_order_type(sell_order_id, 'sell')),
  CHECK (check_order_type(buy_order_id,  'buy'))
);

CREATE TABLE subscriptions (
  subscription_id SERIAL PRIMARY KEY,
  company_id      INTEGER REFERENCES tradable_companies(company_id),
  wallet_id       INTEGER REFERENCES wallets(wallet_id),
  date            TIMESTAMP NOT NULL DEFAULT current_date,
  shares_amount   INTEGER NOT NULL CHECK (shares_amount > 0),
  shares_assigned INTEGER CHECK (shares_assigned >= 0)
);

-- towns and postal codes

-- accounts
COPY accounts (
  email, password, role,
  first_name, secondary_name, last_name,
  postal_code, town_id,
  street, street_number, apartment_number,
  phone_number, pesel
) FROM STDIN;
admin@example.com	adminpass	admin	Adam	Krzysztof	Duda	07-405	40662	Zarządcza	1	1	+48100000000	44051401359
user1@example.com	pass1	user	Jan	\N	Kowalski	27-640	40661	Krótka	2	2	+48123123123	85062000117
user2@example.com	pass2	user	Ewa	\N	Nowak	38-111	40661	Zielona	3	3	+48123123124	70120500218
user3@example.com	pass3	user	Szymon	\N	Wiśniewski	11-612	40660	Czerwona	4	4	+48123123125	92031400319
user4@example.com	pass4	user	Stanisław	Krystian	Wójcik	97-320	40659	Niebieska	5	5	+48123123126	80103000415
user5@example.com	pass5	user	Marcel	\N	Kamiński	47-300	40658	Szara	6	6	+48123123127	75072200519
user6@example.com	pass6	user	Robert	\N	Lewandowski	47-300	40657	Biała	7	7	+48123123128	65111100610
user7@example.com	pass7	user	Paweł	\N	Zieliński	11-220	40656	Czarna	8	8	+48123123129	99082500715
user8@example.com	pass8	user	Dawid	\N	Szymański	16-060	40656	Złota	9	9	+48123123130	00210100813
user9@example.com	pass9	user	Katarzyna	\N	Woźniak	11-612	40655	Srebrna	10	10	+48123123131	88041700918
user10@example.com	pass10	user	Małgorzata	\N	Mazur	11-612	40654	Bronowicka	11	11	+48123123132	95090901016
\.

-- wallets
COPY wallets (account_id, name, funds, locked_funds) FROM STDIN;
2	Portfel Jan Główny	20000.00	0
2	Portfel Jan Zapasowy	15000.00	0
3	Portfel Ewa	15000.00	0
4	Portfel Szymon	8000.00	0
5	Portfel Stanisław	12000.00	0
6	Portfel Marcel	20000.00	0
7	Portfel Robert	9000.00	1000
8	Portfel Paweł	11000.00	500
9	Portfel Dawid	7000.00	0
10	Portfel Katarzyna	13000.00	0
\.

-- tradable_companies
COPY tradable_companies (
  name, code, postal_code, town_id,
  street, street_number, apartment_number,
  total_shares, owner_wallet_id,
  ipo_price, subscription_start, subscription_end, tradable
) FROM STDIN;
Zakłady Elektromet	ZEL	47-330	40631	ul. Jerozolimskie	10	1	1000	1	100.00	2025-04-01 00:00:00	2025-04-29 23:59:59	true
Polska Fabryka Samochodów	PFS	05-651	40632	ul. Piłsudskiego	20	2	1500	1	10.00	2025-04-01 00:00:00	2025-05-10 23:59:59	false
Tartak Nowak i Spółka	TNS	05-600	40633	ul. Słowiańska	30	3	2000	1	200.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Warszawskie Zakłady Chemiczne	WZC	16-411	40634	ul. Chemiczna	40	4	2500	1	250.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Elektrownia Słowianin	ESL	24-103	40635	ul. Wolsztyńska	50	5	3000	1	300.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Zespół Przemysłowy Stalowa	ZPS	78-200	40636	ul. Pięciomorgowa	60	6	3500	1	350.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Zakład Produkcji Budowlanej	ZPB	19-505	40637	ul. Wylotowa	70	7	4000	1	400.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Polska Fabryka Wózek	PFW	12-100	40638	ul. Fabryczna	80	8	4500	1	450.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Kopalnia Węgla Kamiennego	KWK	47-435	40639	ul. Kopalniana	90	9	5000	1	500.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
Produkcja Rur i Stali	PRS	68-343	40640	ul. Stalowa	100	10	5500	1	550.00	2025-05-01 00:00:00	2025-05-10 23:59:59	false
\.

-- wallets_tradable_companies
COPY wallets_tradable_companies (wallet_id, company_id, shares_amount, locked_shares) FROM STDIN;
3	1	90	0
4	1	200	0
5	1	300	0
6	1	270	200
9	1	10	0
10	1	130	0
\.

-- orders
COPY orders (
  order_type, shares_amount,
  order_start_date, order_expiration_date,
  share_price, wallet_id, company_id
) FROM STDIN;
buy	10	2025-04-30 14:30:00	2025-04-30 14:33:00	105	9	1
sell	10	2025-04-30 14:30:00	2025-04-30 14:33:00	105	3	1
buy	130	2025-04-30 14:30:00	2025-04-30 14:36:00	100	10	1
sell	150	2025-04-30 14:35:00	2025-04-30 14:50:00	110	6	1
sell	200	2025-04-30 15:30:00	\N	\N	6	1
\.

-- transactions
COPY transactions (sell_order_id, buy_order_id, date, shares_amount, share_price) FROM STDIN;
2	1	2025-04-30 14:30:01	10	105
4	3	2025-04-30 14:35:00	130	110
\.

-- subscriptions
COPY subscriptions (company_id, wallet_id, date, shares_amount, shares_assigned) FROM STDIN;
1	3	2025-04-02 00:00:00	100	100
1	4	2025-04-03 00:00:00	200	100
1	5	2025-04-04 00:00:00	300	100
1	6	2025-04-05 00:00:00	400	100
2	7	2025-04-30 00:00:00	100	\N
2	8	2025-04-30 00:00:00	50	\N
\.

COMMIT;