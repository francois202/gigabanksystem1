DELETE FROM app_transaction;
DELETE FROM outbox_messages;
DELETE FROM bank_account;
DELETE FROM app_user;

INSERT INTO app_user (id, name, email, phone)
    VALUES (1, 'VITEK', 'vitya@yahoo.com', '+1234567890');

INSERT INTO bank_account (id, account_number, balance, is_blocked, user_id)
    VALUES (1, 'ACC001', 1000000.00, false, 1);
INSERT INTO bank_account (id, account_number, balance, is_blocked, user_id)
    VALUES (2, 'ACC002', 2000000.00, false, 1);
INSERT INTO bank_account (id, account_number, balance, is_blocked, user_id)
    VALUES (3, 'ACC003', 3000000.00, false, 1);