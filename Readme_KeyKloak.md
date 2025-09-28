# Настройкака сервера авторизации OAuth2 Keycloack

1.	Создаем Realm
    * Название: toyshop
    * Сделать его текущим (выбрать в верхнем меню Keycloak).

2.	Создать клиента StoreFront
    * Client ID: storefront
    * Client Protocol: openid-connect
    * Access Type: confidential (если backend обрабатывает токены)
    * Valid Redirect URIs: http://localhost:8085/* (или конкретно http://localhost:8085/login/oauth2/code/*)
    * Включить Standard Flow Enabled для логина пользователя
    * Сохранить и записать client secret (нужен backend для обмена code на токен).

3.	Создать клиента Payment Service
    * Client ID: payment-service
    * Client Protocol: openid-connect
    * Access Type: confidential
    * Включить Service Accounts Enabled (для machine-to-machine, без пользователя)
    * Сохранить и записать client secret (будет использоваться в Payment Service для получения токена через client credentials).

4.	(Опционально) Настроить роли и маппинги
    * Можно создать роли типа USER, ADMIN
    * Для StoreFront назначать роли пользователям
    * Для Payment Service роли обычно не нужны, так как это machine client

5.	Проверка
    * StoreFront: пользователь логинится → получает access token и refresh token
    * Payment Service: получает access token через client credentials → может обращаться к API платежей
