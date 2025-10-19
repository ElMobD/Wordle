# 🧩 Wordle+

## 🎯 Description du projet

**Wordle+** est une version modernisée et multijoueur du célèbre jeu Wordle.  
Développée dans le cadre d’un projet étudiant, elle propose une expérience personnalisable, sociale et évolutive.

### 🚀 Fonctionnalités principales

- 🎮 **Mode Wordle classique** (lettres colorées)
- 👥 **Sessions entre amis** en temps réel
- ⚙️ **Paramètres personnalisables** : langue, longueur des mots, nombre d’essais, etc.
- 🌐 **Authentification via Google**
- 🌗 **Mode sombre / clair**
- 🌍 **Support multi-langue** avec base de mots dynamique

---

## 🏗️ Architecture du projet

```
Wordle/
├── backend/          → API Spring Boot (Java)
├── frontend/         → Application Vue.js (Vite + Tailwind)
├── db/               → Base de données PostgreSQL + scripts Docker
│   ├── docker-compose.yml
│   └── initdb/
│       └── V1__schema.sql
├── docs/             → Documentation (schémas, maquettes…)
└── README.md
```

---

## ⚙️ Technologies utilisées

| Domaine | Technologie |
|----------|--------------|
| **Frontend** | Vue.js + Vite + TailwindCSS |
| **Backend** | Java Spring Boot |
| **Base de données** | PostgreSQL 16 |
| **Interface DB** | pgAdmin4 |
| **Infrastructure** | Docker + Docker Compose |
| **Authentification** | Google OAuth 2.0 |
| **Stockage dynamique** | JSONB |
| **Horodatage universel** | TIMESTAMPTZ |

---

## 🧰 Installation & Lancement

### 🐋 Prérequis

- Docker Desktop (Windows / macOS)
- Node.js ≥ 20
- Java ≥ 17
- Git

---

### 1️⃣ Cloner le projet

```bash
git clone https://github.com/<ton-repo>/Wordle.git
cd Wordle
```

---

### 2️⃣ Lancer la base de données (Docker)

1. Ouvre un terminal dans le dossier `db/` :
   ```bash
   cd db
   ```

2. Lance les conteneurs :
   ```bash
   docker compose up -d
   ```

3. Vérifie :
   ```bash
   docker compose ps
   ```
   Tu devrais voir :
   ```
   local_postgres   Up (healthy)
   pgadmin          Up
   ```

4. Accède à pgAdmin :
   - 🌍 [http://localhost:5050](http://localhost:5050)
   - **Email** → `admin@local.com`
   - **Password** → `admin`

5. Connecte le serveur Postgres :
   - **Host name / address** → `db`
   - **Port** → `5432`
   - **Username** → `app`
   - **Password** → `app`
   - **Database** → `app_dev`

---

### 3️⃣ Vérifier la structure SQL

Le script d’initialisation est ici :
```
db/initdb/V1__schema.sql
```

Lors du premier démarrage, Postgres exécute automatiquement tous les fichiers `.sql` du dossier `initdb/`.

Si tu modifies la structure de la base, réinitialise le conteneur :
```bash
docker compose down -v
docker compose up -d
```

---

### 4️⃣ Lancer le frontend

```bash
cd frontend
npm install
npm run dev
```

- 🌐 Application → [http://localhost:5173](http://localhost:5173)

---

### 5️⃣ Lancer le backend

```bash
cd backend
./mvnw spring-boot:run
```

- API → [http://localhost:8080](http://localhost:8080)

---

## 🗄️ Structure de la base de données

### Tables principales

| Table | Description |
|--------|--------------|
| **users** | Comptes utilisateurs (connexion Google) |
| **user_settings** | Préférences de chaque joueur |
| **languages** | Langues disponibles |
| **lexicon** | Mots jouables par langue |
| **sessions** | Sessions multijoueur |
| **session_settings** | Paramètres d’une session |
| **session_participants** | Participants à chaque session |
| **rounds** | Manches de jeu |
| **games** | Parties d’un joueur pour un round |
| **guesses** | Tentatives de mots pendant un round |

> 💡 Tous les timestamps sont gérés avec `TIMESTAMPTZ` (UTC),  
> et certaines colonnes utilisent `JSONB` pour stocker des données flexibles (ex. paramètres additionnels).

---

## 🤝 Collaboration & Workflow Git

### Branches

- `main` → version stable  
- `dev` → développement principal  
- `feature/<nom>` → branche pour une nouvelle fonctionnalité

### Workflow

1. Crée une branche depuis `dev`
   ```bash
   git checkout dev
   git checkout -b feature/connexion-google
   ```
2. Développe, commit puis push :
   ```bash
   git add .
   git commit -m "feat: connexion Google"
   git push origin feature/connexion-google
   ```
3. Crée une **Pull Request** vers `dev` et demande une revue.

---

## 🔧 Commandes utiles Docker

| Action | Commande |
|--------|-----------|
| Démarrer la base | `docker compose up -d` |
| Arrêter les conteneurs | `docker compose down` |
| Réinitialiser la base | `docker compose down -v && docker compose up -d` |
| Voir les logs | `docker compose logs -f db` |
| Shell SQL | `docker exec -it local_postgres psql -U app -d app_dev` |

---

## 📚 Ressources

- [Documentation PostgreSQL](https://www.postgresql.org/docs/)
- [Documentation Docker Compose](https://docs.docker.com/compose/)
- [pgAdmin](https://www.pgadmin.org/)
- [Vue.js](https://vuejs.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [OAuth 2.0 Google](https://developers.google.com/identity/protocols/oauth2)

---

## 👥 Équipe

| Nom | Rôle |
|------|------|
| 🧑‍💻 Toi | Chef de projet / Back-end |
| 🎨 Collègue 1 | Front-end (Vue.js) |
| 🧱 Collègue 2 | Base de données / DevOps |
| 💅 Collègue 3 | UI / UX |

---

## 🧠 Notes techniques

- PostgreSQL initialise la base **seulement lors de la première création du volume**.  
- Le script `initdb/*.sql` est **automatiquement exécuté** si la base est vide.  
- `jsonb` → stockage flexible pour options.  
- `timestamptz` → gestion automatique des fuseaux horaires (stockage UTC).  

---

## 🏁 Démarrage rapide

```bash
# 1. Lancer la base
cd db && docker compose up -d

# 2. Lancer le frontend
cd ../frontend && npm install && npm run dev

# 3. Lancer le backend
cd ../backend && ./mvnw spring-boot:run
```

Et c’est parti 🎉  
Connecte-toi à [pgAdmin](http://localhost:5050) ou joue sur [http://localhost:5173](http://localhost:5173) !

---

> 📄 Auteur : **Équipe Wordle+** — Projet étudiant 2025  
> Licence : Libre pour usage pédagogique
