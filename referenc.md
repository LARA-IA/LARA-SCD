[cite_start]Este é o Documento de Especificação de Requisitos de Software (ERS) para o **Sistema de Registro Clínico e Suporte ao Diagnóstico Dermatológico Baseado em Deep Learning**, datado de 27 de março de 2026[cite: 3, 5].

---

## 1. Introdução
### 1.1 Propósito
[cite_start]O documento define os requisitos e o modelo de dados para um sistema voltado à coleta, armazenamento e análise de imagens de lesões de pele[cite: 11]. [cite_start]O objetivo é gerir o prontuário do paciente e fornecer infraestrutura de dados para treinamento e inferência de modelos de Inteligência Artificial[cite: 12].

### 1.2 Escopo
* [cite_start]Cadastro de pacientes e gestão de permissões (LGPD)[cite: 14].
* [cite_start]Registro temporal de consultas e arquivamento de lesões (imagens clínicas e dermatoscópicas)[cite: 14, 15].
* [cite_start]Rastreamento de evolução temporal e predições versionadas de modelos de Deep Learning[cite: 15].

### 1.3 Perfil dos Usuários
* [cite_start]**Médicos Dermatologistas:** Inserem dados clínicos, imagens, características e diagnósticos finais[cite: 17].
* [cite_start]**Cientistas de Dados / Engenheiros de IA:** Consomem dados anonimizados para treinamento e gerenciam APIs de inferência[cite: 18].

---

## 2. Requisitos Funcionais (RF)
* [cite_start]**RF01 - Gestão de Pacientes:** Cadastro demográfico e registro de consentimento LGPD para uso em IA[cite: 20].
* [cite_start]**RF02 - Gestão de Consultas:** Vínculo entre consulta, paciente e médico para auditoria[cite: 21].
* [cite_start]**RF03 - Registro de Imagens:** Upload de pares de imagens (clínica/dermatoscópica) com metadados de captura[cite: 22].
* [cite_start]**RF04 - Rastreamento Temporal:** Vínculo de novos registros a lesões físicas já mapeadas para análise evolutiva[cite: 23].
* [cite_start]**RF05 - Anotação Morfológica:** Classificação de características como rede pigmentar, estrias e estruturas vasculares[cite: 24].
* [cite_start]**RF06 - Diagnóstico e Conduta:** Registro do diagnóstico confirmado, dificuldade e conduta clínica[cite: 25].
* [cite_start]**RF07 - Integração com IA:** Armazenamento de predições em JSON e versão do modelo utilizado[cite: 26].
* **RF08 - Concordância IA:** Registro automático de concordância entre veredito médico e predição IA para métricas de acurácia.
* **RF09 - Status de Processamento IA:** Rastreamento do estado de processamento de cada imagem pela IA (PENDENTE, CONCLUIDO, FALHA).

---

## 3. Requisitos Não Funcionais (RNF)
* [cite_start]**RNF01 - Privacidade:** Anonimização obrigatória na exportação; apenas pacientes com consentimento compõem a base de treino[cite: 29, 30].
* [cite_start]**RNF02 - Armazenamento:** Imagens salvas em Cloud Storage (ex: AWS S3); banco de dados armazena apenas a URL[cite: 31].
* [cite_start]**RNF03 - Rastreabilidade:** Todas as tabelas devem conter carimbos de tempo (`criado_em`, `atualizado_em`)[cite: 32].
* [cite_start]**RNF04 - Interoperabilidade:** Armazenamento do vetor de probabilidade (*softmax*) completo em JSON[cite: 33].
* **RNF05 - Revogação LGPD:** Suporte à revogação de consentimento com registro de data, bloqueando exportação futura de dados para treinamento de IA.

---

## 4. Regras de Negócio (RN)
* [cite_start]**RN01 - Cálculo de Idade:** Calculada dinamicamente entre `data_nascimento` e `data_consulta`[cite: 35].
* [cite_start]**RN02 - Dependência de Exames:** `diagnostico_exame` preenchido apenas após confirmação laboratorial[cite: 36].
* [cite_start]**RN03 - Versionamento de Predição:** Predições exigem obrigatoriamente a `versao_modelo_ia`[cite: 37].
* **RN04 - Concordância Automática:** Ao confirmar veredito médico, o sistema calcula automaticamente se houve concordância com a classificação binária da IA (maligno/benigno).

---

## 5. Especificação de Dados (Dicionário)

### 5.1 Entidade: medico
| Campo | Tipo | Descrição | Obrigatório |
| :--- | :--- | :--- | :--- |
| id_medico | UUID (PK) | Identificador único | Sim |
| nome | VARCHAR | Nome do profissional | Sim |
| crm | VARCHAR | Registro no conselho de medicina | Sim |
| criado_em | TIMESTAMP | Data de registro no sistema | Sim |

### 5.2 Entidade: paciente
| Campo | Tipo | Descrição | Obrigatório |
| :--- | :--- | :--- | :--- |
| id_paciente | UUID (PK) | Identificador único | Sim |
| nome | VARCHAR | Nome completo | Sim |
| cpf | VARCHAR | CPF do paciente | Sim |
| sexo | VARCHAR(1) | M, F, O | Sim |
| data_nascimento | DATE | Base para cálculo de idade | Sim |
| termo_consentimento_ia | BOOLEAN | Aceite para uso de dados (LGPD) | Sim |
| data_consentimento | TIMESTAMP | Data da assinatura do termo | Não |
| data_revogacao_consentimento | TIMESTAMP | Data de revogação do consentimento (LGPD) | Não |
| criado_em | TIMESTAMP | Auditoria de criação | Sim |
| atualizado_em | TIMESTAMP | Auditoria de modificação | Sim |

### 5.3 Entidade: consulta
| Campo | Tipo | Descrição | Obrigatório |
| :--- | :--- | :--- | :--- |
| id_consulta | UUID (PK) | Identificador único | Sim |
| id_paciente | UUID (FK) | Referência ao paciente | Sim |
| id_medico | UUID (FK) | Referência ao médico (Auditoria) | Sim |
| diagnostico_final | VARCHAR | Diagnóstico final confirmado pelo médico | Não |
| confirmado | BOOLEAN | Se o diagnóstico foi confirmado | Sim |
| criado_em | TIMESTAMP | Auditoria de criação | Sim |
| atualizado_em | TIMESTAMP | Auditoria de modificação | Sim |

### 5.4 Entidade: lesao
| Campo | Tipo | Descrição | Obrigatório |
| :--- | :--- | :--- | :--- |
| id_lesao | UUID (PK) | Identificador único da lesão física | Sim |
| id_paciente | UUID (FK) | Referência ao paciente dono da lesão | Sim |
| localizacao_anatomica | VARCHAR (ENUM) | Localização no corpo (Cabeça, Tronco, etc.) | Sim |
| descricao | VARCHAR | Descrição livre da lesão | Não |
| criado_em | TIMESTAMP | Data de criação | Sim |

> **Nota:** Esta entidade representa uma **lesão física real** no corpo do paciente. Permite rastrear a mesma pinta/lesão ao longo de múltiplas consultas e imagens (RF04).

### 5.5 Entidade: registro_lesao (patient_image)
| Campo | Tipo | Descrição / Domínio | Obrigatório |
| :--- | :--- | :--- | :--- |
| id_registro | UUID (PK) | Identificador deste registro | Sim |
| id_consulta | UUID (FK) | Referência à consulta | Sim |
| id_paciente | UUID (FK) | Referência ao paciente | Sim |
| id_lesao | UUID (FK) | Referência à lesão física rastreada | Não |
| caminho_arquivo | VARCHAR | URL/caminho da imagem armazenada | Sim |
| nome_arquivo | VARCHAR | Nome original do arquivo | Sim |
| tamanho_arquivo | BIGINT | Tamanho em bytes | Sim |
| tipo_conteudo | VARCHAR | MIME type (image/jpeg, etc.) | Sim |
| localizacao | VARCHAR (ENUM) | Cabeça, Tronco, Acral, etc. | Sim |
| veredito_medico | VARCHAR (ENUM) | Diagnóstico final do médico | Não |
| confirmado | BOOLEAN | Se o médico confirmou | Sim |
| concordancia_ia | BOOLEAN | Médico concordou com a classificação da IA? | Não |
| status_processamento_ia | VARCHAR (ENUM) | PENDENTE, CONCLUIDO, FALHA | Sim |
| criado_em | TIMESTAMP | Auditoria de criação | Sim |
| atualizado_em | TIMESTAMP | Auditoria de modificação | Sim |

### 5.6 Entidade: predicao_ia
| Campo | Tipo | Descrição | Obrigatório |
| :--- | :--- | :--- | :--- |
| id_predicao | UUID (PK) | Identificador único | Sim |
| id_registro_lesao | UUID (FK) | Referência ao registro de lesão (imagem) | Sim |
| versao_modelo | VARCHAR | Versão do modelo de IA (ex: YOLOv8_v1.0) | Sim |
| classe_inferida | VARCHAR | Resultado binário: MALIGNO / BENIGNO | Não |
| confianca | DOUBLE | Probabilidade da classe binária | Não |
| mult_classe | VARCHAR | Subtipo específico (mel, bcc, nv, etc.) | Não |
| confianca_mult_classe | DOUBLE | Probabilidade do subtipo | Não |
| probabilidades_json | TEXT | Vetor softmax completo em JSON (RNF04) | Não |
| criado_em | TIMESTAMP | Data da predição | Sim |

> **Nota:** Esta entidade permite **versionamento de predições** (RN03). Uma mesma imagem pode ser re-avaliada por modelos mais novos sem sobrescrever o histórico. Relação 1:N com `registro_lesao`.

---