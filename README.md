# CampConnect Backend

Spring Boot 3 + MySQL + Docker

## Stack

- Java 17
- Spring Boot 3.4.12
- Spring Data JPA
- MySQL 8
- Docker and Docker Compose
- Swagger (OpenAPI)

## Run Locally

Requirements:

- Java 17
- Maven
- MySQL running locally

Check `src/main/resources/application.properties` and make sure the local database settings match your machine.

Common local setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campconnect?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

Start the app:

```bash
mvn spring-boot:run
```

Or run `CampConnectApplication` from your IDE.

Useful URLs:

- Backend: `http://localhost:8082/api`
- Swagger UI: `http://localhost:8082/api/swagger-ui.html`

## Run With Docker

Requirements:

- Docker
- Docker Compose

From the project root:

```bash
docker compose up --build -d
```

Services:

- Backend: `http://localhost:8082/api`
- Swagger UI: `http://localhost:8082/api/swagger-ui.html`
- MySQL: `localhost:3307`
- phpMyAdmin: `http://localhost:8081`

Default Docker database credentials:

- Database: `campconnect`
- Username: `root`
- Password: `root`

Stop containers:

```bash
docker compose down
```

Remove containers and the database volume:

```bash
docker compose down -v
```

## Database Scripts

Database helper scripts now live under `database/` instead of the repo root.

Recommended shared dataset:

- `database/seeds/seed_tunisia_events_reservations.sql`

See `database/README.md` for the team workflow, seed options, maintenance scripts, and notes about archived SQL files.

## Recommended Package Structure

```text
com.esprit.campconnect
|-- InscriptionSite
|   |-- controller
|   |   `-- InscriptionSiteController
|   |-- entity
|   |   |-- InscriptionSite
|   |   `-- StatutInscription
|   |-- repository
|   |   `-- InscriptionSiteRepository
|   `-- service
|       |-- IInscriptionSiteService
|       `-- InscriptionSiteServiceImp
`-- CampConnectApplication
```
