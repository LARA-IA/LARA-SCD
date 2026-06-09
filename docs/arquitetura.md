# 🏗️ Arquitetura do Sistema LARA-SCD

## Visão Geral

O LARA-SCD é uma plataforma de detecção de câncer de pele assistida por IA, composta por **3 microsserviços** que se comunicam para fornecer diagnósticos dermatológicos.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        LARA-SCD Platform                            │
│                                                                     │
│  ┌──────────────┐     ┌─────────────────┐     ┌──────────────────┐ │
│  │  scd-mobile   │────▶│   scd (Backend) │────▶│ api-scd (IA/ML) │ │
│  │  React Native │     │  Spring Boot    │     │    FastAPI       │ │
│  │  Expo         │     │  Java 21        │     │    Python 3.12   │ │
│  │  Port: 8082    │     │  Port: 8080     │     │    Port: 8081    │ │
│  └──────────────┘     └────────┬────────┘     └──────────────────┘ │
│                                │                                    │
│                        ┌───────▼───────┐                           │
│                        │    MySQL 8.2   │                           │
│                        │   Port: 3306   │                           │
│                        └───────────────┘                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Componentes

### 1. `scd` — Backend Principal (Spring Boot)

| Aspecto | Detalhe |
|---|---|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 3.5.7 |
| **Porta** | 8080 |
| **Banco de Dados** | MySQL 8.2 (produção) / H2 (desenvolvimento) |
| **Autenticação** | JWT (Auth0 java-jwt 4.4.0) |
| **Documentação API** | Swagger/OpenAPI (springdoc 2.8.5) |
| **ORM** | Spring Data JPA + Hibernate + Envers |

**Responsabilidades:**
- Autenticação e autorização de usuários (médicos e gerentes)
- CRUD de pacientes, consultas e imagens
- Orquestração de chamadas ao serviço de IA
- Armazenamento de arquivos (imagens de lesões)
- Geração de backups (dataset ZIP com CSV de metadados)
- Dashboard administrativo

---

### 2. `api-scd` — Microsserviço de IA (FastAPI)

| Aspecto | Detalhe |
|---|---|
| **Linguagem** | Python 3.12 |
| **Framework** | FastAPI + Uvicorn |
| **Porta** | 8081 |
| **Modelo IA** | YOLOv8 (simulado — `YOLOv8_simulated_v1.0`) |
| **Bibliotecas** | Pillow, NumPy, Pydantic |

**Responsabilidades:**
- Receber imagens de lesões de pele + metadados (idade, sexo, localização)
- Realizar classificação binária: `benigno` / `maligno`
- Realizar sub-classificação multiclasse:
  - Benigno: `nv`, `bkl`, `df`, `vasc`
  - Maligno: `mel`, `bcc`, `akiec`
- Retornar predições com probabilidades

> ⚠️ **Nota:** Atualmente os modelos YOLOv8 estão **simulados** (valores hardcoded). No futuro, serão substituídos pelos modelos reais treinados.

---

### 3. `scd-mobile` — Aplicativo Móvel (React Native / Expo)

| Aspecto | Detalhe |
|---|---|
| **Framework** | React Native 0.81.5 + Expo SDK 54 |
| **Navegação** | Expo Router 6 |
| **HTTP Client** | Axios 1.13.6 |
| **Armazenamento Local** | AsyncStorage |
| **Linguagem** | TypeScript 5.9.2 |

**Responsabilidades:**
- Interface do médico para login e registro
- Cadastro de pacientes (com consentimento LGPD)
- Criação de consultas com upload de múltiplas imagens
- Visualização de resultados da IA
- Confirmação de diagnóstico pelo médico
- Painel administrativo (dashboard, backup, alteração de senha)

---

## Fluxo Principal de Dados

```
1. Médico faz login no app mobile
        │
        ▼
2. Cadastra paciente (com consentimento LGPD)
        │
        ▼
3. Cria consulta com imagens de lesões
        │
        ▼
4. Backend recebe imagens e para cada uma:
   a. Salva o arquivo no disco
   b. Cria registro PatientImage (status: PENDENTE)
   c. Chama o serviço de IA (api-scd)
   d. Recebe predição (benigno/maligno + sub-classe)
   e. Salva AiPrediction e atualiza status (CONCLUIDO/FALHA)
        │
        ▼
5. Médico revisa cada imagem e confirma/altera o diagnóstico
   (calcula concordância com a IA)
        │
        ▼
6. Gerente pode exportar dataset (imagens + CSV) para treino
```

---

## Modelo de Dados (Entidades Principais)

```
┌──────────┐     ┌──────────┐     ┌──────────────┐     ┌──────────────┐
│   User   │◄────│  Doctor  │────▶│ Consultation │────▶│ PatientImage │
│ (abstract)│     │          │     │              │     │              │
├──────────┤     ├──────────┤     ├──────────────┤     ├──────────────┤
│ id (UUID)│     │ CRM      │     │ id           │     │ id           │
│ nome     │     └──────────┘     │ finalDiagnosis│    │ filePath     │
│ cpf      │                      │ confirmed    │     │ localizacao  │
│ email    │     ┌──────────┐     │ createdAt    │     │ doctorVerdict│
│ password │     │ Manager  │     └──────────────┘     │ confirmed    │
│ accessLvl│     │          │                          │ concordanciaIa│
└──────────┘     └──────────┘                          │ statusIa     │
                                                       └──────┬───────┘
                  ┌──────────┐     ┌──────────┐               │
                  │ Patient  │     │  Lesion  │        ┌──────▼───────┐
                  ├──────────┤     ├──────────┤        │ AiPrediction │
                  │ id       │     │ id       │        ├──────────────┤
                  │ nome     │     │ localizacao│      │ versaoModelo │
                  │ cpf      │     │ patient  │        │ classeInferida│
                  │ dataNasc │     └──────────┘        │ confianca    │
                  │ sexo     │                         │ multClasse   │
                  │ consentimento│                     │ confiancaMulti│
                  └──────────┘                         └──────────────┘
```

---

## Stack Tecnológica Completa

| Camada | Tecnologia |
|---|---|
| **Mobile** | React Native, Expo, TypeScript, Axios |
| **Backend** | Java 21, Spring Boot 3.5.7, Spring Security, Spring Data JPA |
| **IA/ML** | Python 3.12, FastAPI, YOLOv8, NumPy, Pillow |
| **Banco de Dados** | MySQL 8.2, H2 (dev) |
| **Autenticação** | JWT (Auth0 java-jwt), BCrypt |
| **Documentação** | SpringDoc OpenAPI, Swagger UI |
| **Containerização** | Docker, Docker Compose |
| **Auditoria** | Spring Data Envers |
