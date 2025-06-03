\encoding UTF8

BEGIN;

-- Prerequisite: Ensure town and postal code from sample data exist
INSERT INTO towns (town_id, name) VALUES (35803, 'Warszawa') ON CONFLICT (town_id) DO NOTHING;
INSERT INTO postal_codes (postal_code) VALUES ('00-002') ON CONFLICT (postal_code) DO NOTHING;
INSERT INTO postal_codes_towns (postal_code, town_id) VALUES ('00-002', 35803) ON CONFLICT (postal_code, town_id) DO NOTHING;

-- Section 1: Add Funds to Wallets (ensure ample funds for high volume)
INSERT INTO external_transfers (wallet_id, type, amount, date, account_number)
SELECT gs.id, 'deposit', 200000 + (RANDOM()*100000)::INT, NOW() - INTERVAL '95 days' - (gs.id * INTERVAL '12 hours'), '00000000000000000000000000'
FROM generate_series(1,10) AS gs(id);
-- Add a second wave of deposits for very active wallets
INSERT INTO external_transfers (wallet_id, type, amount, date, account_number)
SELECT gs.id, 'deposit', 150000 + (RANDOM()*50000)::INT, NOW() - INTERVAL '60 days' - (gs.id * INTERVAL '12 hours'), '00000000000000000000000000'
FROM generate_series(1,10) AS gs(id) WHERE gs.id % 2 = 0;


-- Section 2: Distribute IPO Shares (ensure ample shares for selling)
INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned)
SELECT 1, gs.id, NOW() - INTERVAL '100 days' - (gs.id * INTERVAL '1 day'), 500 + (RANDOM()*500)::INT, 400 + (RANDOM()*400)::INT
FROM generate_series(1,10) AS gs(id) WHERE gs.id IN (3,4,5,6,8,10)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned)
SELECT 2, gs.id, NOW() - INTERVAL '110 days' - (gs.id * INTERVAL '1 day'), 600 + (RANDOM()*600)::INT, 500 + (RANDOM()*500)::INT
FROM generate_series(1,10) AS gs(id) WHERE gs.id IN (1,7,10)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned)
SELECT 5, gs.id, NOW() - INTERVAL '96 days' - (gs.id * INTERVAL '1 day'), 300 + (RANDOM()*300)::INT, 250 + (RANDOM()*250)::INT
FROM generate_series(1,10) AS gs(id) WHERE gs.id IN (1,2,4)
ON CONFLICT DO NOTHING;


DO $$
DECLARE
    v_order_id_sell INT;
    v_order_id_buy INT;
    v_wallet_id_seller INT;
    v_wallet_id_buyer INT;
    v_shares_to_sell INT;
    v_shares_to_buy INT;
    v_price_sell NUMERIC(17,2);
    v_price_buy NUMERIC(17,2);
    v_company_id INT;
    v_counter INT;
    v_num_orders_per_type INT := 100;
    v_num_transactions_target_per_company INT := 50;
    v_current_shares INT;
    v_available_funds NUMERIC(17,2);

    v_random_day_offset INT;
    v_random_hour_offset INT;
    v_random_minute_offset INT;
    v_generated_order_start_date TIMESTAMP;
    v_sell_order_start_ts TIMESTAMP;
    v_buy_order_start_ts TIMESTAMP;
    v_transaction_base_ts TIMESTAMP;
    v_generated_transaction_date TIMESTAMP;
    v_order_expiration_date TIMESTAMP;

    v_companies_to_trade INT[] := ARRAY[1];
    v_company_ipo_end_date TIMESTAMP;
    v_base_price NUMERIC(17,2);
    v_price_spread NUMERIC(17,2);
BEGIN

    FOREACH v_company_id IN ARRAY v_companies_to_trade
    LOOP
        RAISE NOTICE '--------------------------------------------------------------------';
        RAISE NOTICE 'Symulacja handlu dla Firmy ID % ... Cel transakcji: %', v_company_id, v_num_transactions_target_per_company;
        RAISE NOTICE '--------------------------------------------------------------------';

        SELECT MAX(i.subscription_end) INTO v_company_ipo_end_date
        FROM ipo i
        WHERE i.company_id = v_company_id;

        IF v_company_ipo_end_date IS NULL THEN
            v_company_ipo_end_date := NOW() - INTERVAL '365 days';
            RAISE WARNING 'Brak danych IPO dla firmy %, zakładam, że była handlowalna od roku.', v_company_id;
        END IF;

        IF v_company_id = 1 THEN v_base_price := 10.50; v_price_spread := 0.50;
        ELSIF v_company_id = 2 THEN v_base_price := 5.20; v_price_spread := 0.30;
        ELSIF v_company_id = 4 THEN v_base_price := 19.80; v_price_spread := 0.70;
        ELSE v_base_price := 15.00; v_price_spread := 1.00; END IF;

        RAISE NOTICE 'Generowanie % zleceń sprzedaży dla Firmy ID % (od %)...', v_num_orders_per_type, v_company_id, v_company_ipo_end_date;
        FOR v_counter IN 1..v_num_orders_per_type LOOP
            IF v_counter % 500 = 0 THEN RAISE NOTICE '  Wygenerowano % zleceń sprzedaży dla C%', v_counter, v_company_id; END IF;

            IF v_company_id = 1 THEN SELECT wallet_id INTO v_wallet_id_seller FROM (VALUES (3),(4),(5),(6),(8),(10)) AS s(wallet_id) ORDER BY RANDOM() LIMIT 1;
            ELSIF v_company_id = 2 THEN SELECT wallet_id INTO v_wallet_id_seller FROM (VALUES (1),(7),(10)) AS s(wallet_id) ORDER BY RANDOM() LIMIT 1;
            ELSIF v_company_id = 4 THEN SELECT wallet_id INTO v_wallet_id_seller FROM (VALUES (1),(2),(4)) AS s(wallet_id) ORDER BY RANDOM() LIMIT 1;
            ELSE SELECT gs INTO v_wallet_id_seller FROM generate_series(1,10) gs ORDER BY RANDOM() LIMIT 1; END IF;

            v_current_shares := shares_in_wallet(v_wallet_id_seller, v_company_id) - blocked_shares_in_wallet(v_wallet_id_seller, v_company_id);
            IF v_current_shares < 2 THEN CONTINUE; END IF;

            v_shares_to_sell := GREATEST(1, LEAST(v_current_shares - 1, (RANDOM()*5 + 1)::INT));
            v_price_sell := ROUND((v_base_price + (RANDOM() * v_price_spread * 2) - v_price_spread)::NUMERIC, 2);

            v_random_day_offset := (RANDOM() * 89)::INT;
            v_generated_order_start_date := date_trunc('day',NOW()) - (v_random_day_offset * INTERVAL '1 day') + ((RANDOM()*23)::INT * INTERVAL '1 hour') + ((RANDOM()*59)::INT * INTERVAL '1 minute');
            IF v_generated_order_start_date < v_company_ipo_end_date THEN v_generated_order_start_date := v_company_ipo_end_date + INTERVAL '1 hour' + (RANDOM() * INTERVAL '7 days'); END IF;
            IF v_generated_order_start_date >= NOW() THEN v_generated_order_start_date := NOW() - INTERVAL '2 hours'; END IF;
            v_order_expiration_date := v_generated_order_start_date + INTERVAL '20 days';

            BEGIN
                INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id)
                VALUES ('sell', v_shares_to_sell, v_generated_order_start_date, v_order_expiration_date, v_price_sell, v_wallet_id_seller, v_company_id);
            EXCEPTION WHEN OTHERS THEN
                -- RAISE DEBUG USUNIĘTE
            END;
        END LOOP;

        RAISE NOTICE 'Generowanie % zleceń kupna dla Firmy ID % (od %)...', v_num_orders_per_type, v_company_id, v_company_ipo_end_date;
        FOR v_counter IN 1..v_num_orders_per_type LOOP
            IF v_counter % 500 = 0 THEN RAISE NOTICE '  Wygenerowano % zleceń kupna dla C%', v_counter, v_company_id; END IF;
            SELECT gs INTO v_wallet_id_buyer FROM generate_series(1,10) gs ORDER BY RANDOM() LIMIT 1;

            v_shares_to_buy := (RANDOM()*5 + 1)::INT;
            v_price_buy := ROUND((v_base_price - (RANDOM() * v_price_spread * 2) + v_price_spread)::NUMERIC, 2);

            IF random() < 0.1 THEN v_price_buy := ROUND((v_base_price + (RANDOM() * v_price_spread * 0.5))::NUMERIC, 2); END IF;

            v_available_funds := unblocked_funds_in_wallet(v_wallet_id_buyer);
            IF v_available_funds < v_shares_to_buy * v_price_buy THEN CONTINUE; END IF;

            v_random_day_offset := (RANDOM() * 89)::INT;
            v_generated_order_start_date := date_trunc('day',NOW()) - (v_random_day_offset * INTERVAL '1 day') + ((RANDOM()*23)::INT * INTERVAL '1 hour') + ((RANDOM()*59)::INT * INTERVAL '1 minute');
            IF v_generated_order_start_date < v_company_ipo_end_date THEN v_generated_order_start_date := v_company_ipo_end_date + INTERVAL '1 hour' + (RANDOM() * INTERVAL '7 days'); END IF;
            IF v_generated_order_start_date >= NOW() THEN v_generated_order_start_date := NOW() - INTERVAL '1 hour'; END IF;
            v_order_expiration_date := v_generated_order_start_date + INTERVAL '20 days';

            BEGIN
                INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id)
                VALUES ('buy', v_shares_to_buy, v_generated_order_start_date, v_order_expiration_date, v_price_buy, v_wallet_id_buyer, v_company_id);
            EXCEPTION WHEN OTHERS THEN
                -- RAISE DEBUG USUNIĘTE
            END;
        END LOOP;

        RAISE NOTICE 'Generowanie transakcji dla Firmy ID %...', v_company_id;
        DECLARE
            v_successful_transactions INT := 0;
            v_attempt_counter INT := 0;
            v_max_attempts INT := v_num_transactions_target_per_company * 5;
            v_tx_price NUMERIC(17,2);
        BEGIN
            WHILE v_successful_transactions < v_num_transactions_target_per_company AND v_attempt_counter < v_max_attempts LOOP
                v_attempt_counter := v_attempt_counter + 1;
                IF v_attempt_counter % 200 = 0 THEN RAISE NOTICE '  Próba transakcji #% dla C% (udanych: %)', v_attempt_counter, v_company_id, v_successful_transactions; END IF;

                SELECT abo.order_id, abo.wallet_id, abo.shares_left, abo.share_price, abo.order_start_date
                INTO v_order_id_buy, v_wallet_id_buyer, v_shares_to_buy, v_price_buy, v_buy_order_start_ts
                FROM active_buy_orders abo
                WHERE abo.company_id = v_company_id AND abo.share_price IS NOT NULL AND abo.order_start_date < NOW()
                ORDER BY abo.share_price DESC, abo.order_start_date ASC
                LIMIT 1;

                IF NOT FOUND THEN RAISE NOTICE 'Brak aktywnych zleceń kupna. Kończenie prób.'; EXIT; END IF;

                SELECT aso.order_id, aso.wallet_id, aso.shares_left, aso.share_price, aso.order_start_date
                INTO v_order_id_sell, v_wallet_id_seller, v_shares_to_sell, v_price_sell, v_sell_order_start_ts
                FROM active_sell_orders aso
                WHERE aso.company_id = v_company_id AND aso.share_price IS NOT NULL AND aso.order_start_date < NOW()
                  AND aso.share_price <= v_price_buy
                  AND aso.wallet_id != v_wallet_id_buyer
                  AND aso.order_start_date < (SELECT o.order_expiration_date FROM orders o WHERE o.order_id = v_order_id_buy)
                  AND v_buy_order_start_ts < aso.order_expiration_date
                ORDER BY aso.share_price ASC, aso.order_start_date ASC
                LIMIT 1;

                IF NOT FOUND THEN CONTINUE; END IF;

                DECLARE
                    tx_shares INT := LEAST(v_shares_to_buy, v_shares_to_sell);
                BEGIN
                    IF tx_shares > 0 THEN
                        v_tx_price := v_price_buy;

                        v_transaction_base_ts := GREATEST(v_sell_order_start_ts, v_buy_order_start_ts);
                        v_generated_transaction_date := v_transaction_base_ts + (INTERVAL '1 minute' * (1 + (RANDOM() * 1439)::INT));
                        IF v_generated_transaction_date >= NOW() THEN v_generated_transaction_date := NOW() - INTERVAL '10 seconds'; END IF;
                        IF v_generated_transaction_date <= v_transaction_base_ts THEN v_generated_transaction_date := v_transaction_base_ts + INTERVAL '10 seconds'; END IF;
                        v_generated_transaction_date := LEAST(v_generated_transaction_date, NOW() - INTERVAL '10 seconds');

                        INSERT INTO transactions (sell_order_id, buy_order_id, date, shares_amount, share_price)
                        VALUES (v_order_id_sell, v_order_id_buy, v_generated_transaction_date, tx_shares, v_tx_price);
                        v_successful_transactions := v_successful_transactions + 1;
                    END IF;
                EXCEPTION WHEN OTHERS THEN
                    -- RAISE DEBUG USUNIĘTE
                END;
            END LOOP;
            RAISE NOTICE 'Dla Firmy ID % utworzono % transakcji (cel: %). Prób: %.', v_company_id, v_successful_transactions, v_num_transactions_target_per_company, v_attempt_counter;
        END;
        RAISE NOTICE 'Symulacja handlu dla Firmy ID % zakończona.', v_company_id;
    END LOOP;
END $$;

COMMIT;