# PPBanking — Spring Boot + H2 + JWT

A minimal internet banking API as a pet project. Supports registration, JWT login, **USER/ADMIN** roles, deposits, withdrawals, transfers, and transaction history. The database is **H2** in file mode (data persists across restarts).

## Features
- Registration & login (JWT: access + refresh)
- Roles: **USER**, **ADMIN**
- Balance & transaction history
- Deposit, withdraw, transfer between users
- H2 Console to inspect the DB

## Tech Stack
- Java 17+, Spring Boot 3.x
- Spring Web, Spring Security 6, Spring Data JPA, Validation, Lombok
- H2 Database (file mode)
- JWT (jjwt)
- Maven

## Requirements
- Java 17+
- Maven 3.8+ (if you don't start from IntelliJ)

## Quick Start
1. Open the project in IntelliJ IDEA (or clone and open).
2. Ensure you have `src/main/resources/application.yml` configured (see example below).
3. Run the app:
   - **IntelliJ:** run the class with `@SpringBootApplication` (e.g., `PpbankingApplication`).  
   - **Maven:** from project root run:
     ```bash
     mvn spring-boot:run
     ```

### H2 Console
- URL: `http://localhost:8080/h2-console`
- JDBC: `jdbc:h2:file:./data/bankdb`
- User: `sa`
- Password: *(empty)*

### Example `application.yml`
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/bankdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

app:
  jwt:
    secret: changeThisSuperSecretKey1234567890
    accessMillis: 900000      # 15 minutes
    refreshMillis: 604800000  # 7 days
```

## Roles & Access Control
- The `User` entity contains a string `role` field: `USER` or `ADMIN`.
- **Admin bootstrap:** when the `users` table is empty, you can call `POST /auth/register-admin` **without a token** once to create the very first user (usually an `ADMIN`).  
- After at least one user exists:
  - `POST /auth/register-admin` requires an `ADMIN` access token.
  - Public registration `POST /auth/register` always creates a `USER`.

In `SecurityConfig`:
- `/admin/**` — requires `ADMIN`
- `/auth/register-admin` — requires `ADMIN` (except the bootstrap case)
- `/auth/login`, `/auth/refresh`, `/auth/register`, `/h2-console/**` — public

## API Reference
### Auth
#### `POST /auth/register`  (public → USER)
```json
{ "username": "kate", "password": "pass123" }
```

#### `POST /auth/register-admin`  (bootstrap w/o token; later requires ADMIN)
```json
{ "username": "maks", "password": "pass123", "role": "ADMIN" }
```
`role` is optional; default is `"ADMIN"`.

#### `POST /auth/login`
```json
{ "username": "maks", "password": "pass123" }
```
Response:
```json
{ "accessToken": "...", "refreshToken": "..." }
```

#### `POST /auth/refresh`
```json
{ "refreshToken": "..." }
```
Response:
```json
{ "accessToken": "...", "refreshToken": "..." }
```

### Account (requires `Authorization: Bearer <accessToken>`)
#### `GET /account` — current balance & history
Response:
```json
{ "balance": 650.0, "history": [ /* array of transactions */ ] }
```

#### `POST /account/deposit`
```json
{ "amount": 250 }
```

#### `POST /account/withdraw`
```json
{ "amount": 50 }
```

#### `POST /account/transfer`
```json
{ "toUserId": 2, "amount": 100 }
```

## Postman
Ready-to-use files:
- Collection: `PPBanking.postman_collection.json`
- Environment: `PPBanking.local.postman_environment.json`

### How to use
1. Import both files into Postman.
2. Select environment **PPBanking Local (H2)**.
3. Fresh DB scenario:
   1) **Admin → Register Admin (Bootstrap - no auth)**  
   2) **Admin → Login Admin** (stores `adminAccessToken`)  
   3) **Admin → Register User via Admin (Protected)** *or* **User → Register User (Public)**  
   4) **User → Login User** (stores `userAccessToken`)  
   5) **User → Deposit (User)** → **User → Account (User)**  
   6) Optional **User → Transfer (User → toUserId)** → **User → Account (User)**  
   7) Optional **Admin → Deposit (Admin)** → **Admin → Account (Admin)**

## Build a JAR
```bash
mvn clean package
```
Run:
```bash
java -jar target/ppbanking-*.jar
```

## Project Structure (main packages)
```
com.example.ppbanking
├── domain            // Entities: User, Transaction
├── dto               // DTOs: RegisterRequest, LoginRequest, AuthResponse, etc.
├── repo              // Spring Data JPA repositories
├── security          // JwtService, JwtAuthFilter, AppUserDetailsService, SecurityConfig
├── service           // AccountService (registration, balance, operations)
└── web               // REST controllers: AuthController, AccountController, AdminController (optional)
```

## Common Issues
- **403 on /auth/register-admin** after first run — you need an `ADMIN` token (bootstrap already used).
- **401 on /account/** — ensure `Authorization: Bearer <accessToken>` header is present.
- **Ambiguous `authorities` in UserBuilder** — pass roles explicitly: `.roles(u.getRole())` or use `authorities(Collections.emptyList())`.
- **H2 locks or errors** — stop the app and delete `./data/bankdb*` files; restart (this wipes data).

## Roadmap
- PostgreSQL profile
- Flyway migrations
- Minimal React UI
- WebSocket balance updates
- Transaction categories & reports
