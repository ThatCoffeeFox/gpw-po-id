CREATE DATABASE stock_market
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    IS_TEMPLATE = False;

DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS accounts_names CASCADE;
DROP TABLE IF EXISTS wallets CASCADE;
DROP TABLE IF EXISTS tradable_companies CASCADE;
DROP TABLE IF EXISTS wallets_tradable_companies CASCADE;
DROP TABLE IF EXISTS order_types CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;

CREATE TYPE user_role as ENUM ('admin', 'user');

CREATE OR REPLACE FUNCTION is_valid_pesel(pesel_input VARCHAR(11))
RETURNS BOOLEAN
AS $$
    SELECT 
        pesel_input ~ '^\d{11}$' 
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
        ) 
        = (10 - SUBSTRING(pesel_input FROM 11 FOR 1)::INT) % 10;
$$ LANGUAGE SQL;

CREATE TABLE accounts (
	account_id SERIAL PRIMARY KEY,
	created_at timestamp DEFAULT current_timestamp,
	email VARCHAR(256) UNIQUE NOT NULL CHECK(email SIMILAR TO '.+\@.+\.[a-z]+'),
	password VARCHAR(256) NOT NULL,
	role user_role NOT NULL,
	last_name VARCHAR(256) NOT NULL,
	phone_number VARCHAR(16) NOT NULL CHECK (phone_number SIMILAR TO '\+[0-9]{10,13}'),
	pesel varchar(11) UNIQUE NOT NULL CHECK (is_valid_pesel(pesel))
);

CREATE TABLE accounts_names (
	name VARCHAR(256) NOT NULL,
	account_id integer REFERENCES accounts,
	name_order integer NOT NULL CHECK (name_order > 0),

	PRIMARY KEY (account_id, name_order)
);

CREATE TABLE wallets (
	wallet_id SERIAL PRIMARY KEY,
	account_id integer REFERENCES accounts,
	name varchar(128) NOT NULL,
	funds numeric(17, 2) NOT NULL DEFAULT 0 CHECK (funds >= 0)
);

CREATE TABLE tradable_companies (
	company_id SERIAL PRIMARY KEY,
	total_shares integer NOT NULL CHECK (total_shares > 0),
	owner_wallet_id integer REFERENCES wallets(wallet_id),
	ipo_price numeric(17, 2) NOT NULL CHECK (ipo_price > 0),
	subscription_start timestamp NOT NULL,
	subscription_end timestamp NOT NULL,
	tradable boolean NOT NULL DEFAULT false,

	CHECK(subscription_start < subscription_end)
);

CREATE TABLE wallets_tradable_companies (
	wallet_id integer REFERENCES wallets,
	company_id integer REFERENCES tradable_companies,
	shares_amount integer NOT NULL CHECK (shares_amount > 0),

	PRIMARY KEY (wallet_id, company_id)
);

CREATE TABLE order_types (
	order_type varchar(32) PRIMARY KEY
);

INSERT INTO order_types VALUES ('sell'), ('buy');

CREATE TABLE orders (
	order_id SERIAL PRIMARY KEY,
	order_type varchar(32) REFERENCES order_types,
	shares_amount integer NOT NULL CHECK (shares_amount > 0),
	order_start_date timestamp NOT NULL DEFAULT current_timestamp,
	order_expiration_date timestamp NOT NULL,
	share_price numeric(17, 2),
	wallet_id integer REFERENCES wallets,
	company_id integer REFERENCES tradable_companies,

	CHECK(order_start_date < order_expiration_date)
);

CREATE OR REPLACE FUNCTION check_order_type(order_id int, type varchar) RETURNS boolean AS
$$
	SELECT (order_type=$2) FROM orders WHERE order_id=$1
$$ 
LANGUAGE SQL;

CREATE TABLE transactions (
	sell_order_id integer REFERENCES orders(order_id),
	buy_order_id integer REFERENCES orders(order_id),
	date timestamp NOT NULL default current_date,
	shares_amount integer NOT NULL CHECK (shares_amount > 0),
	share_price numeric(17, 2) NOT NULL,

	PRIMARY KEY(sell_order_id, buy_order_id),
      
	CHECK(check_order_type(sell_order_id, 'sell')),
	CHECK(check_order_type(buy_order_id, 'buy'))
);

CREATE TABLE subscriptions (
	subscription_id SERIAL PRIMARY KEY,
	company_id integer REFERENCES tradable_companies,
	wallet_id integer REFERENCES wallets,
	date timestamp NOT NULL default current_date,
	shares_amount integer NOT NULL CHECK (shares_amount > 0),
	shares_assigned integer CHECK (COALESCE(shares_assigned, 0) >= 0)
);