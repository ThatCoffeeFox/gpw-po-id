\encoding UTF8

DO $$
DECLARE
    r RECORD;
    v_new_order_start_date TIMESTAMP;
    v_random_days INT;
    v_random_hours INT;
    v_random_minutes INT;
    v_random_seconds INT;
BEGIN
    RAISE NOTICE 'Rozpoczynanie aktualizacji dat zleceń (orders)...';

    FOR r IN SELECT order_id, order_start_date as old_start_date
            FROM orders
    LOOP
        -- Wygeneruj losową liczbę dni wstecz (0 do 29)
        v_random_days := floor(random() * 90)::INT; -- 0 do 29 dni
        v_random_hours := floor(random() * 24)::INT;
        v_random_minutes := floor(random() * 60)::INT;
        v_random_seconds := floor(random() * 60)::INT;

        -- Ustal nową datę bazową od początku dzisiejszego dnia minus losowe dni
        v_new_order_start_date := date_trunc('day', NOW()) - (v_random_days * INTERVAL '1 day');
        -- Dodaj losową godzinę, minutę, sekundę
        v_new_order_start_date := v_new_order_start_date +
                                  (v_random_hours * INTERVAL '1 hour') +
                                  (v_random_minutes * INTERVAL '1 minute') +
                                  (v_random_seconds * INTERVAL '1 second');

        -- Upewnij się, że data nie jest w przyszłości
        IF v_new_order_start_date > NOW() THEN
            v_new_order_start_date := NOW() - INTERVAL '1 second';
        END IF;

        -- Zaktualizuj order_start_date i order_expiration_date
        -- Zakładamy, że order_expiration_date to zawsze order_start_date + stały interwał (np. 7 dni)
        -- Jeśli masz inną logikę dla order_expiration_date, dostosuj ją.
        -- Sprawdź też, czy order_expiration_date nie jest NULL.
        UPDATE orders
        SET order_start_date = v_new_order_start_date,
            order_expiration_date = CASE
                                        WHEN order_expiration_date IS NOT NULL THEN v_new_order_start_date + (INTERVAL '7 days') -- Dostosuj interwał, jeśli jest inny
                                        ELSE NULL
                                    END
        WHERE order_id = r.order_id;

        -- Sprawdzenie CHECK (order_start_date < order_expiration_date)
        -- Jeśli order_expiration_date stałoby się wcześniejsze lub równe, skoryguj
        IF (SELECT order_expiration_date FROM orders WHERE order_id = r.order_id) IS NOT NULL AND
           (SELECT order_expiration_date FROM orders WHERE order_id = r.order_id) <= (SELECT order_start_date FROM orders WHERE order_id = r.order_id) THEN
            UPDATE orders
            SET order_expiration_date = (SELECT order_start_date FROM orders WHERE order_id = r.order_id) + INTERVAL '1 day' -- Minimalna różnica
            WHERE order_id = r.order_id;
        END IF;


        -- RAISE NOTICE 'Zaktualizowano zlecenie %: % -> %', r.order_id, r.old_start_date, v_new_order_start_date;

    END LOOP;

    RAISE NOTICE 'Aktualizacja dat zleceń (orders) zakończona.';
END $$;

DO $$
DECLARE
    r RECORD;
    v_new_date TIMESTAMP;
    v_random_days_offset_from_order INT; -- Ile dni *po* rozpoczęciu zlecenia ma być transakcja
    v_random_hours INT;
    v_random_minutes INT;
    v_random_seconds INT;
    v_sell_order_start_date TIMESTAMP;
    v_buy_order_start_date TIMESTAMP;
    v_earliest_possible_tx_date TIMESTAMP;
    v_sell_order_expiration_date TIMESTAMP;
    v_buy_order_expiration_date TIMESTAMP;
    v_latest_possible_tx_date TIMESTAMP;
BEGIN
    RAISE NOTICE 'Rozpoczynanie aktualizacji dat transakcji (transactions)...';

    FOR r IN SELECT t.sell_order_id, t.buy_order_id, t.date as old_date,
                   os.order_start_date as sell_start, ob.order_start_date as buy_start,
                   os.order_expiration_date as sell_exp, ob.order_expiration_date as buy_exp
            FROM transactions t
            JOIN orders os ON t.sell_order_id = os.order_id
            JOIN orders ob ON t.buy_order_id = ob.order_id
    LOOP
        v_sell_order_start_date := r.sell_start;
        v_buy_order_start_date := r.buy_start;
        v_sell_order_expiration_date := r.sell_exp;
        v_buy_order_expiration_date := r.buy_exp;

        -- Ustal najwcześniejszą możliwą datę transakcji
        v_earliest_possible_tx_date := GREATEST(v_sell_order_start_date, v_buy_order_start_date) + INTERVAL '1 second';

        -- Ustal najpóźniejszą możliwą datę transakcji (najwcześniejsza z dat wygaśnięcia zleceń lub TERAZ)
        -- Jeśli data wygaśnięcia jest NULL, traktujemy ją jako odległą przyszłość, więc ograniczamy przez NOW()
        IF v_sell_order_expiration_date IS NULL AND v_buy_order_expiration_date IS NULL THEN
            v_latest_possible_tx_date := NOW();
        ELSIF v_sell_order_expiration_date IS NULL THEN
            v_latest_possible_tx_date := LEAST(NOW(), v_buy_order_expiration_date);
        ELSIF v_buy_order_expiration_date IS NULL THEN
            v_latest_possible_tx_date := LEAST(NOW(), v_sell_order_expiration_date);
        ELSE
            v_latest_possible_tx_date := LEAST(NOW(), v_sell_order_expiration_date, v_buy_order_expiration_date);
        END IF;
        
        -- Jeśli z jakiegoś powodu najpóźniejsza data jest wcześniejsza niż najwcześniejsza, ustawiamy na najwcześniejszą + mały interwał
        IF v_latest_possible_tx_date <= v_earliest_possible_tx_date THEN
             v_latest_possible_tx_date := v_earliest_possible_tx_date + INTERVAL '1 hour'; -- Dajmy trochę przestrzeni
        END IF;
        -- Upewnijmy się, że najpóźniejsza możliwa data nie jest w przyszłości
        v_latest_possible_tx_date := LEAST(v_latest_possible_tx_date, NOW() - INTERVAL '1 second');


        -- Generuj losową datę transakcji MIĘDZY v_earliest_possible_tx_date a v_latest_possible_tx_date
        -- Całkowity dostępny interwał w sekundach
        DECLARE
            v_interval_seconds BIGINT;
            v_random_offset_seconds BIGINT;
        BEGIN
            v_interval_seconds := EXTRACT(EPOCH FROM (v_latest_possible_tx_date - v_earliest_possible_tx_date));

            IF v_interval_seconds <= 0 THEN
                -- Jeśli nie ma przestrzeni, użyj najwcześniejszej możliwej daty (już z dodaną sekundą)
                v_new_date := v_earliest_possible_tx_date;
            ELSE
                v_random_offset_seconds := floor(random() * v_interval_seconds)::BIGINT;
                v_new_date := v_earliest_possible_tx_date + (v_random_offset_seconds * INTERVAL '1 second');
            END IF;
        END;


        -- Ostateczne zabezpieczenia
        IF v_new_date > NOW() THEN
            v_new_date := NOW() - INTERVAL '1 second';
        END IF;
        IF v_new_date < v_earliest_possible_tx_date THEN
             v_new_date := v_earliest_possible_tx_date;
        END IF;
        -- Upewnijmy się, że data transakcji jest zawsze przed ewentualnym anulowaniem tych zleceń (jeśli masz takie dane)
        -- To jest bardziej zaawansowane i wymagałoby dołączenia tabeli order_cancellations. Na razie pomijam.

        UPDATE transactions
        SET date = v_new_date
        WHERE sell_order_id = r.sell_order_id AND buy_order_id = r.buy_order_id;

        -- RAISE NOTICE 'Zaktualizowano transakcję (%-%) z % na % (Earliest: %, Latest: %)', r.sell_order_id, r.buy_order_id, r.old_date, v_new_date, v_earliest_possible_tx_date, v_latest_possible_tx_date;

    END LOOP;

    RAISE NOTICE 'Aktualizacja dat transakcji (transactions) zakończona.';
END $$;