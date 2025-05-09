\encoding UTF8

BEGIN;


--TEMPORARY VALUES FOR TESTING:
INSERT INTO accounts(role) VALUES
('admin'),
('user');

INSERT INTO accounts_info(account_id, email, password, first_name, secondary_name, last_name, postal_code, town_id, street, street_number, apartment_number, phone_number, pesel) VALUES 
(1, 'admin@example.com', 'admin', 'Jan', 'III', 'Sobieski', '45-346', 23069, 'Kolorowa', '6A', '1', '+48123456789', '05252802316'), 
(2, 'user@example.com', '1234', 'Anna', 'Maria', 'Wesołowska', '45-401', 23069, 'Stawowa', '1', '3B', '+48098765432', '05252802316');

COMMIT;

BEGIN;

INSERT INTO accounts_info(account_id, email, password, first_name, secondary_name, last_name, postal_code, town_id, street, street_number, apartment_number, phone_number, pesel) VALUES
(2, 'user@example.com', '1234', 'Anna', 'Magdalena', 'Wesołowska', '45-401', 23069, 'Stawowa', '1A', '3B', '+48098765432', '05252802316');

COMMIT;
