# 🔐 Segurança e Autenticação

> Documentação das configurações de segurança, JWT e regras de acesso do LARA-SCD.

---

## Autenticação JWT

O sistema utiliza **JWT (JSON Web Token)** para autenticação stateless.

### Configuração

| Propriedade | Valor | Descrição |
|---|---|---|
| `api.security.token.secret` | `${TOKEN_SECRET : topsecret}` | Chave secreta para assinatura do token |
| `api.security.token.issuer` | `${TOKEN_ISSUER : SCD}` | Emissor do token |

### Fluxo de Autenticação

```
1. Cliente envia POST /api/auth/login { email, password }
        │
        ▼
2. AuthenticationManager valida credenciais (BCrypt)
        │
        ▼
3. JwtUtil gera token com claims: email, userId, accessLevel
        │
        ▼
4. Retorna { type: "Bearer", token: "eyJ...", id, nome, email, accessLevel }
        │
        ▼
5. Cliente armazena token (AsyncStorage no mobile)
        │
        ▼
6. Todas as requisições subsequentes incluem header:
   Authorization: Bearer eyJ...
        │
        ▼
7. JwtFilter valida o token em cada request
```

### Componentes de Segurança

| Classe | Responsabilidade |
|---|---|
| `SecurityConfig` | Configuração principal do Spring Security |
| `JwtFilter` | Filtro que valida JWT em cada requisição |
| `JwtUtil` | Geração e validação de tokens JWT |
| `CustomUserDetailsService` | Carrega detalhes do usuário pelo email |
| `SecurityContext` | Obtém o usuário autenticado no contexto atual |

---

## Níveis de Acesso (Roles)

| Role | Enum | Descrição |
|---|---|---|
| `ROLE_DOCTOR` | `AccessLevel.DOCTOR` | Médico — acesso a consultas, pacientes e predições |
| `ROLE_MANAGER` | `AccessLevel.MANAGER` | Gerente/Admin — acesso total incluindo dashboard e backup |

### Hierarquia de Entidades

```
User (abstract)
├── Doctor  → AccessLevel.DOCTOR  (tem CRM)
└── Manager → AccessLevel.MANAGER
```

---

## Regras de Acesso por Endpoint

### Endpoints Públicos (sem autenticação)

| Pattern | Descrição |
|---|---|
| `/api/auth/**` | Login e registro |
| `/api/user/login` | Login (alternativo) |
| `/api/doctor/register` | Registro de médico |
| `/api/files/**` | Servir imagens/arquivos |
| `/swagger-ui/**` | Interface Swagger |
| `/swagger-ui.html` | Swagger UI HTML |
| `/v3/api-docs/**` | Documentação OpenAPI |
| `/api-docs/**` | API Docs |
| `/h2-console/**` | Console H2 (dev) |

### Endpoints Restritos

| Pattern | Role Necessária | Descrição |
|---|---|---|
| `/api/admin/**` | `ROLE_MANAGER` | Endpoints administrativos |
| `/api/medico/**` | `ROLE_DOCTOR` ou `ROLE_MANAGER` | Endpoints de consultas |
| Qualquer outro | Autenticado | Requer JWT válido |

---

## CORS (Cross-Origin Resource Sharing)

### Backend (Spring Boot)

| Configuração | Valor |
|---|---|
| Origens permitidas | `*` (todas) |
| Métodos permitidos | `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS` |
| Headers permitidos | `*` (todos) |
| Headers expostos | `Authorization` |

### Serviço de IA (FastAPI)

| Configuração | Valor |
|---|---|
| Origens permitidas | `*` (todas) |
| Credenciais | Permitidas |
| Métodos permitidos | `*` (todos) |
| Headers permitidos | `*` (todos) |

---

## Criptografia de Senhas

| Aspecto | Detalhe |
|---|---|
| **Algoritmo** | BCrypt |
| **Implementação** | `BCryptPasswordEncoder` (Spring Security) |
| **Onde** | Senhas são criptografadas no registro de Doctor e Manager |

---

## Proteções no Mobile

O app mobile implementa:

1. **Armazenamento seguro do token:** Via `AsyncStorage`
2. **Interceptor de request:** Adiciona JWT automaticamente a todas as chamadas
3. **Interceptor de response:** Em caso de HTTP 401, limpa token e dados do usuário (logout automático)
4. **Timeout:** 30 segundos para requisições normais, 120 segundos para upload de imagens

---

## Consentimento LGPD

O sistema implementa consentimento LGPD para uso dos dados do paciente pela IA:

| Campo | Tabela | Descrição |
|---|---|---|
| `termo_consentimento_ia` | `patients` | Se o paciente consentiu com o uso da IA |
| `data_consentimento` | `patients` | Data/hora do consentimento |
| `data_revogacao_consentimento` | `patients` | Data/hora da revogação (se aplicável) |

O consentimento é registrado no momento do cadastro do paciente e pode ser revogado posteriormente.
