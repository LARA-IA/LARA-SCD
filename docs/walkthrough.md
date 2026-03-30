# LARA-SCD — Project Walkthrough

## Overview

**LARA-SCD (Skin Cancer Detection)** is a clinical decision support system that uses AI-powered image classification to assist dermatologists in diagnosing skin lesions. It follows a **Human-in-the-Loop** methodology — AI results are suggestions that must be confirmed by a medical professional.

---

## Architecture

```mermaid
graph LR
  Mobile["scd-mobile<br/>(Expo/React Native)"] -->|REST API| Backend["scd<br/>(Spring Boot)"]
  Backend -->|HTTP + Image| AI["api-scd<br/>(FastAPI/Python)"]
  Backend -->|MySQL| DB[(MySQL 8.2)]
```

The system is composed of **3 decoupled services**:

| Component | Stack | Port | Purpose |
|---|---|---|---|
| `scd` | Java 21, Spring Boot 3.5.7 | 8080 | Core backend — auth, CRUD, business rules |
| `api-scd` | Python 3.12, FastAPI | 8081 | AI inference — image classification |
| `scd-mobile` | TypeScript, Expo/React Native | — | Mobile client for doctors & admins |

---

## Component Details

### 1. `api-scd` — AI Prediction Service

**Entry point:** [main.py](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/api-scd/main.py)

- **Framework:** FastAPI + Uvicorn
- **Endpoint:** `POST /predict/` — receives an image + metadata (age, sex, location)
- **Response:** JSON with `predictions[]` and `model_version` (for model traceability)
- **Classification pipeline** (hierarchical, top-down):
  1. **Binary model** → `benigno` or `maligno`
  2. **Multiclass model** → subtype based on binary result:
     - Benign: `nv`, `bkl`, `df`, `vasc`
     - Malignant: `mel`, `bcc`, `akiec`
- **Current state:** Models are **simulated** (hardcoded results). Real YOLO model integration is marked with `TODO` comments.

Key files:
- [controller_predict.py](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/api-scd/controller/controller_predict.py) — route handler
- [predict.py](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/api-scd/service/predict.py) — prediction logic (simulated)

---

### 2. `scd` — Java/Spring Boot Backend

**Entry point:** [ScdApplication.java](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd/src/main/java/com/lara/scd/ScdApplication.java)

Organized by **domain modules**, each with `application` (controllers/DTOs) and `domain` (models/repos/services) layers:

| Module | Description |
|---|---|
| `consultation` | Consultation lifecycle (create, list, confirm diagnosis) |
| `doctor` | Doctor registration and management |
| `patient` | Patient CRUD (**decoupled from consultation**), images, verdicts |
| `lesion` | Physical lesion tracking across consultations (temporal evolution) |
| `predict` | AI prediction versioning + proxy to the Python AI API |
| `manager` | Admin/manager operations, dashboard (5 metrics) |
| `user` | Authentication (login, JWT) |
| `shared` | File storage, static file serving |
| `config/security` | JWT filter, Spring Security config |

> [!NOTE]
> **DTOs on all controllers**: No JPA entity is returned directly. Each controller uses record or class DTOs with `from()` factory method. Repositories follow Java convention (no `I` prefix).

**Key dependencies:** Spring Data JPA, Spring Security, JWT (jjwt + java-jwt), WebFlux (for async HTTP to AI API), Lombok, SpringDoc (Swagger), MySQL, H2 (test).

**Infrastructure:** Docker Compose spins up MySQL 8.2 + the Spring Boot app on a shared `redescd` bridge network.

---

#### Database Schema (Entity Relationship)

```mermaid
erDiagram
    Patient ||--o{ Consultation : has
    Patient ||--o{ Lesion : has
    Doctor ||--o{ Patient : manages
    Doctor ||--o{ Consultation : conducts
    Consultation ||--o{ PatientImage : contains
    Lesion ||--o{ PatientImage : tracks
    PatientImage ||--o{ AiPrediction : evaluated_by

    Patient {
        UUID id PK
        String nome
        String cpf
        String sexo
        Date dataNascimento
        Boolean termoConsentimentoIa
        DateTime dataConsentimento
        DateTime dataRevogacaoConsentimento
        DateTime criadoEm
        DateTime atualizadoEm
    }

    Lesion {
        UUID id PK
        UUID patient_id FK
        Enum localizacaoAnatomica
        String descricao
        DateTime criadoEm
    }

    PatientImage {
        UUID id PK
        UUID consultation_id FK
        UUID patient_id FK
        UUID lesion_id FK
        String filePath
        Enum localizacao
        Enum doctorVerdict
        Boolean confirmed
        Boolean concordanciaIa
        Enum statusProcessamentoIa
        DateTime criadoEm
        DateTime atualizadoEm
    }

    AiPrediction {
        UUID id PK
        UUID patient_image_id FK
        String versaoModelo
        String classeInferida
        Double confianca
        String multClasse
        Double confiancaMultClasse
        Text probabilidadesJson
        DateTime criadoEm
    }
```

---

### 3. `scd-mobile` — Mobile Frontend

**Framework:** Expo (React Native) with TypeScript and file-based routing.

| Directory | Contents |
|---|---|
| `app/` | Screens: [login.tsx](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/app/login.tsx), [register.tsx](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/app/register.tsx), [index.tsx](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/app/index.tsx), tabs ([admin.tsx](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/app/%28tabs%29/admin.tsx), [medico.tsx](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/app/%28tabs%29/medico.tsx)) |
| `services/` | [api.ts](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/services/api.ts), [authService.ts](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/services/authService.ts), [consultationService.ts](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/services/consultationService.ts), **[patientService.ts](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/services/patientService.ts)** (decoupled patient CRUD), [adminService.ts](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/services/adminService.ts), [predictService.ts](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/services/predictService.ts) (deprecated) |
| `contexts/` | [AuthContext.tsx](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/scd-mobile/contexts/AuthContext.tsx) — authentication state and role-based navigation |
| `components/` | Reusable UI components |
| `constants/` | Theme configuration |

---

## Data Flow

### Patient Registration (decoupled from consultation)
```mermaid
sequenceDiagram
  participant D as Doctor (Mobile)
  participant B as Backend (Java)
  
  D->>B: POST /api/patient/register (nome, cpf, sexo, termoConsentimentoIa)
  B->>B: Validate unique CPF, record LGPD consent
  B-->>D: PatientResponseDto (id, nome, cpf, timestamps)
```

### Skin Lesion Diagnosis
```mermaid
sequenceDiagram
  participant D as Doctor (Mobile)
  participant B as Backend (Java)
  participant AI as AI API (Python)
  
  D->>B: Search patient by CPF (GET /api/patient/search)
  B-->>D: PatientResponseDto
  D->>B: Upload image + patientId (POST /api/medico/consultations)
  B->>B: Fetch patient by ID (404 if not found)
  B->>B: Create consultation, store image
  B->>B: Find/create Lesion (by patient + location)
  B->>B: Save PatientImage (status: PENDENTE)
  B->>AI: POST /predict/ (image + metadata)
  AI->>AI: Run classification pipeline
  AI-->>B: JSON {predictions[], model_version}
  B->>B: Create AiPrediction record (versioned)
  B->>B: Update status → CONCLUIDO or FALHA
  B-->>D: ConsultationResponse (DTOs, not entities)
  D->>B: Confirm/override diagnosis (Doctor Verdict)
  B->>B: Calculate concordanciaIa (agree with AI?)
  B->>B: Mark image as confirmed
```

---

## Key Observations

> [!IMPORTANT]
> The AI models are currently **simulated** — [predict.py](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/api-scd/service/predict.py) returns hardcoded values. Real YOLO model files (`.pt`) need to be trained and placed in a `models/` directory.

> [!NOTE]
> The system uses **two JWT libraries** (`jjwt` + `java-jwt`). Consider consolidating to one.

> [!TIP]
> The [api-scd/requirements.txt](file:///c:/Users/Jair%20Victor/Documents/LARA-SCD/api-scd/requirements.txt) does **not** include `ultralytics` (YOLO). It will need to be added when real models are integrated.

> [!NOTE]
> AI predictions are now **versioned** in the `ai_predictions` table. A single image can be re-evaluated by newer models without losing historical data. The `concordancia_ia` field on each image tracks whether the doctor agreed with the AI's binary classification (malignant/benign).

> [!NOTE]
> Patient registration is **decoupled** from consultation creation. The doctor first registers the patient (with LGPD consent), then creates consultations linking by `patientId`.

