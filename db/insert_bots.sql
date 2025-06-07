\encoding UTF8
BEGIN;

-- 1. Create the company owner account
INSERT INTO accounts (role) VALUES ('user');

-- 2. Company owner account info
INSERT INTO accounts_info (
    account_id, updated_at, email, password, first_name, secondary_name,
    last_name, town_id, postal_code, street, street_number,
    apartment_number, phone_number, pesel
) VALUES (
    1, NOW() - INTERVAL '20 days', 'owner@example.com',
    '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
    'OwnerFirst', NULL, 'OwnerLast',
    35803, '00-002', 'Innovation Drive', '100', '200',
    '+48111111111', '58071854316'
);

-- 3. Create the company
INSERT INTO companies DEFAULT VALUES;
-- company_id = 1

-- 4. Company info with fixed address
INSERT INTO companies_info (
    company_id, updated_at, name, code, town_id, postal_code,
    street, street_number, apartment_number
) VALUES (
    1, NOW() - INTERVAL '19 days', 'Tech Innovators Inc.', 'TII',
    35803, '00-002', 'Innovation Drive', '100', '200'
);

-- 5. Make the company tradable
INSERT INTO companies_status (company_id, date, tradable) VALUES
(1, NOW() - INTERVAL '18 days', TRUE);

-- 6. Company owner's wallet
INSERT INTO wallets (account_id, name, active) VALUES
(1, 'Company Wallet', true);
-- wallet_id = 1

-- 7. Create IPO
INSERT INTO ipo (
    company_id, payment_wallet_id, shares_amount, ipo_price,
    subscription_start, subscription_end
) VALUES (
    1, 1, 1000000, 10.00,
    NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days'
);
-- ipo_id = 1

-- 8. Create 10 bot accounts
INSERT INTO accounts (role) VALUES
('user'), ('user'), ('user'), ('user'), ('user'),
('user'), ('user'), ('user'), ('user'), ('user');

-- 9. Accounts_info for each bot with fixed address and unique PESELs
INSERT INTO accounts_info (
    account_id, updated_at, email, password, first_name, secondary_name,
    last_name, town_id, postal_code, street, street_number,
    apartment_number, phone_number, pesel
) VALUES
(2, NOW() - INTERVAL '15 days', 'bot1@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst1', NULL, 'BotLast1', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000001', '86050735781'),

(3, NOW() - INTERVAL '15 days', 'bot2@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst2', NULL, 'BotLast2', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000002', '93042697819'),

(4, NOW() - INTERVAL '15 days', 'bot3@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst3', NULL, 'BotLast3', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000003', '53070284616'),

(5, NOW() - INTERVAL '15 days', 'bot4@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst4', NULL, 'BotLast4', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000004', '01271467644'),

(6, NOW() - INTERVAL '15 days', 'bot5@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst5', NULL, 'BotLast5', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000005', '09231113226'),

(7, NOW() - INTERVAL '15 days', 'bot6@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst6', NULL, 'BotLast6', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000006', '90111323752'),

(8, NOW() - INTERVAL '15 days', 'bot7@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst7', NULL, 'BotLast7', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000007', '93122917558'),

(9, NOW() - INTERVAL '15 days', 'bot8@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst8', NULL, 'BotLast8', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000008', '01212717674'),

(10, NOW() - INTERVAL '15 days', 'bot9@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst9', NULL, 'BotLast9', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000009', '74121674873'),

(11, NOW() - INTERVAL '15 days', 'bot10@example.com',
 '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi',
 'BotFirst10', NULL, 'BotLast10', 35803, '00-002',
 'Innovation Drive', '100', '200', '+48000000010', '88021474682');

-- 10. Create wallets for bots
INSERT INTO wallets (account_id, name, active) VALUES
(2, 'Bot1 Wallet', true),
(3, 'Bot2 Wallet', true),
(4, 'Bot3 Wallet', true),
(5, 'Bot4 Wallet', true),
(6, 'Bot5 Wallet', true),
(7, 'Bot6 Wallet', true),
(8, 'Bot7 Wallet', true),
(9, 'Bot8 Wallet', true),
(10, 'Bot9 Wallet', true),
(11, 'Bot10 Wallet', true);

-- 11. Deposit funds for bots (20,000 per wallet, before subscription)
INSERT INTO external_transfers (wallet_id, type, amount, date, account_number) VALUES
(2, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(3, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(4, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(5, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(6, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(7, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(8, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(9, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(10, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0'),
(11, 'deposit', 200000.00, NOW() - INTERVAL '9 days', '0');


-- 12. Subscribe each bot to IPO with 1,000 shares at 10.00 per share = 10,000.00
INSERT INTO subscriptions (ipo_id, wallet_id, date, shares_amount, shares_assigned) VALUES
(1, 2, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 3, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 4, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 5, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 6, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 7, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 8, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 9, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 10, NOW() - INTERVAL '8 days', 1000, 1000),
(1, 11, NOW() - INTERVAL '8 days', 1000, 1000);

COMMIT;
