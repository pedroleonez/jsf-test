# Gerenciador de Tarefas JSF

Aplicacao web de gerenciamento de tarefas desenvolvida com Jakarta EE 10, JSF (Mojarra), PrimeFaces, CDI/Weld, JPA/Hibernate e PostgreSQL.

## Demo em producao

Aplicacao publicada no Render:

- https://gerenciador-de-tarefas-jsf.onrender.com/

## Objetivo do projeto

O sistema permite cadastrar, editar, concluir, remover e consultar tarefas com filtros dinamicos, mantendo uma separacao clara de responsabilidades entre as camadas de apresentacao, regra de negocio e acesso a dados.

## Funcionalidades

- Cadastro de tarefa com os campos:
  - titulo
  - descricao
  - responsavel
  - prioridade (`ALTA`, `MEDIA`, `BAIXA`)
  - deadline
- Edicao de tarefa existente
- Conclusao de tarefa
- Remocao de tarefa com confirmacao
- Busca com filtros combinados:
  - numero (ID)
  - titulo/descricao (texto livre)
  - responsavel
  - situacao (em andamento/concluida)
- Feedback visual com mensagens JSF (`p:growl`)
- Tratamento global de excecoes de negocio e infraestrutura na camada JSF

## Arquitetura

A aplicacao segue arquitetura em camadas:

- `controller`:
  - `TarefaBean` (`@Named`, `@ViewScoped`) controla estado da tela e eventos do usuario.
- `service`:
  - `TarefaService` concentra regras de negocio, transacao e traducao de erros tecnicos.
- `repository`:
  - `TarefaRepository` encapsula persistencia JPA e consulta dinamica via Criteria API.
- `model`:
  - `Tarefa` e `Prioridade`.
- `util`:
  - `JPAUtil` produz `EntityManager` CDI por request.
- `exception`:
  - hierarquia de excecoes de dominio + handler global JSF.

## Estrutura principal

```text
src/main/java/pedroleonez/jsfff/
  controller/TarefaBean.java
  service/TarefaService.java
  repository/TarefaRepository.java
  model/Tarefa.java
  model/Prioridade.java
  util/JPAUtil.java
  exception/
  exception/jsf/

src/main/webapp/
  index.xhtml
  resources/components/
    cadastroForm.xhtml
    listagemForm.xhtml
    header.xhtml
    footer.xhtml
    confirmDialog.xhtml
  resources/css/index.css
  WEB-INF/web.xml
  WEB-INF/faces-config.xml
  WEB-INF/beans.xml

src/main/resources/META-INF/persistence.xml
src/test/java/pedroleonez/jsfff/
```

## Stack tecnica

- Java 17
- Maven (wrapper `./mvnw`)
- Jakarta EE 10 API
- JSF 4.0.5 (Mojarra)
- PrimeFaces 15 (classifier `jakarta`)
- CDI (Weld Servlet)
- JPA 3.0 + Hibernate ORM 6.2
- PostgreSQL (producao/dev)
- H2 (testes)
- JUnit 5 + Mockito
- Tomcat 10.1
- Docker (deploy no Render)

Dependencias em `pom.xml`.

## Modelo de dados

Entidade principal: `Tarefa`

- `id` (`Long`, PK, identity)
- `titulo` (`@NotBlank`)
- `descricao` (`@NotBlank`)
- `responsavel` (`@NotBlank`)
- `deadline` (`@NotNull`, `@FutureOrPresent`)
- `prioridade` (`@NotNull`, enum `ALTA|MEDIA|BAIXA`)
- `concluida` (`boolean`, default `false`)

DDL automatica:

- `hibernate.hbm2ddl.auto=update` (arquivo `persistence.xml`)

## Configuracao de banco

### Local (fallback)

Por padrao, sem variaveis de ambiente, a aplicacao usa os valores de `src/main/resources/META-INF/persistence.xml`.

### Producao/Render (recomendado)

`JPAUtil` permite sobrescrever conexao via ambiente:

- `JDBC_URL`
- `DB_USER`
- `DB_PASSWORD`

Tambem existe fallback para `DATABASE_URL` (quando vier em formato URL nao-JDBC, o codigo converte para `jdbc:postgresql://...`).

## Como executar localmente

### Pre-requisitos

- JDK 17 instalado
- `JAVA_HOME` configurado
- PostgreSQL rodando
- Maven Wrapper (`./mvnw`) ja esta no projeto

### 1. Ajuste o banco

Opcao A: manter `persistence.xml` com seu banco local.

Opcao B: exportar variaveis e usar override:

```bash
export JDBC_URL="jdbc:postgresql://localhost:5432/jsfff_db"
export DB_USER="seu_usuario"
export DB_PASSWORD="sua_senha"
```

### 2. Build

```bash
./mvnw clean package
```

### 3. Execucao no Tomcat

- Gere o WAR e publique no Tomcat 10.1
- Ou rode pelo IntelliJ com configuracao de Tomcat local apontando para o artefato `war exploded`

A aplicacao abre em `index.xhtml`.

## Executar com Docker (local)

Este projeto possui `Dockerfile` multi-stage.

```bash
docker build -t jsfff .
docker run --rm -p 8080:8080 \
  -e JDBC_URL="jdbc:postgresql://host.docker.internal:5432/jsfff_db" \
  -e DB_USER="seu_usuario" \
  -e DB_PASSWORD="sua_senha" \
  jsfff
```

Acesse:

- `http://localhost:8080/`

## Deploy no Render

O deploy atual usa `Dockerfile` na raiz.

### Passo a passo

1. Suba o codigo no GitHub.
2. Crie um banco PostgreSQL no Render.
3. Crie um `Web Service` no Render apontando para o repositorio.
4. Em `Environment`, configure:
   - `JDBC_URL=jdbc:postgresql://<host-interno>:5432/<database>`
   - `DB_USER=<usuario>`
   - `DB_PASSWORD=<senha>`
5. Execute o deploy.

### Observacoes

- O container publica o WAR como `ROOT.war`.
- O comando de start ajusta o Tomcat para a porta `PORT` fornecida pelo Render.

## Testes

Testes unitarios e de integracao:

- `TarefaServiceTest` (regra de negocio + traducao de excecoes)
- `TarefaRepositoryH2Test` (consulta dinamica e operacoes de repositorio com H2)
- `TarefaBeanTest` (comportamento do bean e mensagens JSF)

Executar testes:

```bash
./mvnw test
```

## Tratamento de erros

A aplicacao registra um `ExceptionHandlerFactory` customizado em `faces-config.xml`.

- `GlobalExceptionHandlerFactory`
- `GlobalExceptionHandler`

Comportamento:

- captura excecoes nao tratadas da camada JSF
- resolve causa raiz
- mostra mensagem amigavel ao usuario
- preserva mensagem via flash
- redireciona para a view atual (ou `index.xhtml`)

## Componentes de UI

A pagina principal (`index.xhtml`) é montada por componentes compostos em `resources/components`:

- `header.xhtml`
- `cadastroForm.xhtml`
- `listagemForm.xhtml`
- `confirmDialog.xhtml`
- `footer.xhtml`

Observacao tecnica importante:

- atualizacoes Ajax entre formularios usam IDs completos de naming container, por exemplo:
  - `:cadastroComp:formCadastro`
  - `:listagemComp:formListagem`

## Troubleshooting

### Erro `Cannot find component for expressions ":formCadastro"`

Causa:

- referencia Ajax sem considerar naming container de componente composto.

Correcao:

- usar caminho completo com ID do componente pai (`:cadastroComp:formCadastro`).

### `which: no javac` / `JAVA_HOME environment variable is not set`

Causa:

- JDK nao encontrado no ambiente do terminal.

Correcao:

```bash
export JAVA_HOME="/caminho/do/jdk17"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
```

### Erro de conexao com banco no Render

Checklist:

- `JDBC_URL`, `DB_USER` e `DB_PASSWORD` definidos no service
- URL com host interno do banco no Render
- banco aceitando conexoes da rede interna

## Qualidade e boas praticas aplicadas

- separacao de responsabilidades por camada
- traducao de erro tecnico para mensagem amigavel
- controle transacional explicito no service (RESOURCE_LOCAL)
- filtros dinamicos com Criteria API
- cobertura de testes de unidade e integracao

## Licenca

Projeto para fins educacionais/desafio tecnico.
