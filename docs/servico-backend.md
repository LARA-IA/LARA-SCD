# 🖥️ Serviço Backend — API REST (Spring Boot)

> **Projeto:** `scd/` | **Porta:** `8080` | **Base URL:** `/api`

---

## 📦 Pacotes e Organização

O backend segue uma arquitetura **DDD (Domain-Driven Design)** com separação por domínios:

```
com.lara.scd/
├── config/
│   ├── docs/          → SpringDocOpenApiConfig
│   └── security/      → SecurityConfig, JwtFilter, JwtUtil, SecurityContext
├── consultation/
│   ├── application/   → ConsultationController, DTOs
│   └── domain/        → Consultation (model), ConsultationRepository, ConsultationService
├── doctor/
│   ├── application/   → DoctorController, DTOs
│   └── domain/        → Doctor (model), DoctorRepository, DoctorService
├── patient/
│   ├── application/   → PatientController, DTOs
│   └── domain/        → Patient, PatientImage, Enums, Repositories, PatientService
├── predict/
│   ├── application/   → PredictController, DTOs
│   └── domain/        → AiPrediction (model), AiPredictionRepository, PredictService
├── manager/
│   ├── application/   → ManagerController, DTOs
│   └── domain/        → Manager (model), ManagerRepository, ManagerService
├── user/
│   ├── application/   → AuthController, DTOs
│   └── domain/        → User (abstract), AccessLevel, UserRepository, UserService
├── lesion/
│   └── domain/        → Lesion (model), LesionRepository
├── shared/
│   ├── application/   → FileController
│   └── service/       → FileStorageService
├── exception/         → UnicidadeVioladaException
└── handler/           → DefaultExceptionHandler, ValidationExceptionHandler, ErrorResponse
```

---

## 🔐 Serviço de Autenticação (UserService)

**Classe:** `com.lara.scd.user.domain.service.UserService`

Responsável pela autenticação de usuários via JWT.

### Métodos

#### `login(LoginRequestDto request) → LoginResponseDto`

Autentica um usuário com email e senha, retornando um token JWT válido.

| Campo Request | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `email` | `String` | ✅ | Email do usuário |
| `password` | `String` | ✅ | Senha do usuário |

| Campo Response | Tipo | Descrição |
|---|---|---|
| `type` | `String` | Tipo do token (`"Bearer"`) |
| `token` | `String` | Token JWT |
| `id` | `UUID` | ID do usuário |
| `nome` | `String` | Nome do usuário |
| `email` | `String` | Email do usuário |
| `accessLevel` | `AccessLevel` | Nível de acesso (`DOCTOR` ou `MANAGER`) |

**Endpoint:** `POST /api/auth/login`  
**Acesso:** Público (sem autenticação)

---

## 👨‍⚕️ Serviço de Médico (DoctorService)

**Classe:** `com.lara.scd.doctor.domain.service.DoctorService`

Gerencia o registro de novos médicos no sistema.

### Métodos

#### `registerDoctor(DoctorRegisterRequestDto dto) → void`

Registra um novo médico na plataforma. A senha é criptografada com BCrypt antes do armazenamento.

| Campo Request | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | `String` | ✅ | Nome completo do médico |
| `cpf` | `String` | ✅ | CPF (único) |
| `email` | `String` | ✅ | Email (único) |
| `password` | `String` | ✅ | Senha (será criptografada) |
| `CRM` | `String` | ✅ | Número do CRM |

**Endpoint:** `POST /api/auth/register` ou `POST /api/doctor/register`  
**Acesso:** Público (sem autenticação)

**Exceções:**
- `UnicidadeVioladaException` — Se email já cadastrado (HTTP 409)

---

## 🏥 Serviço de Paciente (PatientService)

**Classe:** `com.lara.scd.patient.domain.service.PatientService`

Gerencia o CRUD de pacientes e a confirmação de diagnósticos.

### Métodos

#### `registerPatient(PatientRegisterRequestDto dto) → Patient`

Cadastra um novo paciente vinculado ao médico autenticado.

| Campo Request | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | `String` | ✅ | Nome completo do paciente |
| `cpf` | `String` | ✅ | CPF (único) |
| `dataNascimento` | `LocalDate` | ❌ | Data de nascimento |
| `sexo` | `String` | ✅ | Sexo (`"M"` ou `"F"`) |
| `termoConsentimentoIa` | `Boolean` | ❌ | Consentimento LGPD para uso da IA |

**Endpoint:** `POST /api/patient/register`  
**Acesso:** Autenticado (DOCTOR, MANAGER)

**Regras de negócio:**
- CPF deve ser único — retorna HTTP 409 se duplicado
- Se consentimento LGPD for `true`, registra a data de consentimento
- Vincula automaticamente ao médico logado

---

#### `findByCpf(String cpf) → Optional<Patient>`

Busca paciente por CPF.

**Endpoint:** `GET /api/patient/search?cpf={cpf}`  
**Acesso:** Autenticado

---

#### `findById(UUID id) → Patient`

Busca paciente por ID.

**Endpoint:** `GET /api/patient/{id}`  
**Acesso:** Autenticado

**Exceções:** HTTP 404 se não encontrado

---

#### `listByDoctorId() → List<Patient>`

Lista todos os pacientes vinculados ao médico logado.

**Endpoint:** `GET /api/patient`  
**Acesso:** Autenticado (DOCTOR, MANAGER)

---

#### `confirmDiagnosis(UUID imageId, DoctorVerdict verdict) → PatientImage`

Confirma o diagnóstico de uma imagem específica.

**Endpoint:** `PUT /api/patient/images/{imageId}/confirm`  
**Acesso:** Autenticado

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `imageId` | `UUID` | ID da imagem |
| `verdict` | `DoctorVerdict` | Veredicto do médico |

**Valores possíveis de `DoctorVerdict`:**

| Valor | Descrição | Tipo |
|---|---|---|
| `AGUARDANDO_BIOPSIA` | Aguardando resultado de biópsia | — |
| `MELANOMA` | Melanoma | Maligno |
| `CARCINOMA_BASOCELULAR` | Carcinoma Basocelular | Maligno |
| `CARCINOMA_ESPINOCELULAR` | Carcinoma Espinocelular | Maligno |
| `QUERATOSE_ACTINICA` | Queratose Actínica | Maligno |
| `NEVO` | Nevo (sinal benigno) | Benigno |

---

## 📋 Serviço de Consulta (ConsultationService)

**Classe:** `com.lara.scd.consultation.domain.service.ConsultationService`

O serviço mais complexo — orquestra a criação de consultas, upload de imagens, chamadas à IA e confirmação de diagnósticos.

### Métodos

#### `createConsultation(ConsultationRequest request) → ConsultationResponse`

Cria uma nova consulta médica com múltiplas imagens. Para cada imagem:
1. Encontra ou cria uma `Lesion` baseada no paciente + localização
2. Salva o arquivo no disco via `FileStorageService`
3. Cria o registro `PatientImage` com status `PENDENTE`
4. Chama o serviço de IA (`PredictService.predictImage`)
5. Salva a predição `AiPrediction` e atualiza o status para `CONCLUIDO` ou `FALHA`

| Campo Request | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `patientId` | `UUID` | ✅ | ID do paciente (deve existir previamente) |
| `images` | `List<MultipartFile>` | ✅ | Imagens de lesões (1 a 20) |
| `localizacoes` | `List<String>` | ✅ | Localização anatômica de cada imagem |

**Endpoint:** `POST /api/medico/consultations` (`multipart/form-data`)  
**Acesso:** Autenticado (DOCTOR, MANAGER)

**Validações:**
- Pelo menos 1 imagem obrigatória
- Máximo de 20 imagens por consulta
- Quantidade de localizações deve ser igual à de imagens
- Paciente deve existir antes da criação da consulta

**Valores possíveis de `Localizacao`:**

| Valor | Descrição |
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

**Response (`ConsultationResponse`):**

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | ID da consulta |
| `patient` | `Object` | Dados do paciente (id, nome, cpf, sexo, etc.) |
| `doctor` | `Object` | Dados do médico (id, nome) |
| `finalDiagnosis` | `String` | Diagnóstico final (se confirmado) |
| `confirmed` | `Boolean` | Se a consulta foi confirmada |
| `createdAt` | `DateTime` | Data de criação |
| `images` | `List<ImageInfo>` | Lista de imagens com predições |

---

#### `confirmConsultationDiagnosis(UUID consultationId, ConfirmDiagnosisRequest request) → ConsultationResponse`

Confirma o diagnóstico geral da consulta.

**Endpoint:** `PUT /api/medico/consultations/{id}/confirm`  
**Acesso:** O médico dono da consulta ou MANAGER

---

#### `confirmImageDiagnosis(UUID imageId, ConfirmDiagnosisRequest request) → ConsultationResponse`

Confirma o diagnóstico de uma imagem individual. Calcula automaticamente a **concordância com a IA** (se o veredicto do médico concorda com a classificação da IA em termos de benigno/maligno).

**Endpoint:** `PUT /api/medico/consultations/images/{imageId}/confirm`  
**Acesso:** O médico dono da consulta ou MANAGER

**Lógica de concordância:**
- A IA classifica como `benigno` ou `maligno`
- O médico dá um veredicto (ex: `MELANOMA`, `NEVO`)
- O sistema compara: se ambos concordam na natureza (maligno/benigno), `concordanciaIa = true`

---

#### `listConsultations(String nome, String cpf) → List<ConsultationResponse>`

Lista consultas do médico logado. Suporta filtros opcionais por nome e CPF do paciente.

**Endpoint:** `GET /api/medico/consultations?nome={nome}&cpf={cpf}`  
**Acesso:** Autenticado (DOCTOR, MANAGER)

---

#### `getConsultation(UUID id) → ConsultationResponse`

Retorna detalhes completos de uma consulta.

**Endpoint:** `GET /api/medico/consultations/{id}`  
**Acesso:** O médico dono da consulta ou MANAGER

---

## 🤖 Serviço de Predição (PredictService)

**Classe:** `com.lara.scd.predict.domain.service.PredictService`

Cliente HTTP que se comunica com o microsserviço de IA (FastAPI).

### Métodos

#### `predictImage(Resource resource, int idade, String sexo, String localizacao) → AiPredictionResponse`

Envia uma imagem para o serviço de IA e retorna a predição.

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `resource` | `Resource` | Imagem como recurso (bytes) |
| `idade` | `int` | Idade do paciente (calculada automaticamente) |
| `sexo` | `String` | Sexo do paciente |
| `localizacao` | `String` | Localização anatômica da lesão |

**Comunicação:** `POST http://{ai-service-url}/predict/` (`multipart/form-data`)  
**Configuração:** `app.ai-service.url` (padrão: `http://localhost:8081`)

**Response (`AiPredictionResponse`):**

| Campo | Tipo | Descrição |
|---|---|---|
| `predictions` | `List<Prediction>` | Lista de predições |
| `model_version` | `String` | Versão do modelo de IA |

| Campo Prediction | Tipo | Descrição |
|---|---|---|
| `Class` | `String` | Classe binária (`benigno`/`maligno`) |
| `Probabilidade` | `Double` | Confiança da classificação binária |
| `MultClass` | `String` | Sub-classe (ex: `mel`, `nv`, `bcc`) |
| `ProbabilidadeMultClass` | `Double` | Confiança da sub-classificação |

---

## 🏢 Serviço de Gerente/Admin (ManagerService)

**Classe:** `com.lara.scd.manager.domain.service.ManagerService`

Funcionalidades administrativas do sistema.

### Métodos

#### `registerManager(ManagerRegisterRequestDto dto) → void`

Registra um novo gerente/administrador.

| Campo Request | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | `String` | ✅ | Nome |
| `cpf` | `String` | ✅ | CPF (único) |
| `email` | `String` | ✅ | Email (único) |
| `password` | `String` | ✅ | Senha |

**Endpoint:** `POST /api/admin/register`  
**Acesso:** Autenticado (MANAGER)

---

#### `getDashboardStats() → DashboardResponseDto`

Retorna estatísticas gerais do sistema.

| Campo Response | Tipo | Descrição |
|---|---|---|
| `totalPatients` | `long` | Total de pacientes cadastrados |
| `totalDoctors` | `long` | Total de médicos |
| `totalImages` | `long` | Total de imagens enviadas |
| `totalPredictions` | `long` | Total de predições da IA |
| `totalLesions` | `long` | Total de lesões registradas |

**Endpoint:** `GET /api/admin/dashboard`  
**Acesso:** Autenticado (MANAGER)

---

#### `changePassword(ChangePasswordRequest request) → void`

Altera a senha do administrador logado.

| Campo Request | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `currentPassword` | `String` | ✅ | Senha atual |
| `newPassword` | `String` | ✅ | Nova senha |

**Endpoint:** `POST /api/admin/change-password`  
**Acesso:** Autenticado (MANAGER)

---

#### `generateBackup() → byte[]`

Gera um arquivo ZIP contendo:
- `database.csv` — CSV com metadados de todas as imagens confirmadas pelo médico
- `dataset/` — Pasta com as imagens renomeadas no formato `{imageId}_{patientId}_{verdict}.{ext}`

**Colunas do CSV:**

| Coluna | Descrição |
|---|---|
| `Image ID` | UUID da imagem |
| `Patient ID` | UUID do paciente |
| `Lesion ID` | UUID da lesão |
| `AI Class` | Classe inferida pela IA |
| `AI Confidence` | Confiança da IA |
| `AI MultClass` | Sub-classe da IA |
| `AI MultClass Confidence` | Confiança da sub-classe |
| `AI Model Version` | Versão do modelo |
| `Doctor Final Diagnosis` | Diagnóstico final do médico |
| `Concordancia IA` | Se médico concordou com a IA |

**Endpoint:** `GET /api/admin/backup`  
**Acesso:** Autenticado (MANAGER)  
**Response:** `application/zip` (attachment)

---

## 📁 Serviço de Armazenamento de Arquivos (FileStorageService)

**Classe:** `com.lara.scd.shared.service.FileStorageService`

Gerencia o armazenamento e recuperação de arquivos de imagem no disco.

### Métodos

#### `storeFile(MultipartFile file) → String`

Armazena um arquivo no diretório de uploads com nome UUID único.

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `file` | `MultipartFile` | Arquivo a ser armazenado |
| **Retorno** | `String` | Nome do arquivo gerado (UUID + extensão) |

**Configuração:** `app.upload.dir` (padrão: `uploads`)

---

#### `loadFileAsResource(String fileName) → Resource`

Carrega um arquivo do diretório de uploads como Resource.

**Endpoint:** `GET /api/files/by-name/{filename}`  
**Acesso:** Público (sem autenticação)

---

#### `getStorageLocation() → Path`

Retorna o caminho absoluto do diretório de armazenamento.

---

#### `getBackupZip() → Resource`

Gera um ZIP com todos os arquivos do diretório de uploads.

---

## 📂 Controller de Arquivos (FileController)

**Classe:** `com.lara.scd.shared.application.FileController`

### Endpoints

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/api/files/by-name/{filename}` | Serve um arquivo por nome |
| `GET` | `/api/files/**?path={filePath}` | Serve um arquivo por caminho |

**Acesso:** Público (sem autenticação)  
**Content-Types suportados:** `image/jpeg`, `image/png`, `image/gif`, `image/webp`

---

## 🔄 Serviço de Classificação Direta (PredictController)

**Classe:** `com.lara.scd.predict.application.PredictController`

Controller alternativo que permite classificar uma imagem individual para um paciente específico, sem necessidade de criar uma consulta.

### Endpoints

#### `POST /api/predict/classify/{patientId}`

Classifica uma imagem de lesão para um paciente.

| Parâmetro | Tipo | Localização | Descrição |
|---|---|---|---|
| `patientId` | `UUID` | Path | ID do paciente |
| `file` | `MultipartFile` | Form | Imagem da lesão |
| `localizacao` | `String` | Form | Localização anatômica |

**Acesso:** Autenticado  
**Content-Type:** `multipart/form-data`

**Fluxo:**
1. Busca o paciente pelo ID
2. Calcula a idade a partir da data de nascimento
3. Chama o serviço de IA
4. Salva o arquivo no disco
5. Cria `PatientImage` + `AiPrediction`
6. Retorna `PatientImageResponseDto`

---

## 📊 Resumo de Todos os Endpoints

### Públicos (sem autenticação)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Login (retorna JWT) |
| `POST` | `/api/auth/register` | Registrar médico |
| `POST` | `/api/doctor/register` | Registrar médico (alternativo) |
| `GET` | `/api/files/by-name/{filename}` | Servir arquivo |
| `GET` | `/api/files/**` | Servir arquivo por path |
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/api-docs` | OpenAPI JSON |

### Médico (ROLE_DOCTOR ou ROLE_MANAGER)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/medico/consultations` | Criar consulta com imagens |
| `GET` | `/api/medico/consultations` | Listar consultas |
| `GET` | `/api/medico/consultations/{id}` | Obter consulta por ID |
| `PUT` | `/api/medico/consultations/{id}/confirm` | Confirmar diagnóstico da consulta |
| `PUT` | `/api/medico/consultations/images/{imageId}/confirm` | Confirmar diagnóstico de imagem |

### Paciente (Autenticado)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/patient/register` | Cadastrar paciente |
| `GET` | `/api/patient/search?cpf={cpf}` | Buscar paciente por CPF |
| `GET` | `/api/patient/{id}` | Buscar paciente por ID |
| `GET` | `/api/patient` | Listar pacientes do médico |
| `PUT` | `/api/patient/images/{imageId}/confirm` | Confirmar diagnóstico de imagem |

### Predição (Autenticado)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/predict/classify/{patientId}` | Classificar imagem individual |

### Admin (ROLE_MANAGER)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/admin/register` | Registrar gerente |
| `GET` | `/api/admin/dashboard` | Dashboard de estatísticas |
| `POST` | `/api/admin/change-password` | Alterar senha |
| `GET` | `/api/admin/backup` | Download do backup (ZIP) |
