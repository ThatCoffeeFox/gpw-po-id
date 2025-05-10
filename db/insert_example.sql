\encoding UTF8
-- 1. Accounts (1 admin, 4 users)
INSERT INTO accounts (role) VALUES
('admin'),
('user'),
('user'),
('user'),
('user');

-- 2. Accounts Info
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
    (1, NOW() - INTERVAL '2 days', 'admin@example.com', '$2a$10$cP0D1tjQTFQYd5LrsYuz4OiIDjHYWovX3wZWsZEgM3d1F5TMiBbh6', 'AdminFirstName', NULL, 'AdminLastName',35803, '00-002', '1', '1', 'a', '+48111222333', '53123075363');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
    (2, NOW() - INTERVAL '2 days', 'user1@example.com', '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi', 'UserOneFirst', 'MidOne', 'UserOneLast', 35803, '00-002', '1', '1', 'a', '+48222333444', '94011484258');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
    (3, NOW() - INTERVAL '2 days', 'user2@example.com', '$2a$10$UaJOkvg4h6fOV34VcHVFSO6HXEUhOQ2q.NTcYiocX79V7Hb4qFa2C', 'UserTwoFirst', NULL, 'UserTwoLast', 35803, '00-002', '1', '1', 'a', '+48333444555', '56072422565');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
    (4, NOW() - INTERVAL '2 days', 'user3@example.com', '$2a$10$7RvmfPpYyiOTJPgnhNLBu.bDwkigaeBRmPvt6iZwDwpOeKtSkoiju', 'UserThreeFirst', 'MidThree', 'UserThreeLast', 35803, '00-002', '1', '1', 'a', '+48444555666', '98103172335');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
    (5, NOW() - INTERVAL '2 days', 'user4@example.com', '$2a$10$TQxTW8484W8ZzNFs0/G8Q..5PrJw8jq1ABQecsbTHH18/YEAwTs4K', 'UserFourFirst', NULL, 'UserFourLast', 35803, '00-002', '1', '1', 'a', '+48555666777', '03250282422');

-- 4. Wallets (2 per account)
INSERT INTO wallets (account_id, name) VALUES
(1, 'Admin Main Wallet'),
(1, 'Admin Investment Wallet'),
(2, 'User1 Primary Wallet'),
(2, 'User1 Trading Wallet'),
(3, 'User2 Cash Wallet'),
(3, 'User2 Stock Wallet'),
(4, 'User3 General Wallet'),
(4, 'User3 IPO Wallet'),
(5, 'User4 Savings Wallet'),
(5, 'User4 Market Wallet');

-- 5. External Transfers
-- wallet 1: 49500
-- wallet 2: 99900
-- wallet 3: 19980
-- wallet 4: 14850
-- wallet 5: 24750
-- wallet 6: 17820
-- wallet 7: 29700
-- wallet 8: 21780
-- wallet 9: 34650
-- wallet 10: 27720
INSERT INTO external_transfers (wallet_id, type, amount, date) VALUES
(1, 'deposit', 50000.00, NOW() - INTERVAL '50 days'),
(2, 'deposit', 100000.00, NOW() - INTERVAL '50 days'),
(3, 'deposit', 20000.00, NOW() - INTERVAL '40 days'),
(4, 'deposit', 15000.00, NOW() - INTERVAL '40 days'),
(5, 'deposit', 25000.00, NOW() - INTERVAL '40 days'),
(6, 'deposit', 18000.00, NOW() - INTERVAL '40 days'),
(7, 'deposit', 30000.00, NOW() - INTERVAL '30 days'),
(8, 'deposit', 22000.00, NOW() - INTERVAL '30 days'),
(9, 'deposit', 35000.00, NOW() - INTERVAL '30 days'),
(10, 'deposit', 28000.00, NOW() - INTERVAL '30 days'),
(1, 'withdrawal', 500.00, NOW() - INTERVAL '40 days'),
(2, 'withdrawal', 100.00, NOW() - INTERVAL '40 days'),
(3, 'withdrawal', 20.00, NOW() - INTERVAL '30 days'),
(4, 'withdrawal', 150.00, NOW() - INTERVAL '30 days'),
(5, 'withdrawal', 250.00, NOW() - INTERVAL '30 days'),
(6, 'withdrawal', 180.00, NOW() - INTERVAL '30 days'),
(7, 'withdrawal', 300.00, NOW() - INTERVAL '20 days'),
(8, 'withdrawal', 220.00, NOW() - INTERVAL '20 days'),
(9, 'withdrawal', 350.00, NOW() - INTERVAL '20 days'),
(10, 'withdrawal', 280.00, NOW() - INTERVAL '20 days');

-- 6. Companies
INSERT INTO companies DEFAULT VALUES;
INSERT INTO companies DEFAULT VALUES;
INSERT INTO companies DEFAULT VALUES;
INSERT INTO companies DEFAULT VALUES;

-- 7. Companies Info
INSERT INTO companies_info (company_id, updated_at, name, code, town_id, postal_code, street, street_number, apartment_number) VALUES
(1, NOW() - INTERVAL '10 days', 'Tech Innovators Inc.', 'TII', 35803, '00-002', 'Innovation Drive', '100', '200'),
(2, NOW() - INTERVAL '10 days', 'Green Solutions Co.', 'GSC', 35803, '00-002', 'Eco Park', '1', NULL),
(3, NOW() - INTERVAL '10 days', 'Future Gadgets Ltd.', 'FGL', 35803, '00-002', 'Gadget Plaza', '42', '3'),
(4, NOW() - INTERVAL '5 days' , 'aaa', 'AAA', 35803, '00-002', 'street' , '2', '5');

-- 8. Companies Info (Simulated update)
INSERT INTO companies_info (company_id, updated_at, name, code, town_id, postal_code, street, street_number, apartment_number) VALUES
(1, NOW() - INTERVAL '5 days', 'Tech Innovators Inc.', 'TII', 35803, '00-002', 'Innovation Drive New', '102', '205'),
(2, NOW() - INTERVAL '5 days', 'Green Solutions Co.', 'GSC', 35803, '00-002', 'Eco Park West', '1A', '2'),
(3, NOW() - INTERVAL '5 days', 'Future Gadgets Ltd.', 'FGL', 35803, '00-002', 'Gadget Plaza North', '42B', '3');

-- 9. IPO 
-- C1: ended
-- C2: ended
-- C3: ongoing
-- C4: ended
-- C4: ended
INSERT INTO ipo (company_id, payment_wallet_id, shares_amount, ipo_price, subscription_start, subscription_end) VALUES
(1, 2, 100000, 10.00, NOW() - INTERVAL '30 days', NOW() - INTERVAL '15 days'),
(2, 2, 200000, 5.50,  NOW() - INTERVAL '20 days', NOW() - INTERVAL '10 days'),
(3, 2, 150000, 12.75, NOW() - INTERVAL '7 days',  NOW() + INTERVAL '7 days'),
(4, 2, 100000, 15.64, NOW() - INTERVAL '30 days', NOW() - INTERVAL '10 days'),
(4, 2, 100000, 20.45, NOW() - INTERVAL '9 days', NOW() - INTERVAL '3 days');

-- 10. Companies Status
-- Company 1,2 & 4: Tradable (IPO ended)
-- Company 3: Not Tradable (IPO ongoing)
INSERT INTO companies_status (company_id, date, tradable) VALUES
(1, NOW() - INTERVAL '30 days', FALSE),
(2, NOW() - INTERVAL '20 days', FALSE),
(1, NOW() - INTERVAL '15 days', TRUE), 
(2, NOW() - INTERVAL '10 days', TRUE), 
(3, NOW() - INTERVAL '7 days', FALSE),
(4, NOW() - INTERVAL '30 days', FALSE),
(4, NOW() - INTERVAL '10 days', TRUE),
(4, NOW() - INTERVAL '9 days', FALSE),
(4, NOW() - INTERVAL '3 days', TRUE);

-- 11. Subscriptions
-- Wallet 3 (funds: 19980) - IPO 1 (price 10)
-- Cost: 500 shares * 10.00 = 5000. New funds: 14980.
INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(1, 3, NOW() - INTERVAL '20 days', 500, 500); 

-- Wallet 4 (funds: 14850) - IPO 1 (price 10)
-- Cost: 300 shares * 10 = 3000. New funds: 11850 - shares_assigned is less than shares_amount
INSERT INTO subscriptions(ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(1, 4, NOW() - INTERVAL '18 days', 500, 300);

-- Wallet 5 (funds: 24750) - IPO 1 (price 10)
-- Cost: 300 shares * 10.00 = 3000. New funds: 21750.
INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(1, 5, NOW() - INTERVAL '18 days', 300, 300);

-- Wallet 7 (funds: 29700) - IPO 2 (price 5.50)
-- Cost: 1000 shares * 5.50 = 5500. New funds: 24200.
INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(2, 7, NOW() - INTERVAL '15 days', 1000, 1000);

-- Wallet 9 (funds: 34650) - IPO 3 (price 12.75, ongoing)
-- Cost: 400 shares * 12.75 = 5100. New funds: 29550
INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(3, 9, NOW() - INTERVAL '2 days', 400, NULL); -- shares_assigned is NULL as IPO is ongoing

-- 12. Orders (for Company 1 and Company 2)
-- Wallet 3 - 500 shares C1
-- Wallet 4 - 300 shares C1
-- Wallet 5 - 300 shares C1
-- Wallet 7 - 1000 shares C2

-- Wallet 3 sells 100 shares of C1 at 11.00 (price: 1100 total)
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 100, NOW() - INTERVAL '3 hours', NOW() + INTERVAL '7 days', 11.00, 3, 1);

-- Wallet 5 wants to buy 50 shares of C1 at 11.00 (Cost 50 * 11.00 = 550.00)
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 50, NOW() - INTERVAL '2 hours', NOW() + INTERVAL '7 days', 11.00, 5, 1);

-- order 1 and 2 matched for 50 shares
-- wallet 3 funds: 15530 + 450 shares C1
-- wallet 5 funds: 21200 + 350 shares C1
INSERT INTO transactions (sell_order_id, buy_order_id, date, shares_amount, share_price) VALUES
(1, 2, NOW() - INTERVAL '1 hour', 50, 11.00);

-- wallet 10 wants to buy 50 shares of C1 (to match with the rest od order 1) (cost = 550.00)
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 50, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '7 days', 11.00, 10, 1);

-- order 1 and 3 marched for 50 shares
-- wallet 3 funds: 16080 + 400 shares C1
-- wallet 10 funds: 27170 + 50 shares C1
INSERT INTO transactions (sell_order_id, buy_order_id, date, shares_amount, share_price) VALUES
(1, 3, NOW() - INTERVAL '30 minutes', 50, 11.00);

-- Wallet 7 sells 200 shares of C2 at 6.00
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 200, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 6.00, 7, 2);

-- Wallet 10 wants to buy 150 shares of C2 at 6.00 (to match above. Cost 150 * 6.00 = 900)
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 150, NOW() - INTERVAL '30 minutes', NOW() + INTERVAL '5 days', 6.00, 10, 2);

-- order 4 and 5 matched for 150 shares
-- Wallet 7 funds: 25100 + 850 shares C2
-- Wallet 10 funds: 26270 + 50 shares C1 + 150 shares C2
INSERT INTO transactions (sell_order_id, buy_order_id, date, shares_amount, share_price) VALUES
(4, 5, NOW(), 150, 6.00);

-- wallet 8 wants to sell 100 shares C1 - order should be cancelled, not enough shares in wallet
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 100, NOW() - INTERVAL '30 minutes', NOW() + INTERVAL '5 days', 10.00, 8, 1);

-- wallet 6 wants to buy 100 shares C1 at high prices - should be cancelled, not enough funds in wallet 
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 100, NOW() - INTERVAL '30 minutes', NOW() + INTERVAL '5 days', 100000.00, 6, 1);

-- wallet 8 wants to buy 500 shares C1 but resignes
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 500, NOW() - INTERVAL '30 minutes', NOW() + INTERVAL '5 days', 10.00, 8, 1);

INSERT INTO order_cancellations (order_id, date) VALUES
(8, NOW() - INTERVAL '1 minute');

--wallet 3 want to sell 50 shares C1 - cant match with above
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 50, NOW() - INTERVAL '30 minutes', NOW() + INTERVAL '5 days', 10.00, 3, 1);
INSERT INTO transactions (sell_order_id, buy_order_id, date, shares_amount, share_price) VALUES
(9, 8, NOW(), 50, 10.00);

-- some more buy and sell orders
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 20, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 8.00, 7, 2);
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 20, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 9.00, 7, 2);
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('sell', 20, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 5.00, 7, 2);
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 20, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 6.00, 9, 2);
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 20, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 6.50, 9, 2);
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 10, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 7.00, 9, 2);

-- should be cancelled - C3 is not tradable
INSERT INTO orders (order_type, shares_amount, order_start_date, order_expiration_date, share_price, wallet_id, company_id) VALUES
('buy', 10, NOW() - INTERVAL '1 hour', NOW() + INTERVAL '5 days', 7.00, 9, 3);