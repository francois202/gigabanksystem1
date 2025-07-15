# Account Management Service

Account Management Service - это бэкенд сервис для управления банковскими счетами, предоставляющий пользователям возможность заводить несколько счетов, осуществлять платежи и переводы, а также получать аналитику по своим тратам.

**Основные функции:**
* Создание и удаление счетов
* Пополнение счетов
* Осуществление платежей и переводов между счетами
* Переводы средств в другие банковские системы
* Предоставление аналитики по тратам
______

Примеры запросов: 

POST /api/accounts
Content-Type: application/json

{
"ownerName": "Иван Иванов",
"ownerEmail": "ivan@example.com",
"ownerPhone": "+79161234567",
"initialBalance": 1000.00
}
______

POST /api/accounts/123/deposit
Content-Type: application/json

{
"amount": 500.00,
"description": "Пополнение через терминал"
}
______

POST /api/transactions/transfer
Content-Type: application/json

{
"fromAccountId": 123,
"toAccountId": 456,
"amount": 200.00,
"description": "Оплата услуг"
}