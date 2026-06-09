# 🤖 Serviço de IA — API de Predição (FastAPI)

> **Projeto:** `api-scd/` | **Porta:** `8081` | **Framework:** FastAPI + Uvicorn

---

## Visão Geral

Microsserviço responsável por receber imagens de lesões de pele e retornar predições de classificação utilizando modelos de deep learning (YOLOv8).

> ⚠️ **Status Atual:** Os modelos YOLOv8 estão **simulados** com valores hardcoded. No futuro, serão integrados os modelos reais treinados (`binary_model.pt`, `benigno_model.pt`, `maligno_model.pt`).

---

## 📁 Estrutura do Projeto

```
api-scd/
├── main.py                       → Entrypoint FastAPI
├── controller/
│   ├── controller_predict.py     → Router do endpoint de predição
│   └── dto/
│       └── metadados_dto.py      → DTO de metadados (Pydantic)
├── service/
│   └── predict.py                → Lógica de predição (serviço de IA)
├── requirements.txt              → Dependências Python
├── Dockerfile                    → Imagem Docker (python:3.12-slim)
└── docker-compose.yml            → Configuração Docker Compose
```

---

## 📡 Endpoint

### `POST /predict/`

Recebe uma imagem de lesão de pele com metadados clínicos e retorna a classificação da IA.

**Content-Type:** `multipart/form-data`

#### Parâmetros de Entrada

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `file` | `UploadFile` | ✅ | Imagem da lesão (JPEG, PNG, etc.) |
| `idade` | `int` | ✅ | Idade do paciente em anos |
| `sexo` | `str` | ✅ | Sexo do paciente (`"M"` ou `"F"`) |
| `localizacao` | `str` | ✅ | Localização anatômica da lesão |

#### Exemplo de Request (cURL)

```bash
curl -X POST http://localhost:8081/predict/ \
  -F "file=@lesao.jpg" \
  -F "idade=45" \
  -F "sexo=M" \
  -F "localizacao=BRACO_DIREITO"
```

#### Response de Sucesso (HTTP 200)

```json
{
  "predictions": [
    {
      "Class": "maligno",
      "Probabilidade": 0.9542,
      "MultClass": "mel",
      "ProbabilidadeMultClass": 0.9231
    }
  ],
  "model_version": "YOLOv8_simulated_v1.0"
}
```

#### Response de Erro — Imagem Inválida

```json
{
  "predictions": [],
  "model_version": "YOLOv8_simulated_v1.0",
  "error": "Imagem inválida ou não fornecida"
}
```

#### Response de Erro — Arquivo não é imagem (HTTP 400)

```json
{
  "detail": "O arquivo enviado não é uma imagem válida."
}
```

---

## 🧠 Lógica de Classificação

O processo de classificação segue um pipeline de **2 estágios**:

```
Imagem → Modelo Binário → benigno / maligno
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
            Modelo Benigno      Modelo Maligno
            ┌──────────┐       ┌──────────┐
            │ nv       │       │ mel      │
            │ bkl      │       │ bcc      │
            │ df       │       │ akiec    │
            │ vasc     │       └──────────┘
            └──────────┘
```

### Estágio 1 — Classificação Binária

| Classe | Descrição |
|---|---|
| `benigno` | Lesão benigna |
| `maligno` | Lesão maligna |

### Estágio 2 — Sub-classificação Multiclasse

#### Se Benigno:

| Classe | Nome Completo | Descrição |
|---|---|---|
| `nv` | Nevo Melanocítico | Sinal comum ("pinta") |
| `bkl` | Queratose Benigna | Lesão não cancerosa da pele |
| `df` | Dermatofibroma | Nódulo benigno firme |
| `vasc` | Lesão Vascular | Lesão de vasos sanguíneos |

#### Se Maligno:

| Classe | Nome Completo | Descrição |
|---|---|---|
| `mel` | Melanoma | Câncer de pele mais agressivo |
| `bcc` | Carcinoma Basocelular | Câncer de pele mais comum |
| `akiec` | Queratose Actínica | Lesão pré-cancerosa |

---

## 📦 DTO — MetaDadosDTO

**Arquivo:** `controller/dto/metadados_dto.py`

```python
class MetaDadosDTO(BaseModel):
    """
    DTO para transferir dados básicos como Idade, Sexo e Localização da lesão.
    """
    idade: int
    sexo: str
    localizacao: str
```

---

## ⚙️ Configuração

### Dependências (`requirements.txt`)

| Pacote | Função |
|---|---|
| `fastapi` | Framework web assíncrono |
| `uvicorn` | Servidor ASGI |
| `fastapi[standard]` | Extensões padrão do FastAPI |
| `pydantic` | Validação de dados |
| `Pillow` | Processamento de imagens |
| `numpy` | Operações numéricas |
| `pika` | Cliente RabbitMQ (reservado para uso futuro) |

### Docker

**Imagem base:** `python:3.12-slim`  
**Porta exposta:** `8081`  
**Comando de execução:** `uvicorn main:app --host 0.0.0.0 --port 8081`

### CORS

O serviço aceita requisições de **qualquer origem** (`*`):
- Todas as origens permitidas
- Todos os métodos HTTP permitidos
- Todos os headers permitidos
- Credenciais permitidas

---

## 🔮 Roadmap (TODOs no Código)

1. **Integrar modelos YOLOv8 reais:**
   - `models/binary_model.pt` — Classificação binária
   - `models/benigno_model.pt` — Sub-classificação benigna
   - `models/maligno_model.pt` — Sub-classificação maligna

2. **Utilizar metadados clínicos** (idade, sexo, localização) como features adicionais no modelo

3. **Implementar fila de processamento** com RabbitMQ (dependência `pika` já incluída)
