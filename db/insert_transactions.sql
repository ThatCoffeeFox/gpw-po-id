\encoding UTF8

DO $$
DECLARE
    -- =================================================================================
    -- KONFIGURACJA
    -- =================================================================================
    v_total_orders_to_generate INT := 50;
    v_tradable_companies INT[] := ARRAY[1, 2, 4]; -- ID spółek, którymi można handlować
    v_user_wallets INT[] := ARRAY[3, 4, 5, 6, 7, 8, 9, 10]; -- Portfele, które będą handlować

    v_shares_per_ipo_subscription INT := 10000; -- Ile akcji dostaje każdy portfel w IPO
    v_funds_deposit_amount NUMERIC(17, 2) := 500000.00; -- Jaki depozyt dostaje każdy portfel

    -- Zmienne pętli i pomocnicze
    i INT;
    v_company_id INT;
    v_wallet_id INT;
    v_ipo_id INT;
    v_ipo_price NUMERIC(17, 2);

    v_order_type VARCHAR;
    v_shares_amount INT;
    v_share_price NUMERIC(17, 2);
    v_available_shares INT;
    v_available_funds NUMERIC(17, 2);
    v_max_shares_to_buy INT;
    v_potential_sellers INT[];
    v_base_price NUMERIC(17, 2);

BEGIN
    RAISE NOTICE '--- ETAP 1: PRZYGOTOWANIE DANYCH (SEEDING) ---';

    -- =================================================================================
    -- 1B. Zasilanie portfeli w środki pieniężne
    -- =================================================================================
    RAISE NOTICE '-> Zasilanie portfeli w środki poprzez External Transfers...';
    FOREACH v_wallet_id IN ARRAY v_user_wallets
    LOOP
        INSERT INTO external_transfers (wallet_id, type, amount, date, account_number)
        VALUES (v_wallet_id, 'deposit', v_funds_deposit_amount, NOW() - INTERVAL '1 day', '99999999999999999999999999');
        RAISE NOTICE '  -> Portfel ID: % zasilony kwotą: %', v_wallet_id, v_funds_deposit_amount;
    END LOOP;

    -- =================================================================================
    -- 1A. Tworzenie IPO i przyznawanie akcji portfelom
    -- =================================================================================
    RAISE NOTICE '-> Tworzenie IPO i subskrypcji, aby zasilić portfele w akcje...';
    FOREACH v_company_id IN ARRAY v_tradable_companies
    LOOP
        -- Ustal cenę IPO dla danej spółki
        SELECT
            CASE v_company_id
                WHEN 1 THEN 10.00
                WHEN 2 THEN 5.00
                WHEN 4 THEN 20.00
            END
        INTO v_ipo_price;

        RAISE NOTICE '  -> Tworzenie IPO dla spółki ID: %, cena: %', v_company_id, v_ipo_price;
        
        -- Stwórz zakończone IPO
        INSERT INTO ipo (company_id, payment_wallet_id, shares_amount, ipo_price, subscription_start, subscription_end)
        VALUES (v_company_id, 1, 1000000, v_ipo_price, NOW() - INTERVAL '10 days', NOW() - INTERVAL '5 days')
        RETURNING ipo_id INTO v_ipo_id;
        
        -- Upewnij się, że spółka jest oznaczona jako 'tradable' po zakończeniu IPO
        INSERT INTO companies_status (company_id, date, tradable)
        VALUES (v_company_id, NOW() - INTERVAL '4 days', TRUE)
        ON CONFLICT DO NOTHING;

        -- Przypisz akcje z tego IPO do każdego z portfeli użytkowników
        FOREACH v_wallet_id IN ARRAY v_user_wallets
        LOOP
            INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned)
            VALUES (v_ipo_id, v_wallet_id, NOW() - INTERVAL '7 days', v_shares_per_ipo_subscription, v_shares_per_ipo_subscription);
        END LOOP;
        
        RAISE NOTICE '     Spółka ID: % - akcje przyznane % portfelom.', v_company_id, array_length(v_user_wallets, 1);
    END LOOP;


    RAISE NOTICE '--- ETAP 2: GENEROWANIE ZLECEŃ HANDLOWYCH ---';
    FOR i IN 1..v_total_orders_to_generate LOOP
        -- Losuj typ zlecenia
        IF random() < 0.5 THEN
            v_order_type := 'sell';
        ELSE
            v_order_type := 'buy';
        END IF;

        -- Losuj spółkę
        v_company_id := v_tradable_companies[1 + floor(random() * array_length(v_tradable_companies, 1))];
        
        -- Ustaw bazową cenę dla spółki (nieco wyższą niż cena IPO)
        SELECT
            CASE v_company_id
                WHEN 1 THEN 11.50
                WHEN 2 THEN 6.20
                WHEN 4 THEN 20.80
            END
        INTO v_base_price;

        -- =================================================================================
        -- Logika dla ZLECENIA SPRZEDAŻY (SELL)
        -- =================================================================================
        IF v_order_type = 'sell' THEN
            SELECT array_agg(w.wallet_id) INTO v_potential_sellers
            FROM wallets w
            WHERE w.wallet_id = ANY(v_user_wallets)
              AND shares_in_wallet(w.wallet_id, v_company_id) > 0;

            IF array_length(v_potential_sellers, 1) IS NULL THEN
                i := i - 1; CONTINUE;
            END IF;

            v_wallet_id := v_potential_sellers[1 + floor(random() * array_length(v_potential_sellers, 1))];
            v_available_shares := shares_in_wallet(v_wallet_id, v_company_id) - blocked_shares_in_wallet(v_wallet_id, v_company_id);
            
            IF v_available_shares <= 0 THEN
                i := i - 1; CONTINUE;
            END IF;

            v_shares_amount := 1 + floor(random() * LEAST(v_available_shares, 500)); -- Sprzedaj do 500 akcji
            v_share_price := round(v_base_price + (random() * 2 - 0.5)::numeric, 2);

            RAISE NOTICE 'SELL: Wallet %, Company %, Shares: %, Price: % (Available Shares: %)', 
                         v_wallet_id, v_company_id, v_shares_amount, v_share_price, v_available_shares;

        -- =================================================================================
        -- Logika dla ZLECENIA KUPNA (BUY)
        -- =================================================================================
        ELSE -- v_order_type = 'buy'
            v_wallet_id := v_user_wallets[1 + floor(random() * array_length(v_user_wallets, 1))];
            v_share_price := round(v_base_price + (random() * 0.5 - 1)::numeric, 2);
            v_available_funds := unblocked_funds_in_wallet(v_wallet_id);
            
            IF v_available_funds < v_share_price THEN
                i := i - 1; CONTINUE;
            END IF;

            v_max_shares_to_buy := floor(v_available_funds / v_share_price);

            IF v_max_shares_to_buy <= 0 THEN
                i := i - 1; CONTINUE;
            END IF;

            v_shares_amount := 1 + floor(random() * LEAST(v_max_shares_to_buy, 500)); -- Kup do 500 akcji

            RAISE NOTICE 'BUY:  Wallet %, Company %, Shares: %, Price: % (Available Funds: %, Max Shares: %)', 
                         v_wallet_id, v_company_id, v_shares_amount, v_share_price, v_available_funds, v_max_shares_to_buy;

        END IF;

        -- Wstaw poprawne zlecenie do bazy danych
        INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id)
        VALUES (v_order_type, v_shares_amount, NOW() - (random() * INTERVAL '2 minutes'), NOW() + INTERVAL '7 days', v_share_price, v_wallet_id, v_company_id);

    END LOOP;

    RAISE NOTICE '--- Zakończono generowanie % zleceń. ---', v_total_orders_to_generate;
END;
$$ LANGUAGE plpgsql;