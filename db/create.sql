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

CREATE OR REPLACE FUNCTION funds_in_wallets()
    RETURNS TABLE(wallet_id INTEGER, funds NUMERIC(17,2))
    AS $$
    BEGIN
        RETURN QUERY SELECT w.wallet_id,
                (SELECT COALESCE(SUM(et.amount),0) 
                    FROM external_transfers et 
                    WHERE et.wallet_id = w.wallet_id AND type = 'deposit') --wplacone na konto
                - (SELECT COALESCE(SUM(et.amount),0) 
                    FROM external_transfers et
                    WHERE et.wallet_id = w.wallet_id AND type = 'withdrawal') --wyplacone z konta
                + (SELECT COALESCE(SUM(t.share_price*t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON t.sell_order_id = o.order_id 
                    WHERE o.wallet_id = w.wallet_id) --pieniadze ze sprzedanych akcji
                - (SELECT COALESCE(SUM(t.share_price*t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON t.buy_order_id = o.order_id 
                    WHERE o.wallet_id = w.wallet_id) --pieniadze wydane na akcje
                - (SELECT COALESCE(SUM(s.shares_assigned*i.ipo_price),0) 
                    FROM subscriptions s 
                    JOIN ipo i ON i.ipo_id = s.ipo_id 
                    WHERE s.wallet_id = w.wallet_id AND s.shares_assigned IS NOT NULL) --pieniadze wydane na zakonczone zapisy
                - (SELECT COALESCE(SUM(s.shares_amount*i.ipo_price),0) 
                    FROM subscriptions s 
                    JOIN ipo i ON i.ipo_id = s.ipo_id 
                    WHERE s.wallet_id = w.wallet_id AND s.shares_assigned IS NULL) --pieniadze wydane na trwajace zapisy
                FROM wallets w;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_left_in_order() --funkcja na obliczenie ile akcji zostalo w zleceniu
    RETURNS TABLE(order_id INTEGER, shares_left INTEGER)
    AS $$
    BEGIN
        RETURN QUERY SELECT o.order_id,
                o.shares_amount - (SELECT SUM(t.shares_amount) FROM transactions t WHERE t.sell_order_id = o.order_id OR t.buy_order_id = o.order_id)::INTEGER
                FROM orders o;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_in_wallets()  --funkcja na ilosc akcji firm w portfelu
    RETURNS TABLE(wallet_id INTEGER, company_id INTEGER, shares_amount INTEGER)
    AS $$
    BEGIN
        RETURN QUERY SELECT w.wallet_id, c.company_id,
                (SELECT COALESCE(SUM(t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON o.order_id = t.buy_order_id 
                    WHERE o.wallet_id = w.wallet_id AND o.company_id = c.company_id)::INTEGER --kupione akcje
                - (SELECT COALESCE(SUM(t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON o.order_id = t.sell_order_id 
                    WHERE o.wallet_id = w.wallet_id AND o.company_id = c.company_id)::INTEGER --sprzedane akcje
                + (SELECT COALESCE(SUM(s.shares_assigned),0) 
                    FROM subscriptions s 
                    JOIN ipo i ON s.ipo_id = i.ipo_id
                    WHERE s.wallet_id = w.wallet_id AND s.shares_assigned IS NOT NULL AND i.company_id = c.company_id)::INTEGER --akcje kupione w trakcie emisji
                FROM wallets w CROSS JOIN companies c;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION blocked_funds_in_wallets()  --funkcja na zabklokowane srodku w portfelu
    RETURNS TABLE(wallet_id INTEGER, blocked_funds NUMERIC(17,2))
    AS $$
    BEGIN
        RETURN QUERY SELECT w.wallet_id,
                (SELECT COALESCE(SUM(sl.shares_left*o.share_price),0) 
                    FROM orders o 
                    JOIN shares_left_in_order() sl ON sl.order_id = o.order_id 
                    WHERE o.order_type = 'buy')
                FROM wallets w;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION blocked_shares_in_wallets() --funkcja na zablokowane akcje firmy w portfelu
    RETURNS TABLE(wallet_id INTEGER, company_id INTEGER, blocked_shares INTEGER)
    AS $$
    BEGIN
        RETURN QUERY SELECT w.wallet_id, c.company_id,
                (SELECT COALESCE(SUM(sl.shares_left),0) 
                    FROM orders o 
                    JOIN shares_left_in_order() sl ON sl.order_id = o.order_id 
                    WHERE o.order_type = 'sell' AND o.company_id = c.company_id AND o.wallet_id = w.wallet_id)::INTEGER
        FROM wallets w CROSS JOIN companies c;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_accounts_info()
    RETURNS TRIGGER
    AS $$
    DECLARE
        no_of_diff_accounts INTEGER;
    BEGIN
        SELECT COUNT(*) INTO no_of_diff_accounts
        FROM accounts_info
        WHERE pesel = NEW.pesel AND account_id != NEW.account_id;
        IF no_of_diff_accounts != 0 THEN
            RETURN NULL;
        END IF;

        SELECT COUNT(*) INTO no_of_diff_accounts 
        FROM accounts_info 
        WHERE email = NEW.email AND account_id != NEW.account_id;
        IF no_of_diff_accounts = 0 THEN
            RETURN NEW;
        ELSE               
            RETURN NULL;
        END IF;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER check_accounts_info_trigger
    BEFORE INSERT ON accounts_info
    FOR EACH ROW 
    EXECUTE PROCEDURE check_accounts_info();

CREATE OR REPLACE FUNCTION check_companies_info()
    RETURNS TRIGGER
    AS $$
    DECLARE
        no_of_diff_companies INTEGER;
    BEGIN
        SELECT COUNT(*) INTO no_of_diff_companies
        FROM companies_info
        WHERE code = NEW.code AND company_id != NEW.company_id;
        IF no_of_diff_companies = 0 THEN
            RETURN NEW;
        ELSE
            RETURN NULL;
        END IF;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER check_companies_info_trigger
    BEFORE INSERT ON companies_info
    FOR EACH ROW
    EXECUTE PROCEDURE check_companies_info();

CREATE OR REPLACE FUNCTION is_valid_order() --nowe typy zlecen beda wymagaly zmiany
    RETURNS TRIGGER
    AS $$
    DECLARE
        funds NUMERIC(17,2);
        shares INTEGER;
    BEGIN
        IF NEW.order_type = 'buy' THEN
            SELECT f.funds INTO funds
            FROM funds_in_wallets() f
            WHERE f.wallet_id = NEW.wallet_id;
            IF funds < NEW.shares_amount*NEW.share_price THEN
                RETURN NULL;
            ELSE 
                RETURN NEW;
            END IF;
        ELSE
            SELECT f.shares_amount INTO shares
            FROM shares_in_wallets() f
            WHERE f.wallet_id = NEW.wallet_id AND f.company_id = NEW.company_id;
            IF shares < NEW.shares_amount THEN
                RETURN NULL;
            ELSE
                RETURN NEW;
            END IF;
        END IF;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER is_valid_order_trigger
    BEFORE INSERT ON orders
    FOR EACH ROW
    EXECUTE PROCEDURE is_valid_order();
COMMIT;
