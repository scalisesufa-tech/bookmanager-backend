# BookManager – Backend con Spring Boot, OAuth2 e Cloud Run

## Descrizione del progetto
BookManager è un'applicazione **backend** sviluppata in **Java 17** con **Spring Boot 3.2**, progettata per la gestione di un archivio di libri.  
L’app espone un set di API REST per operazioni CRUD (Create, Read, Update, Delete) sull’entità `Book` (id, titolo, autore, ISBN).  
Il progetto è stato realizzato come esercitazione per il Master in Cloud Computing con l’obiettivo di comprendere:  
- sviluppo backend con Spring Boot,  
- containerizzazione con Docker,  
- deployment su Google Cloud Run,  
- utilizzo di un database gestito (Cloud SQL con PostgreSQL),  
- autenticazione sicura con OAuth2 (Google).  

## Tecnologie principali
- Java 17  
- Spring Boot 3.2  
- PostgreSQL 17 (Cloud SQL)  
- Docker + Docker Compose  
- Google Cloud Run  
- OAuth2 (Google Identity)  

## API REST principali
- `GET /books` → lista di tutti i libri  
- `GET /books/{id}` → recupera un libro per ID  
- `POST /books` → aggiunge un nuovo libro  
- `PUT /books/{id}` → aggiorna un libro esistente  
- `DELETE /books/{id}` → elimina un libro  

Documentazione interattiva disponibile via **Swagger UI**:  
- Locale: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- Cloud: `https://bookmanager-817624605910.europe-west8.run.app/swagger-ui/index.html#/`  

## Configurazione variabili d’ambiente
Il progetto utilizza un file `.env` (da creare a partire da `.env.example`).  
Di seguito le variabili principali:

| Variabile              | Descrizione |
|-------------------------|-------------|
| `DB_HOST`              | Host del database PostgreSQL |
| `DB_NAME`              | Nome del database |
| `DB_USER`              | Utente del database |
| `DB_PASS`              | Password del database |
| `GOOGLE_CLIENT_ID`     | Client ID per autenticazione OAuth2 Google |
| `GOOGLE_CLIENT_SECRET` | Secret per OAuth2 Google |
| `SPRING_PROFILES_ACTIVE` | Profilo Spring Boot (es: `prod`, `dev`) |

## Avvio in locale (Docker)
```bash
cp .env.example .env
# inserisci client id/secret e variabili DB locali
docker compose up --build
```

## Deploy su Google Cloud Run
### Prerequisiti
- gcloud installato e autenticato (`gcloud init`) e (`gcloud auth login`) 
- Abilitare le API necessarie:  
```bash
gcloud services enable run.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com sqladmin.googleapis.com
```

### 1) Build e push immagine
```bash
gcloud artifacts repositories create apps --repository-format=docker --location=REGION --description="App containers" || true
gcloud auth configure-docker REGION-docker.pkg.dev
gcloud builds submit --tag REGION-docker.pkg.dev/PROJECT_ID/apps/bookmanager:$(date +%Y%m%d-%H%M%S)
```

### 2) Deploy iniziale (per ottenere URL)
```bash
gcloud run deploy bookmanager   --image REGION-docker.pkg.dev/PROJECT_ID/apps/bookmanager:TAG   --region REGION   --allow-unauthenticated   --set-env-vars=SPRING_PROFILES_ACTIVE=prod,GOOGLE_CLIENT_ID=...,GOOGLE_CLIENT_SECRET=...
```

### 3) Configurazione Cloud SQL (PostgreSQL) + redeploy
- Crea istanza/DB/utente (vedi documentazione Cloud SQL).  
- Redeploy collegando l’istanza:  
```bash
gcloud run deploy bookmanager   --image REGION-docker.pkg.dev/PROJECT_ID/apps/bookmanager:TAG   --region REGION   --set-env-vars=SPRING_PROFILES_ACTIVE=prod,GOOGLE_CLIENT_ID=...,GOOGLE_CLIENT_SECRET=...,DB_HOST=/cloudsql/INSTANCE_CONNECTION_NAME,DB_NAME=...,DB_USER=...,DB_PASS=...
```

## Testing
- **Swagger UI** per test API direttamente dal browser, raggiungibili al seguente URL: https://bookmanager-817624605910.europe-west8.run.app/swagger-ui/index.html#/ 
- **Postman** per verificare endpoint REST.
- **Pagina html** pagina di index.html creata per un testing semplificato con una UI minimale, raggiungibile al seguente URL: https://bookmanager-817624605910.europe-west8.run.app.  
- Unit test inclusi per operazioni CRUD di base.  

## Link utili
- [Spring Boot Docs](https://spring.io/projects/spring-boot)  
- [Docker Docs](https://docs.docker.com/)  
- [Google Cloud Run Docs](https://cloud.google.com/run)  
- [PostgreSQL Docs](https://www.postgresql.org/docs/)  
