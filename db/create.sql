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

CREATE TABLE IF NOT EXISTS postal_codes (
  postal_code VARCHAR(6) PRIMARY KEY
    CHECK (postal_code ~ '[0-9]{2}-[0-9]{3}')
);

CREATE TABLE IF NOT EXISTS towns (
  town_id SERIAL PRIMARY KEY,
  name    VARCHAR(128) NOT NULL
);

CREATE TABLE IF NOT EXISTS postal_codes_towns (
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
  name         VARCHAR(128) NOT NULL,
  active       BOOLEAN NOT NULL
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
  date TIMESTAMP DEFAULT current_timestamp,
  PRIMARY KEY(company_id, date),
  tradable BOOLEAN NOT NULL DEFAULT false 
);

CREATE TABLE external_transfers(
  transfer_id SERIAL PRIMARY KEY,
  wallet_id INTEGER NOT NULL REFERENCES wallets,
  type transfer_type NOT NULL,
  date TIMESTAMP NOT NULL DEFAULT current_timestamp,
  amount NUMERIC(17,2) NOT NULL CHECK(amount > 0),
  account_number VARCHAR(26) NOT NULL
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
  order_id INTEGER UNIQUE REFERENCES orders,
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

CREATE OR REPLACE FUNCTION funds_in_wallet(arg_wallet_id INTEGER, arg_before_date TIMESTAMP DEFAULT current_timestamp)
    RETURNS NUMERIC(17,2)
    AS $$
    BEGIN
        RETURN (SELECT COALESCE(SUM(et.amount),0) 
                    FROM external_transfers et 
                    WHERE et.wallet_id = arg_wallet_id AND type = 'deposit' AND date < arg_before_date) --wplacone na konto
                - (SELECT COALESCE(SUM(et.amount),0) 
                    FROM external_transfers et
                    WHERE et.wallet_id = arg_wallet_id AND type = 'withdrawal'AND date < arg_before_date) --wyplacone z konta
                + (SELECT COALESCE(SUM(t.share_price*t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON t.sell_order_id = o.order_id 
                    WHERE o.wallet_id = arg_wallet_id AND t.date < arg_before_date) --pieniadze ze sprzedanych akcji
                - (SELECT COALESCE(SUM(t.share_price*t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON t.buy_order_id = o.order_id 
                    WHERE o.wallet_id = arg_wallet_id AND t.date < arg_before_date) --pieniadze wydane na akcje
                - (SELECT COALESCE(SUM(s.shares_assigned*i.ipo_price),0) 
                    FROM subscriptions s 
                    JOIN ipo i ON i.ipo_id = s.ipo_id 
                    WHERE s.wallet_id = arg_wallet_id AND s.shares_assigned IS NOT NULL AND i.subscription_end < arg_before_date) --pieniadze wydane na zakonczone zapisy
                - (SELECT COALESCE(SUM(s.shares_amount*i.ipo_price),0) 
                    FROM subscriptions s 
                    JOIN ipo i ON i.ipo_id = s.ipo_id 
                    WHERE s.wallet_id = arg_wallet_id AND s.shares_assigned IS NULL  AND s.date < arg_before_date); --pieniadze wydane na trwajace zapisy
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_left_in_orders()
    RETURNS TABLE(order_id INTEGER, shares_left INTEGER)
    AS $$
    BEGIN
        RETURN QUERY SELECT o.order_id,
                o.shares_amount - COALESCE((SELECT SUM(t.shares_amount) FROM transactions t WHERE t.sell_order_id = o.order_id OR t.buy_order_id = o.order_id),0)::INTEGER
                FROM orders o;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_left_in_order(arg_order_id INTEGER)
    RETURNS INTEGER
    AS $$
    BEGIN 
        RETURN (SELECT shares_amount FROM orders WHERE order_id = arg_order_id) - COALESCE((SELECT SUM(t.shares_amount) FROM transactions t WHERE t.sell_order_id = arg_order_id OR t.buy_order_id = arg_order_id),0)::INTEGER;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_in_wallet(arg_wallet_id INTEGER, arg_company_id INTEGER)
    RETURNS INTEGER
    AS $$
    BEGIN
        RETURN (SELECT COALESCE(SUM(t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON o.order_id = t.buy_order_id 
                    WHERE o.wallet_id = arg_wallet_id AND o.company_id = arg_company_id)::INTEGER --kupione akcje
                - (SELECT COALESCE(SUM(t.shares_amount),0) 
                    FROM transactions t 
                    JOIN orders o ON o.order_id = t.sell_order_id 
                    WHERE o.wallet_id = arg_wallet_id AND o.company_id = arg_company_id)::INTEGER --sprzedane akcje
                + (SELECT COALESCE(SUM(s.shares_assigned),0) 
                    FROM subscriptions s 
                    JOIN ipo i ON s.ipo_id = i.ipo_id
                    WHERE s.wallet_id = arg_wallet_id AND s.shares_assigned IS NOT NULL AND i.company_id = arg_company_id)::INTEGER; --akcje kupione w trakcie emisji
    END
$$ LANGUAGE plpgsql;        

CREATE OR REPLACE FUNCTION blocked_shares_in_wallet(arg_wallet_id INTEGER, arg_company_id INTEGER)
    RETURNS INTEGER
    AS $$
    BEGIN
        RETURN (SELECT COALESCE(SUM(shares_left_in_order(o.order_id)),0)
                    FROM orders o
                    WHERE o.wallet_id = arg_wallet_id AND o.company_id = arg_company_id AND o.order_type = 'sell');
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_value(arg_company_id INTEGER)
    RETURNS NUMERIC(17,2)
    AS $$
    BEGIN
    RETURN (SELECT COALESCE((SELECT t.share_price 
                            FROM transactions t
                            JOIN orders o ON t.sell_order_id = o.order_id
                            WHERE o.company_id = arg_company_id
                            ORDER BY t.date DESC LIMIT 1), i.ipo_price)
                    FROM ipo i
                    WHERE i.company_id = arg_company_id AND
                    i.subscription_start = (SELECT subscription_start FROM ipo ii 
                                            WHERE ii.company_id = arg_company_id 
                                            ORDER BY 1 DESC LIMIT 1));
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION shares_value_last_day(arg_company_id INTEGER)
    RETURNS NUMERIC(17,2)
    AS $$
    BEGIN
    RETURN (SELECT t.share_price
            FROM transactions t JOIN orders o ON t.sell_order_id = o.order_id
            WHERE t.date < CURRENT_DATE AND o.company_id = arg_company_id
            ORDER BY t.date DESC
            LIMIT 1);
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION tradable_companies()
       RETURNS TABLE(company_id INTEGER)
       AS $$
       BEGIN
            RETURN QUERY SELECT cs.company_id
                    FROM companies_status cs
                    WHERE date = (SELECT cs1.date FROM companies_status cs1 WHERE cs1.company_id = cs.company_id ORDER BY cs1.date DESC LIMIT 1)
                    AND cs.tradable = true;
            END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE VIEW active_buy_orders AS
    SELECT o.order_id, sl.shares_left, o.order_start_date, o.order_expiration_date, o.share_price, o.wallet_id, o.company_id, o.shares_amount, o.order_type
    FROM orders o
    JOIN shares_left_in_orders() sl ON o.order_id = sl.order_id
    WHERE o.order_type = 'buy' 
    AND sl.shares_left > 0 
    AND (o.order_expiration_date IS NULL OR order_expiration_date > current_timestamp)
    AND o.order_id NOT IN (SELECT oc.order_id FROM order_cancellations oc);

CREATE OR REPLACE VIEW active_sell_orders AS
    SELECT o.order_id, sl.shares_left, o.order_start_date, o.order_expiration_date, o.share_price, o.wallet_id, o.company_id, o.shares_amount, o.order_type
    FROM orders o
    JOIN shares_left_in_orders() sl ON o.order_id = sl.order_id
    WHERE o.order_type = 'sell'
    AND sl.shares_left > 0
    AND (o.order_expiration_date IS NULL OR order_expiration_date > current_timestamp)
    AND o.order_id NOT IN (SELECT oc.order_id FROM order_cancellations oc);

CREATE OR REPLACE FUNCTION unblocked_funds_in_wallet(arg_wallet_id INTEGER)
    RETURNS NUMERIC(17,2)
    AS $$
    BEGIN
        RETURN CASE 
                    WHEN (SELECT COUNT(*) - COUNT(share_price)
                            FROM active_buy_orders abo
                            WHERE abo.wallet_id = arg_wallet_id) != 0
                    THEN 0
                    ELSE funds_in_wallet(arg_wallet_id) - 
                    (SELECT COALESCE(SUM(shares_left_in_order(o.order_id)*o.share_price),0)
                        FROM orders o
                        WHERE o.wallet_id = arg_wallet_id AND o.order_type = 'buy')
                END;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION unblocked_founds_before_market_buy_order(arg_order_id INTEGER)--assumes that it is market buy order
    RETURNS NUMERIC(17,2)
    AS $$
    DECLARE
    before_date TIMESTAMP;
    arg_wallet_id INTEGER;
    BEGIN
        SELECT o.order_start_date INTO before_date
        FROM orders o
        WHERE o.order_id = arg_order_id;
        SELECT o.wallet_id INTO arg_wallet_id
        FROM orders o
        WHERE o.order_id = arg_order_id;
        RETURN CASE
                WHEN (SELECT COUNT(*) - COUNT(share_price)
                        FROM active_buy_orders abo
                        WHERE abo.wallet_id = arg_wallet_id AND abo.order_start_date < before_date) != 0
                THEN 0
                ELSE funds_in_wallet(arg_wallet_id, before_date) -
                (SELECT COALESCE(SUM(shares_left_in_order(o.order_id)*o.share_price),0)
                    FROM orders o
                    WHERE o.wallet_id = arg_wallet_id AND o.order_type = 'buy' AND o.order_start_date < before_date)
            END;
    END;
$$ LANGUAGE plpgsql;

--data validation triggers

CREATE OR REPLACE FUNCTION check_accounts_info()
    RETURNS TRIGGER
    AS $$
    DECLARE
        no_of_diff_accounts INTEGER;
        no_of_updates INTEGER;
    BEGIN
        SELECT COUNT(*) INTO no_of_diff_accounts
        FROM accounts_info ai
        WHERE ai.pesel = NEW.pesel 
            AND ai.account_id != NEW.account_id
            AND ai.updated_at = (SELECT aii.updated_at FROM accounts_info aii 
                                WHERE aii.account_id = ai.account_id 
                                ORDER BY aii.updated_at DESC 
                                LIMIT 1);
        IF no_of_diff_accounts != 0 THEN
            SELECT COUNT(*) INTO no_of_updates
            FROM accounts_info
            WHERE account_id = NEW.account_id;
            IF no_of_updates = 0 THEN
                DELETE FROM accounts a WHERE a.account_id = NEW.account_id;
            END IF;
            RAISE EXCEPTION 'pesel should be unique for each account';
            RETURN NULL;
        END IF;

        SELECT COUNT(*) INTO no_of_diff_accounts 
        FROM accounts_info 
        WHERE email = NEW.email AND account_id != NEW.account_id;
        IF no_of_diff_accounts = 0 THEN
            RETURN NEW;
        ELSE
            SELECT COUNT(*) INTO no_of_updates
            FROM accounts_info
            WHERE account_id = NEW.account_id;
            IF no_of_updates = 0 THEN
                DELETE FROM accounts a WHERE a.account_id = NEW.account_id;
            END IF;
            RAISE EXCEPTION 'email should be unique for each account';
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
        no_of_updates INTEGER;
    BEGIN
        SELECT COUNT(*) INTO no_of_diff_companies
        FROM companies_info
        WHERE code = NEW.code AND company_id != NEW.company_id;
        IF no_of_diff_companies = 0 THEN
            RETURN NEW;
        ELSE
            SELECT COUNT(*) INTO no_of_updates
            FROM companies_info
            WHERE company_id = NEW.company_id;
            IF no_of_updates = 0 THEN
                DELETE FROM companies c WHERE c.company_id = NEW.company_id;
            END IF;
            RAISE EXCEPTION 'code should be unique for each company';
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
        funds NUMERIC(17,2) = unblocked_funds_in_wallet(NEW.wallet_id);
        shares INTEGER = shares_in_wallet(NEW.wallet_id, NEW.company_id);
    BEGIN
        IF NEW.company_id NOT IN (SELECT * FROM tradable_companies()) THEN
            RAISE EXCEPTION 'company % is not tradable', NEW.company_id;
        END IF;
        IF NEW.order_type = 'buy' THEN
            IF funds < NEW.shares_amount*NEW.share_price THEN
                RAISE EXCEPTION 'cannot place order - not enough funds in wallet %', NEW.wallet_id;
                RETURN NULL;
            ELSE 
                RETURN NEW;
            END IF;
        ELSE
            IF shares < NEW.shares_amount THEN
                RAISE EXCEPTION 'cannot place order - not enough shares in wallet %', NEW.wallet_id;
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

CREATE OR REPLACE FUNCTION is_valid_subscription()
    RETURNS TRIGGER
    AS $$
    DECLARE 
        funds NUMERIC(17,2) = unblocked_funds_in_wallet(NEW.wallet_id);
    BEGIN
        IF funds < NEW.shares_amount * (SELECT i.ipo_price FROM ipo i WHERE i.ipo_id = NEW.ipo_id) THEN
            RAISE EXCEPTION 'not enough funds in wallet %', NEW.wallet_id;
        END IF;
        --ipo wciaz trwa
        IF NEW.date > (SELECT i.subscription_end FROM ipo i WHERE i.ipo_id = NEW.ipo_id) THEN
            RAISE EXCEPTION 'subscription has ended';
        END IF;
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER is_valid_subscription_trigger
    BEFORE INSERT ON subscriptions
    FOR EACH ROW
    EXECUTE PROCEDURE is_valid_subscription();

CREATE OR REPLACE FUNCTION is_valid_transaction()
    RETURNS TRIGGER
    AS $$
    BEGIN
        IF NEW.sell_order_id IN (SELECT order_id FROM order_cancellations) THEN
            RAISE EXCEPTION 'sell order is cancelled';
        ELSE IF NEW.buy_order_id IN (SELECT order_id FROM order_cancellations) THEN
            RAISE EXCEPTION 'buy order is cancelled'; END IF;
        END IF;
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER is_valid_transaction_trigger
    BEFORE INSERT ON transactions
    FOR EACH ROW
    EXECUTE PROCEDURE is_valid_transaction();

CREATE OR REPLACE FUNCTION is_valid_cancellation()
    RETURNS TRIGGER
    AS $$
    BEGIN
        IF shares_left_in_order(NEW.order_id) = 0 THEN
            RAISE EXCEPTION 'cannot cancel a completed order';
        END IF;
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER is_valid_cancellation_trigger
    BEFORE INSERT ON order_cancellations
    FOR EACH ROW
    EXECUTE PROCEDURE is_valid_cancellation();

--triggers preventing updates

CREATE OR REPLACE FUNCTION prevent_update_on_immutable_table()
    RETURNS TRIGGER
    AS $$
    BEGIN
        RAISE EXCEPTION 'table %.% is immutable', TG_TABLE_SCHEMA, TG_TABLE_NAME;
        RETURN NULL;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER prevent_update_on_accounts_trigger
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_accounts_info_trigger
    BEFORE UPDATE ON accounts_info
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_companies_trigger
    BEFORE UPDATE ON companies
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_companies_info_trigger
    BEFORE UPDATE ON companies_info
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_ipo_trigger
    BEFORE UPDATE ON ipo
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_companies_status_trigger
    BEFORE UPDATE ON companies_status
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_external_transfers_trigger
    BEFORE UPDATE ON external_transfers
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_order_types_trigger
    BEFORE UPDATE ON order_types
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_orders_trigger
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_order_cancellations_trigger
    BEFORE UPDATE ON order_cancellations
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

CREATE OR REPLACE TRIGGER prevent_update_on_transactions_trigger
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_update_on_immutable_table();

COMMIT;
