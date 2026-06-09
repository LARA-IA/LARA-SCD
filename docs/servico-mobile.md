# 📱 Serviço Mobile — Camada de Serviços (React Native / Expo)

> **Projeto:** `scd-mobile/` | **Framework:** React Native + Expo SDK 54 | **HTTP Client:** Axios

---

## Visão Geral

O app mobile contém uma camada de serviços em TypeScript que encapsula todas as chamadas à API REST do backend. Cada serviço é responsável por um domínio específico.

---

## 📁 Estrutura dos Serviços

```
scd-mobile/services/
├── api.ts                   → Configuração base do Axios (interceptors, JWT)
├── authService.ts           → Autenticação (login, registro)
├── patientService.ts        → CRUD de pacientes
├── consultationService.ts   → Consultas médicas (CRUD + confirmação)
├── predictService.ts        → Predição/classificação (deprecated)
└── adminService.ts          → Dashboard + backup administrativo
```

---

## 🔧 Configuração Base — `api.ts`

Configura a instância Axios com:

| Configuração | Valor |
|---|---|
| **Base URL (Android)** | `http://192.168.1.4:8080/api` |
| **Base URL (iOS)** | `http://localhost:8080/api` |
| **Content-Type padrão** | `application/json` |
| **Timeout** | 30 segundos |

### Interceptors

**Request Interceptor:**
- Recupera o token JWT do `AsyncStorage`
- Adiciona header `Authorization: Bearer {token}` a todas as requisições

**Response Interceptor:**
- Se receber HTTP 401, remove `token` e `user` do `AsyncStorage` (logout automático)

---

## 🔐 authService — Serviço de Autenticação

**Arquivo:** `services/authService.ts`

### Interfaces

```typescript
interface LoginRequest {
    email: string;
    password: string;
}

interface LoginResponse {
    token: string;
    type: string;        // "Bearer"
    id: string;          // UUID do usuário
    nome: string;
    email: string;
    accessLevel: 'MANAGER' | 'DOCTOR';
}

interface DoctorRegisterRequest {
    nome: string;
    cpf: string;
    email: string;
    password: string;
    CRM: string;
}
```

### Métodos

| Método | Endpoint | Descrição |
|---|---|---|
| `login(credentials)` | `POST /auth/login` | Autentica e retorna JWT + dados do usuário |
| `registerDoctor(data)` | `POST /auth/register` | Cadastra um novo médico |

---

## 🏥 patientService — Serviço de Pacientes

**Arquivo:** `services/patientService.ts`

### Interfaces

```typescript
interface PatientRegisterRequest {
    nome: string;
    cpf: string;
    dataNascimento?: string;   // formato ISO (YYYY-MM-DD)
    sexo: string;              // "M" ou "F"
    termoConsentimentoIa?: boolean;
}

interface PatientResponse {
    id: string;
    nome: string;
    cpf: string;
    sexo: string;
    dataNascimento?: string;
    termoConsentimentoIa?: boolean;
    dataConsentimento?: string;
    criadoEm?: string;
    atualizadoEm?: string;
}
```

### Métodos

| Método | Endpoint | Descrição |
|---|---|---|
| `registerPatient(data)` | `POST /patient/register` | Cadastra novo paciente |
| `searchByCpf(cpf)` | `GET /patient/search?cpf={cpf}` | Busca paciente por CPF (retorna `null` se 404) |
| `getPatient(id)` | `GET /patient/{id}` | Busca paciente por ID |
| `listPatients()` | `GET /patient` | Lista pacientes do médico logado |

---

## 📋 consultationService — Serviço de Consultas

**Arquivo:** `services/consultationService.ts`

O serviço mais completo, com tipos detalhados para a consulta, imagens e predições.

### Interfaces Principais

```typescript
interface ConsultationRequest {
    patientId: string;
    localizacoes: string[];     // Array de Localizacao enum values
    images: {
        uri: string;            // URI local da imagem
        name: string;           // Nome do arquivo
        type: string;           // MIME type (ex: "image/jpeg")
    }[];
}

interface ConsultationResponse {
    id: string;
    patient: {
        id: string;
        nome: string;
        cpf: string;
        sexo: string;
        dataNascimento?: string;
        termoConsentimentoIa?: boolean;
    };
    doctor: { id: string; nome: string };
    finalDiagnosis?: string;
    confirmed: boolean;
    createdAt: string;
    updatedAt?: string;
    images: ImageInfo[];
}

interface ImageInfo {
    id: string;
    filePath: string;
    fileName: string;
    fileSize: number;
    contentType: string;
    localizacao?: string;
    finalDiagnosis?: string;
    confirmed: boolean;
    concordanciaIa?: boolean;
    statusProcessamentoIa?: string;   // "PENDENTE" | "CONCLUIDO" | "FALHA"
    lesionId?: string;
    predictions: AiPredictionInfo[];
}

interface AiPredictionInfo {
    id: string;
    versaoModelo: string;
    classeInferida?: string;     // "benigno" ou "maligno"
    confianca?: number;          // 0.0 a 1.0
    multClasse?: string;         // ex: "mel", "nv", "bcc"
    confiancaMultClasse?: number;
    criadoEm?: string;
}
```

### Enums e Labels

#### DoctorVerdict

| Valor | Label (PT-BR) |
|---|---|
| `AGUARDANDO_BIOPSIA` | Aguardando Biópsia |
| `MELANOMA` | Melanoma |
| `CARCINOMA_BASOCELULAR` | Carcinoma Basocelular |
| `CARCINOMA_ESPINOCELULAR` | Carcinoma Espinocelular |
| `QUERATOSE_ACTINICA` | Queratose Actínica |
| `NEVO` | Nevo |

#### Localizacao

| Valor | Label (PT-BR) |
|---|---|
| `CABECA` | Cabeça |
| `PESCOCO` | Pescoço |
| `TRONCO` | Tronco |
| `BRACO_DIREITO` | Braço Direito |
| `BRACO_ESQUERDO` | Braço Esquerdo |
| `MAO_DIREITA` | Mão Direita |
| `MAO_ESQUERDA` | Mão Esquerda |
| `PERNA_DIREITA` | Perna Direita |
| `PERNA_ESQUERDA` | Perna Esquerda |
| `PE_DIREITO` | Pé Direito |
| `PE_ESQUERDO` | Pé Esquerdo |
| `COSTAS` | Costas |
| `ABDOMEN` | Abdômen |

### Métodos

| Método | Endpoint | Descrição |
|---|---|---|
| `createConsultation(data)` | `POST /medico/consultations` | Cria consulta com upload de imagens (multipart, timeout: 120s) |
| `listConsultations(nome?, cpf?)` | `GET /medico/consultations` | Lista consultas com filtros opcionais |
| `getConsultation(id)` | `GET /medico/consultations/{id}` | Obtém consulta por ID |
| `confirmConsultationDiagnosis(id, verdict)` | `PUT /medico/consultations/{id}/confirm` | Confirma diagnóstico da consulta |
| `confirmImageDiagnosis(imageId, verdict)` | `PUT /medico/consultations/images/{imageId}/confirm` | Confirma diagnóstico de imagem individual |

### Upload de Imagens — Compatibilidade Multiplataforma

O `createConsultation` tem tratamento especial para **Web** vs **Mobile**:

- **Web:** Converte URI em Blob, depois em File, e appenda ao FormData
- **Mobile (Android/iOS):** Appenda diretamente o objeto `{ uri, name, type }` ao FormData

---

## 🤖 predictService — Serviço de Predição (Deprecated)

**Arquivo:** `services/predictService.ts`

> ⚠️ **Deprecated:** Use `patientService` + `consultationService` no fluxo separado.

### Métodos (legados)

| Método | Endpoint | Descrição |
|---|---|---|
| `registerPatient(data)` | `POST /patient/register` | Cadastro de paciente (delegado) |
| `confirmDiagnosis(imageId, verdict)` | `PUT /medico/consultations/images/{imageId}/confirm` | Confirma diagnóstico |

---

## 🏢 adminService — Serviço Administrativo

**Arquivo:** `services/adminService.ts`

### Interfaces

```typescript
interface DashboardResponse {
    totalPatients: number;
    totalDoctors: number;
    totalImages: number;
    totalPredictions: number;
    totalLesions: number;
}

interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
}
```

### Métodos

| Método | Endpoint | Descrição |
|---|---|---|
| `getDashboard()` | `GET /admin/dashboard` | Retorna estatísticas do sistema |
| `changePassword(data)` | `POST /admin/change-password` | Altera senha do admin |
| `downloadBackup()` | — | Retorna a URL para download do backup ZIP |

> **Nota:** O `downloadBackup()` retorna a URL completa (`{baseURL}/admin/backup`) para ser usada com download nativo do dispositivo, não faz a requisição diretamente.
