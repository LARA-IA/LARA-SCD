# 🐳 Infraestrutura e Deploy

> Docker, Docker Compose e configurações de ambiente do LARA-SCD.

---

## Visão Geral dos Containers

```
┌─────────────────────────────────────────────────────┐
│                    Docker Network                    │
│                     "redescd"                        │
│                                                     │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │  scd (app)  │  │  db (MySQL)  │  │  api-scd   │ │
│  │  :8080      │─▶│  :3306       │  │  :8081     │ │
│  │  Java 21    │  │  MySQL 8.2   │  │  Python 3.12│ │
│  └─────────────┘  └──────────────┘  └────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## 1. Backend (scd) — Docker Compose

**Arquivo:** `scd/docker-compose.yml`

### Serviços

#### `db` — MySQL 8.2

| Configuração | Valor |
|---|---|
| **Imagem** | `mysql:8.2` |
| **Porta** | `3306:3306` |
| **Root Password** | `root` |
| **Database** | `scd` |
| **User** | `user` |
| **Password** | `root` |
| **Network** | `redescd` |

**Health Check:**
```bash
mysqladmin ping -h localhost -u root -proot
# Intervalo: 10s | Timeout: 10s | Retries: 10 | Start Period: 60s
```

#### `app` — Spring Boot

| Configuração | Valor |
|---|---|
| **Build** | `Dockerfile` local |
| **Porta** | `8080:8080` |
| **Restart** | `always` |
| **Depende de** | `db` (condição: `service_healthy`) |
| **Network** | `redescd` |

### Dockerfile (`scd/Dockerfile`)

```dockerfile
# Estágio base: JDK 21 Alpine + Maven
FROM eclipse-temurin:21-jdk-alpine AS base
WORKDIR app
RUN apk add --no-cache maven

# Estágio de build
FROM base AS build
COPY . .
RUN mvn install -DskipTests

EXPOSE 8080
CMD ["mvn", "spring-boot:run"]
```

| Aspecto | Detalhe |
|---|---|
| **Imagem base** | `eclipse-temurin:21-jdk-alpine` |
| **Build tool** | Maven |
| **Multi-stage** | Sim (base + build) |
| **Testes** | Ignorados no build (`-DskipTests`) |

---

## 2. Serviço de IA (api-scd) — Docker Compose

**Arquivo:** `api-scd/docker-compose.yml`

### Serviço

#### `api` — FastAPI

| Configuração | Valor |
|---|---|
| **Build** | `Dockerfile` local |
| **Porta** | `8081:8081` |
| **Volume** | `.:/app` (bind mount para desenvolvimento) |

### Dockerfile (`api-scd/Dockerfile`)

```dockerfile
FROM python:3.12-slim
LABEL authors="jairvictor"

# Dependências do sistema para OpenCV
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

RUN pip install uvicorn

WORKDIR /app
COPY . /app

RUN pip install --upgrade pip
RUN pip install -r requirements.txt

EXPOSE 8081
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8081"]
```

| Aspecto | Detalhe |
|---|---|
| **Imagem base** | `python:3.12-slim` |
| **Deps do sistema** | `libgl1`, `libglib2.0-0` (para OpenCV/processamento de imagem) |
| **Servidor** | Uvicorn |

---

## 3. Perfis de Configuração (Spring Boot)

### `application.properties` (base)

| Propriedade | Valor | Descrição |
|---|---|---|
| `spring.application.name` | `scd` | Nome da aplicação |
| `spring.jackson.time-zone` | `America/Recife` | Timezone para serialização JSON |
| `server.port` | `8080` | Porta do servidor |
| `spring.profiles.active` | `${PROD : prod}` | Perfil ativo (padrão: prod) |
| `api.security.token.secret` | `${TOKEN_SECRET : topsecret}` | Segredo JWT |
| `api.security.token.issuer` | `${TOKEN_ISSUER : SCD}` | Emissor JWT |

### `application-dev.properties` (desenvolvimento)

Configuração para ambiente de desenvolvimento (provavelmente H2 in-memory).

### `application-prod.properties` (produção)

Configuração para ambiente de produção com MySQL.

### Swagger/OpenAPI

| Propriedade | Valor |
|---|---|
| `springdoc.api-docs.enabled` | `true` |
| `springdoc.swagger-ui.enabled` | `true` |
| `springdoc.api-docs.path` | `/api-docs` |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` |
| `springdoc.swagger-ui.operationsSorter` | `method` |
| `springdoc.swagger-ui.tagsSorter` | `alpha` |
| `springdoc.swagger-ui.try-it-out-enabled` | `true` |

---

## 4. Variáveis de Ambiente

### Backend (Spring Boot)

| Variável | Padrão | Descrição |
|---|---|---|
| `PROD` | `prod` | Perfil Spring ativo |
| `TOKEN_SECRET` | `topsecret` | Segredo para assinatura JWT |
| `TOKEN_ISSUER` | `SCD` | Emissor do token JWT |
| `app.upload.dir` | `uploads` | Diretório de upload de imagens |
| `app.ai-service.url` | `http://localhost:8081` | URL do serviço de IA |

### Banco de Dados (MySQL)

| Variável | Padrão | Descrição |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `root` | Senha root do MySQL |
| `MYSQL_DATABASE` | `scd` | Nome do banco |
| `MYSQL_USER` | `user` | Usuário do banco |
| `MYSQL_PASSWORD` | `root` | Senha do usuário |

---

## 5. App Mobile (Expo)

O app mobile **não é containerizado**, sendo executado com Expo CLI.

### Comandos

| Comando | Descrição |
|---|---|
| `npm start` / `npx expo start` | Inicia o servidor de desenvolvimento |
| `npm run android` | Inicia no emulador Android |
| `npm run ios` | Inicia no simulador iOS |
| `npm run web` | Inicia versão web |
| `npm run lint` | Executa linting |

### Configuração de Rede

O app mobile precisa apontar para o IP correto do backend:

| Plataforma | URL Base |
|---|---|
| Android (emulador) | `http://10.0.2.2:8080/api` |
| Android (dispositivo físico) | `http://{IP_LOCAL}:8080/api` |
| iOS (simulador) | `http://localhost:8080/api` |

> ⚠️ **Nota:** O IP atual hardcoded é `192.168.1.4`. Atualize em `services/api.ts` conforme seu ambiente.

---

## 6. Comandos de Deploy

### Subir todo o sistema

```bash
# 1. Backend + Banco de Dados
cd scd
docker-compose up -d --build

# 2. Serviço de IA
cd ../api-scd
docker-compose up -d --build

# 3. App Mobile (desenvolvimento local)
cd ../scd-mobile
npm install
npx expo start
```

### Verificar status

```bash
# Backend
docker-compose -f scd/docker-compose.yml ps

# IA
docker-compose -f api-scd/docker-compose.yml ps

# Logs
docker-compose -f scd/docker-compose.yml logs -f app
docker-compose -f api-scd/docker-compose.yml logs -f api
```

### Parar tudo

```bash
docker-compose -f scd/docker-compose.yml down
docker-compose -f api-scd/docker-compose.yml down
```

---

## 7. Dados Iniciais (Seed)

No perfil `prod`, a aplicação cria automaticamente dois usuários de teste via `CommandLineRunner`:

| Tipo | Email | Senha | CPF |
|---|---|---|---|
| Doctor | `doctor@example.com` | `password123` | `12345678901` |
| Manager | `manager@example.com` | `password123` | `98765432101` |

> ⚠️ **Atenção:** Em produção real, remova ou altere essas credenciais padrão.
