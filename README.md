# BookManager – Google OAuth + Cloud Run + Cloud SQL

## Local (Docker)
```bash
cp .env.example .env
# inserisci client id/secret e (se vuoi) variabili DB locali
docker compose up --build
```

## Deploy su Google Cloud Run
Prerequisiti:
- gcloud installato e autenticato (`gcloud init`)
- Abilita API: `gcloud services enable run.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com sqladmin.googleapis.com`

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
A fine deploy avrai l'URL tipo: `https://bookmanager-xxxxx-uc.a.run.app`.

Aggiungi questo redirect URI nel client OAuth:
```
https://bookmanager-xxxxx-uc.a.run.app/login/oauth2/code/google
```

### 3) Cloud SQL (PostgreSQL) + redeploy con DB
- Crea istanza/DB/utente (vedi documentazione Google Cloud SQL).
- Redeploy collegando l'istanza:
```bash
gcloud run deploy bookmanager   --image REGION-docker.pkg.dev/PROJECT_ID/apps/bookmanager:TAG   --region REGION   --allow-unauthenticated   --add-cloudsql-instances PROJECT_ID:REGION:bookmanager-pg   --set-env-vars=SPRING_PROFILES_ACTIVE=prod,GOOGLE_CLIENT_ID=...,GOOGLE_CLIENT_SECRET=...,DB_INSTANCE_CONNECTION_NAME=PROJECT_ID:REGION:bookmanager-pg,DB_NAME=bookdb,DB_USER=appuser,DB_PASSWORD=...
```


Workflow CI per Cloud Run in `.github/workflows/cloudrun.yml`.
