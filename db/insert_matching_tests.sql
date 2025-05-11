\encoding UTF8

BEGIN;


INSERT INTO accounts (role) VALUES
('admin'),
('user'),
('user'),
('user'),
('user');

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

INSERT INTO companies DEFAULT VALUES;
INSERT INTO companies DEFAULT VALUES;
INSERT INTO companies DEFAULT VALUES;
INSERT INTO companies DEFAULT VALUES;

INSERT INTO companies_info (company_id, updated_at, name, code, town_id, postal_code, street, street_number, apartment_number) VALUES
(1, NOW() - INTERVAL '10 days', 'Tech Innovators Inc.', 'TII', 35803, '00-002', 'Innovation Drive', '100', '200'),
(2, NOW() - INTERVAL '10 days', 'Green Solutions Co.', 'GSC', 35803, '00-002', 'Eco Park', '1', NULL),
(3, NOW() - INTERVAL '10 days', 'Future Gadgets Ltd.', 'FGL', 35803, '00-002', 'Gadget Plaza', '42', '3'),
(4, NOW() - INTERVAL '5 days' , 'aaa', 'AAA', 35803, '00-002', 'street' , '2', '5');
INSERT INTO companies_status (company_id, date, tradable) VALUES
(1, NOW() - INTERVAL '30 days', TRUE),
(2, NOW() - INTERVAL '20 days', TRUE),
(3, NOW() - INTERVAL '15 days', TRUE),
(4, NOW() - INTERVAL '10 days', TRUE);

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

INSERT INTO external_transfers (wallet_id, type, amount, date) VALUES
(1, 'deposit', 50000.00, NOW() - INTERVAL '50 days'),
(2, 'deposit', 100000.00, NOW() - INTERVAL '50 days'),
(3, 'deposit', 20000.00, NOW() - INTERVAL '40 days'),
(4, 'deposit', 100000000000000.00, NOW() - INTERVAL '40 days'),
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
 

INSERT INTO ipo (company_id, payment_wallet_id, shares_amount, ipo_price, subscription_start, subscription_end) VALUES
(1, 2, 100000, 10.00, NOW() - INTERVAL '30 days', NOW() - INTERVAL '15 days'),
(2, 2, 200000, 5.50,  NOW() - INTERVAL '20 days', NOW() - INTERVAL '10 days'),
(3, 2, 150000, 12.75, NOW() - INTERVAL '7 days',  NOW() + INTERVAL '7 days'),
(4, 2, 100000, 15.64, NOW() - INTERVAL '30 days', NOW() - INTERVAL '10 days'),
(4, 2, 100000, 20.45, NOW() - INTERVAL '9 days', NOW() - INTERVAL '3 days');

INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(1, 4, NOW() - INTERVAL '20 days', 100000, 100000);


COMMIT;
