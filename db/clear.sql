BEGIN;

DROP TRIGGER IF EXISTS check_accounts_info_trigger ON accounts_info;

DROP TRIGGER IF EXISTS check_companies_info_trigger ON companies_info;

DROP TRIGGER IF EXISTS is_valid_order_trigger ON orders;

DROP TRIGGER IF EXISTS is_valid_transaction_trigger ON transactions;

DROP TRIGGER IF EXISTS is_valid_cancellation_trigger ON order_cancellations;

DROP TRIGGER IF EXISTS is_valid_subscription_trigger ON subscriptions;

DROP TRIGGER IF EXISTS prevent_update_on_accounts_trigger ON accounts;

DROP TRIGGER IF EXISTS prevent_update_on_accounts_info_trigger ON accounts_info;

DROP TRIGGER IF EXISTS prevent_update_on_companies_trigger ON companies;

DROP TRIGGER IF EXISTS prevent_update_on_companies_info_trigger ON companies_info;

DROP TRIGGER IF EXISTS prevent_update_on_ipo_trigger ON ipo;

DROP TRIGGER IF EXISTS prevent_update_on_companies_status_trigger ON companies_status;

DROP TRIGGER IF EXISTS prevent_update_on_external_transfers_trigger ON external_transfers;

DROP TRIGGER IF EXISTS prevent_update_on_order_types_trigger ON order_types;

DROP TRIGGER IF EXISTS prevent_update_on_orders_trigger ON orders;

DROP TRIGGER IF EXISTS prevent_update_on_order_cancellations_trigger ON order_cancellations;

DROP TRIGGER IF EXISTS prevent_update_on_transactions_trigger ON transactions;

DROP TRIGGER IF EXISTS prevent_update_on_subscriptions_trigger ON subscriptions;

DROP TRIGGER IF EXISTS prevent_update_on_ipo_trigger ON ipo;

DROP TRIGGER IF EXISTS prevent_update_on_wallets_trigger ON wallets;

DROP TYPE IF EXISTS user_role CASCADE;

DROP TYPE IF EXISTS transfer_type CASCADE;

DROP FUNCTION IF EXISTS is_valid_pesel CASCADE;

DROP TABLE IF EXISTS towns CASCADE;

DROP TABLE IF EXISTS postal_codes CASCADE;

DROP TABLE IF EXISTS postal_codes_towns CASCADE;

DROP TABLE IF EXISTS accounts CASCADE;

DROP TABLE IF EXISTS accounts_info CASCADE;

DROP TABLE IF EXISTS wallets CASCADE;

DROP TABLE IF EXISTS companies CASCADE;

DROP TABLE IF EXISTS companies_info CASCADE;

DROP TABLE IF EXISTS ipo CASCADE;

DROP TABLE IF EXISTS companies_status CASCADE;

DROP TABLE IF EXISTS external_transfers CASCADE;

DROP FUNCTION IF EXISTS check_order_type CASCADE;

DROP TABLE IF EXISTS order_types CASCADE;

DROP TABLE IF EXISTS orders CASCADE;

DROP TABLE IF EXISTS transactions CASCADE;

DROP TABLE IF EXISTS subscriptions CASCADE;

DROP TABLE IF EXISTS order_cancellations CASCADE;

DROP FUNCTION IF EXISTS funds_in_wallet CASCADE;

DROP FUNCTION IF EXISTS shares_left_in_orders CASCADE;

DROP FUNCTION IF EXISTS shares_left_in_order CASCADE;

DROP FUNCTION IF EXISTS shares_in_wallet CASCADE;

DROP FUNCTION IF EXISTS blocked_shares_in_wallet CASCADE;

DROP FUNCTION IF EXISTS shares_value CASCADE;

DROP FUNCTION IF EXISTS shares_value_last_day CASCADE;

DROP FUNCTION IF EXISTS check_accounts_info CASCADE;

DROP FUNCTION IF EXISTS is_valid_order CASCADE;

DROP FUNCTION IF EXISTS check_companies_info CASCADE;

DROP FUNCTION IF EXISTS tradable_companies CASCADE;

DROP FUNCTION IF EXISTS is_valid_transaction CASCADE;

DROP FUNCTION IF EXISTS is_valid_cancellation CASCADE;

DROP FUNCTION IF EXISTS is_valid_subscription CASCADE;

DROP FUNCTION IF EXISTS is_canceled_order CASCADE;

DROP FUNCTION IF EXISTS unblocked_funds_in_wallet CASCADE;

DROP FUNCTION IF EXISTS unblocked_founds_before_market_buy_order CASCADE;

DROP FUNCTION IF EXISTS prevent_update_on_immutable_table CASCADE;

DROP FUNCTION IF EXISTS prevent_update_on_ipo CASCADE;

DROP FUNCTION IF EXISTS prevent_update_on_wallets CASCADE;

DROP FUNCTION IF EXISTS prevent_update_on_subscriptions CASCADE;

DROP VIEW IF EXISTS active_buy_orders CASCADE;

DROP VIEW IF EXISTS active_sell_orders CASCADE;

COMMIT;

DROP DATABASE IF EXISTS stock_market;

DROP ROLE IF EXISTS superadmin;