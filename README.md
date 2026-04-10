# Hotel Service

REST API сервис для управления отелями.

## Возможности

Приложение позволяет:

- создавать отели
- получать список всех отелей
- получать отель по `id`
- искать отели по параметрам
- добавлять amenities к отелю
- строить histogram по выбранному полю

## Стек

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2
- PostgreSQL
- Liquibase
- MapStruct
- Lombok
- Swagger / OpenAPI
- JUnit 5
- Mockito
- MockMvc

---

## Запуск проекта

### Обычный запуск из IDE

По умолчанию приложение можно запускать прямо кнопкой **Run** из IDE.

`application.properties` настроен так же, как `application-h2.properties`, поэтому при обычном запуске используется **H2** и можно быстро протестировать приложение без дополнительной настройки.

---

## Профили

В проекте настроено 2 профиля:

- `h2` — для работы с H2
- `postgres` — для работы с PostgreSQL

### 1. H2 профиль

Файл:

```text
application-h2.properties
