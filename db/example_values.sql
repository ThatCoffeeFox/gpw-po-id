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
(2, NOW() - INTERVAL '2 days', 'user1@example.com', '$2a$10$mPotXezp9H7ImdAsorrdouDazSDpJyYPP/o31jZNvjMaadCZveyzi', 'UserOneFirst', 'MidOne', 'UserOneLast', 35803, '00-003', '1', '1', 'a', '+48222333444', '94011484258');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
(3, NOW() - INTERVAL '2 days', 'user2@example.com', '$2a$10$UaJOkvg4h6fOV34VcHVFSO6HXEUhOQ2q.NTcYiocX79V7Hb4qFa2C', 'UserTwoFirst', NULL, 'UserTwoLast', 35803, '00-004', '1', '1', 'a', '+48333444555', '56072422565');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
(4, NOW() - INTERVAL '2 days', 'user3@example.com', '$2a$10$7RvmfPpYyiOTJPgnhNLBu.bDwkigaeBRmPvt6iZwDwpOeKtSkoiju', 'UserThreeFirst', 'MidThree', 'UserThreeLast', 35803, '00-005', '1', '1', 'a', '+48444555666', '98103172335');
INSERT INTO accounts_info (account_id, updated_at, email, password, first_name, secondary_name, last_name, town_id, postal_code, street, street_number, apartment_number, phone_number, pesel) VALUES
(5, NOW() - INTERVAL '2 days', 'user4@example.com', '$2a$10$TQxTW8484W8ZzNFs0/G8Q..5PrJw8jq1ABQecsbTHH18/YEAwTs4K', 'UserFourFirst', NULL, 'UserFourLast', 35803, '00-006', '1', '1', 'a', '+48555666777', '03250282422');

COMMIT;
