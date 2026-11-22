# Локальный запуск meinangebot (PostgreSQL)

Инструкция для macOS (Apple Silicon и Intel). Время: ~10 минут.

## Что нужно

| Компонент | Версия | Проверка |
|---|---|---|
| Java (JDK) | 17 или новее | `java -version` |
| Maven | 3.8+ (или встроенный в IntelliJ) | `mvn -v` |
| Homebrew | любая | `brew -v` |
| PostgreSQL | 16 | `psql --version` |

Homebrew, если его нет: https://brew.sh

---

## 1. Установить PostgreSQL

```bash
brew install postgresql@16
brew services start postgresql@16
```

`postgresql@16` не добавляется в PATH автоматически. Один раз выполните:

```bash
echo 'export PATH="$(brew --prefix)/opt/postgresql@16/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
psql --version   # должно показать 16.x
```

PostgreSQL теперь запускается сам при входе в систему.

## 2. Создать пользователя и базу

```bash
psql postgres -c "CREATE USER angebot WITH PASSWORD 'angebot';"
createdb -O angebot angebot
```

Проверка:

```bash
psql -h localhost -U angebot -d angebot -c "select 1"
# пароль: angebot
```

Таблицы создавать не нужно — это делает Flyway при первом старте
(`backend/src/main/resources/db/migration/V1__init_schema.sql`).

## 3. Запустить приложение

### Вариант A: IntelliJ IDEA

1. Открыть проект `meinangebot`.
2. Окно **Maven** → кнопка **Reload All Maven Projects** (подтянет драйвер PostgreSQL).
3. Один раз: **Maven → meinangebot → Lifecycle → clean**
   (удаляет старые MySQL-миграции из `target`).
4. Вверху выбрать конфигурацию **MainApplication (dev)** → ▶ Run.

### Вариант B: терминал

```bash
cd ~/IdeaProjects/meinangebot
mvn clean install -DskipTests
java -jar backend/target/angebotapp.jar --spring.profiles.active=dev
```

## 4. Проверить

- Сайт: http://localhost:8080
- API: http://localhost:8080/api/offers?retailer=aldi
- Вход в админку (`/login`): пользователь `admin`, пароль `admin123456`
  (создаётся автоматически в профиле `dev`).

В логе должно быть:

```
Migrating schema "public" to version "1 - init schema"
Started MainApplication in ... seconds
Bootstrap admin user 'admin' is available.
```

Предупреждение `Flyway upgrade recommended: PostgreSQL 16 ... is newer than this version of Flyway` можно игнорировать — всё работает.

---

## Настройки (переменные окружения)

Все значения по умолчанию подходят для локального запуска. Менять при необходимости
(в IntelliJ: Run Configuration → *Environment variables*).

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/angebot` | адрес базы |
| `DB_USERNAME` | `angebot` | пользователь БД |
| `DB_PASSWORD` | `angebot` | пароль БД |
| `SERVER_PORT` | `8080` | порт приложения |
| `APP_ADMIN_PASSWORD` | `admin123456` | пароль пользователя `admin` |
| `JPA_DDL_AUTO` | `validate` | не менять: схемой управляет Flyway |

## Частые ошибки

| Ошибка | Решение |
|---|---|
| `Connection refused ... localhost:5432` | PostgreSQL не запущен: `brew services start postgresql@16` |
| `password authentication failed for user "angebot"` / `role "angebot" does not exist` | повторить шаг 2 |
| `database "angebot" does not exist` | `createdb -O angebot angebot` |
| `Found more than one migration with version 1` | в `target` остались старые миграции: `mvn clean` (шаг 3) |
| `Schema-validation: missing table ...` | база создана не Flyway — пересоздать её (см. ниже) |
| `Port 8080 is already in use` | остановить другой процесс или задать `SERVER_PORT=8081` |
| `command not found: psql` | не выполнен PATH из шага 1 |

## Сбросить базу (начать с нуля)

```bash
dropdb angebot && createdb -O angebot angebot
```

При следующем старте Flyway заново создаст все таблицы.

---

## Сервер (wochenspar.de): переход с MySQL

Сервер запускается **без профиля** или с профилем `prod` и берёт доступ к БД
из переменных окружения.

1. Установить PostgreSQL 16 на сервере, создать пользователя и базу (как в шаге 2,
   но с надёжным паролем).
2. Задать переменные окружения для приложения:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/angebot
   DB_USERNAME=angebot
   DB_PASSWORD=<надёжный пароль>
   ```
3. Один раз запустить новую версию на **пустой** базе — Flyway создаст схему.
   Затем остановить приложение.
4. Перенести данные из MySQL с помощью [pgloader](https://pgloader.io)
   (`brew install pgloader` / `apt install pgloader`). Файл `migrate.load`:
   ```
   LOAD DATABASE
        FROM mysql://root:<mysql-пароль>@localhost/angebot
        INTO postgresql://angebot:<pg-пароль>@localhost/angebot
   WITH data only, truncate, disable triggers
   EXCLUDING TABLE NAMES MATCHING ~/_seq$/, 'flyway_schema_history';
   ```
   ```bash
   pgloader migrate.load
   ```
5. Выровнять счётчики ID:
   ```sql
   SELECT setval('aldi_seq',          COALESCE((SELECT MAX(id) FROM aldi), 0) + 50);
   SELECT setval('edeka_seq',         COALESCE((SELECT MAX(id) FROM edeka), 0) + 50);
   SELECT setval('netto_seq',         COALESCE((SELECT MAX(id) FROM netto), 0) + 50);
   SELECT setval('penny_seq',         COALESCE((SELECT MAX(id) FROM penny), 0) + 50);
   SELECT setval('product_maker_seq', COALESCE((SELECT MAX(id) FROM product_maker), 0) + 50);
   SELECT setval('selected_item_seq', COALESCE((SELECT MAX(id) FROM selected_item), 0) + 50);
   SELECT setval('common_gather_seq', COALESCE((SELECT MAX(id) FROM common_gather), 0) + 50);
   SELECT setval('userinfo_seq',      COALESCE((SELECT MAX(id) FROM userinfo), 0) + 50);
   SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 0) + 1, false);
   SELECT setval(pg_get_serial_sequence('role',  'id'), COALESCE((SELECT MAX(id) FROM role), 0) + 1, false);
   ```
6. Запустить приложение и проверить сайт. Старую MySQL-базу удалить только после проверки.

---

## Frontend (Angular 22)

Frontend собирается автоматически при `mvn clean install`: Maven сам скачивает
Node.js 24 в `frontend/target/node/` и выполняет `npm ci` и `npm run build`.

Для работы над интерфейсом с мгновенной перезагрузкой:

```bash
# 1. Backend запущен (MainApplication (dev), порт 8080)
# 2. В отдельном терминале:
cd frontend
npm ci          # один раз; нужен Node.js 22.22+ или 24.15+ (brew install node@24)
npm start       # http://localhost:4200, запросы к API проксируются на :8080
```

Если после обновления сборка frontend падает, удалите старые папки и соберите заново:

```bash
rm -rf frontend/node frontend/target frontend/node_modules frontend/dist frontend/.angular
mvn clean install -DskipTests
```
