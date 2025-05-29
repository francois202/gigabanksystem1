# Account Management Service

Account Management Service - это бэкенд сервис для управления банковскими счетами, предоставляющий пользователям возможность заводить несколько счетов, осуществлять платежи и переводы
, а также получать аналитику по своим тратам.

**Основные функции:**
* Создание и удаление счетов
* Пополнение счетов
* Осуществление платежей и переводов между счетами
* Переводы средств в другие банковские системы
* Предоставление аналитики по тратам

-- Создание таблицы users
CREATE TABLE account_management.users (
id VARCHAR(36) PRIMARY KEY,
first_name VARCHAR(50) NOT NULL,
middle_name VARCHAR(50),
last_name VARCHAR(50) NOT NULL,
birth_date DATE NOT NULL,
email VARCHAR(100) NOT NULL UNIQUE,
phone_number VARCHAR(20)
);

-- Создание таблицы bank_accounts
CREATE TABLE account_management.bank_accounts (
id VARCHAR(36) PRIMARY KEY,
account_number VARCHAR(20) NOT NULL UNIQUE,
balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
currency VARCHAR(3) NOT NULL,
user_id VARCHAR(36) NOT NULL,
FOREIGN KEY (user_id) REFERENCES account_management.users(id)
);

-- Создание таблицы transactions
CREATE TABLE account_management.transactions (
id VARCHAR(36) PRIMARY KEY,
amount DECIMAL(19, 2) NOT NULL,
created_at TIMESTAMP NOT NULL,
description VARCHAR(255),
account_id VARCHAR(36) NOT NULL,
FOREIGN KEY (account_id) REFERENCES account_management.bank_accounts(id)
);

curl для postmen: 
curl --location 'http://localhost:8080/api/users' \
--header 'Content-Type: application/json' \
--data-raw '{
"firstName": "Иван",
"middleName": "Иванович",
"lastName": "Иванов",
"birthDate": "1990-05-15",
"email": "ivanov@example.com",
"phoneNumber": "+79161234567"
}'