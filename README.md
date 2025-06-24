# Account Management Service

Account Management Service - это бэкенд сервис для управления банковскими счетами, предоставляющий пользователям возможность заводить несколько счетов, осуществлять платежи и переводы
, а также получать аналитику по своим тратам.

**Основные функции:**
* Создание и удаление счетов
* Пополнение счетов
* Осуществление платежей и переводов между счетами
* Переводы средств в другие банковские системы
* Предоставление аналитики по тратам

Курлы для проверки авторизации:

Basic Auth:
Запрос защищенный: 
curl --location 'http://localhost:8080/api/private/hello' \
--header 'Authorization: Basic YWRtaW46YWRtaW4xMjM=' \
--header 'Cookie: JSESSIONID=6A7CC069532ED2EA97C2DABA4DF82B9A'

Ответы:
Status: 200 OK
Body: "Это защищенный endpoint, требуется Basic Auth"

или 

Status: 401 Unauthorized
Headers: WWW-Authenticate: Basic realm="Realm"
Body: (пустое или стандартное сообщение об ошибке)

-----------------------------------------------
Form-Based Login:
Откройте в браузере: http://localhost:8080/login

Введите:
Логин: admin / Пароль: admin123
Или: user / user123

После успешного входа попадёте на /dashboard

-----------------------------------------------
jwt: 

Генерация токена: 
curl --location 'http://localhost:8080/api/public/token' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=27483B2C6FAAA3F7E06D9CC23C82A3F5' \
--data '{
"username": "admin",
"password": "admin123"
}'

Проверка запроса по токену, если генерация токена будет не по ключу, в приложении упадет исключение. 
curl --location 'http://localhost:8080/api/protected-endpoint' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1MTA1NTgzOSwiZXhwIjoxNzUxMTQyMjM5fQ.getQd9LqZNhR3Dcq7o29fKJJ5lWbd0GGeUlGA5uGFK8' \
--header 'Cookie: JSESSIONID=28507750E030BB12E8D4ACAE6852B6BB'

Для того чтобы отдавать ответ при получении конкретного токена, нужно проводить сравнение токе из хедера запроса и токена из бд. 

-----------------------------------------------
OAuth:
Регистрация OAuth-приложения в GitHub
1. Перейдите в настройки разработчика:

Войдите в GitHub
Нажмите на ваш аватар в правом верхнем углу
Выберите Settings → Developer settings → OAuth Apps

2. Создайте новое приложение:

Нажмите New OAuth App

Заполните форму:

Поле: Значение (для локальной разработки)
Application name: Gigabank System
Homepage URL:	http://localhost:8080
Authorization callback URL:	http://localhost:8080/login/oauth2/code/github

3. Зарегистрируйте приложение:
Нажмите Register application

2. Получение учетных данных
   Найдите Client ID и Client Secret:

На странице приложения найдите:

Client ID (показывается сразу)
Client Secret (нажмите Generate a new client secret)

Добавьте секреты в application.yml


4. Проверка работы
Запустите приложение
Перейдите по адресу: http://localhost:8080/login
Нажмите "Login with GitHub"
Авторизуйтесь через GitHub