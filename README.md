# Formaly Business

Prototipo B2B para gestao de turmas, alunos, documentos e comprovantes de formatura.

Contexto da demonstracao: prototipo preparado para validacao com a Timbe Formaturas. Ele nao representa uma plataforma oficial contratada ou homologada pela empresa.

## Status

Prototipo conceitual funcional. Os dados de demonstracao sao ficticios e podem ser recriados pelo seed local.

## Funcionalidades

- Login com JWT e BCrypt.
- Perfis `ADMIN_ORGANIZACAO`, `COLABORADOR`, `COMISSAO` e `ALUNO`.
- Dashboard operacional calculado a partir do banco.
- Gestao de instituicoes, turmas e alunos.
- Responsavel legal opcional no cadastro do aluno.
- Tipos documentais e requisitos por turma.
- Checklist documental por aluno, upload local, aprovacao, reprovacao e historico.
- Gestao de comprovantes com upload e analise.
- Importacao de alunos por CSV ou XLSX com preview, validacao e estrategia de duplicidade.
- Relatorios operacionais e exportacao CSV.
- Seed demonstrativo com dados ficticios.
- Identidade visual inspirada no site publico da Timbe Formaturas, com ativos locais documentados e aviso de prototipo.

## Arquitetura

- Java 17, Spring Boot 4, Maven.
- Spring MVC, Spring Security, Spring Data JPA.
- JWT via `java-jwt`.
- Banco MySQL em local/demo; H2 para testes e smoke demo local.
- Flyway com migration incremental inicial.
- Frontend estatico em HTML, CSS e JavaScript ES Modules.
- CSS organizado em tokens, base, layout, componentes e estilos por pagina.
- Upload local via `StorageService`, com caminho configuravel e pronto para futura implementacao S3.

## Requisitos

- Java 17 ou superior.
- Maven Wrapper incluso.
- MySQL 8 para execucao local tradicional.
- Docker e Docker Compose para execucao containerizada.

## Configuracao

Use `.env.example` como referencia. Nunca versione `.env`.

Variaveis principais:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_SECURITY_JWT_SECRET`
- `APP_DEMO_SEED_ENABLED`
- `APP_DEMO_ADMIN_PASSWORD`
- `APP_DEMO_COLLABORATOR_PASSWORD`
- `APP_DEMO_COMMITTEE_PASSWORD`
- `APP_DEMO_STUDENT_PASSWORD`
- `FORMALY_STORAGE_PATH`

## Execucao local

Com MySQL:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Sem MySQL, para smoke test ou demonstracao rapida:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo-h2"
```

Acesse `http://localhost:8080/login.html`.

## Docker

```powershell
docker compose up --build
```

Acesse `http://localhost:8080/login.html`.

## Credenciais locais de demonstracao

- Admin: `admin.demo@formaly.local` / `DemoAdmin2026!`
- Colaborador: `colaborador.demo@formaly.local` / `DemoColaborador2026!`
- Comissao: `comissao.demo@formaly.local` / `DemoComissao2026!`
- Aluno: `demo001` / `DemoAluno2026!`

Use apenas em ambiente local ou demo. Em qualquer ambiente compartilhado, substitua as senhas por variaveis de ambiente.

## Testes

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Smoke visual responsivo com Playwright, com a aplicacao ja iniciada:

```powershell
./scripts/visual-smoke.ps1 -BaseUrl http://localhost:18080 -OutputDir target/visual/final-smoke
```

O script valida login, dashboard, turmas, detalhe do aluno, documentos, portal do aluno, quatro perfis demo, oito viewports e ausencia de overflow horizontal evidente.

## Dados de demonstracao

O seed roda quando `APP_DEMO_SEED_ENABLED=true`. Para recriar a demonstracao local com MySQL, limpe o banco `formaly_business` e remova os arquivos de `FORMALY_STORAGE_PATH`. No perfil `demo-h2`, remova `target/formaly-business-demo*` e a pasta configurada em `FORMALY_STORAGE_PATH`.

## Estrutura

- `src/main/java/.../model`: entidades JPA.
- `src/main/java/.../controller`: APIs.
- `src/main/java/.../service`: seguranca de dominio, storage, documentos e importacao.
- `src/main/resources/static`: frontend estatico.
- `src/main/resources/static/assets/brand/timbe`: ativos locais da demonstracao visual.
- `src/main/resources/static/css`: sistema visual do Formaly Business.
- `src/main/resources/db/migration`: migrations Flyway.
- `docs`: documentacao do prototipo.

## Limitacoes

- Storage e local, sem S3.
- PDF operacional legado foi mantido, mas os novos relatorios priorizam CSV.
- A matriz de permissoes e inicial e deve ser validada com o cliente.
- Nao ha notificacoes, assinatura digital, pagamentos ou integracoes externas.
- Logo e fotografias da Timbe estao pendentes de autorizacao formal para uso externo.

## Seguranca

Nao versione segredos. `APP_SECURITY_JWT_SECRET` deve ser substituido fora do ambiente local. Uploads possuem validacao de extensao, tamanho, nome armazenado imprevisivel e protecao contra path traversal.

## Autoria

Base academica preservada e evoluida para prototipo empresarial. Consulte os documentos em `docs/` para auditoria, decisoes e escopo.
