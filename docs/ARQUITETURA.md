# Arquitetura

## Backend

Spring Boot MVC com controllers REST, services de dominio e repositories JPA.

Novos endpoints:

- `/api/business/dashboard`
- `/api/business/instituicoes`
- `/api/business/turmas`
- `/api/business/alunos`
- `/api/business/documentos`
- `/api/business/comprovantes`
- `/api/business/importacoes/alunos`
- `/api/business/relatorios`

## Seguranca

JWT stateless, BCrypt e autorizacao no backend por perfil, organizacao e turmas permitidas.

## Frontend

HTML/CSS/JS estatico. A decisao evita migracao desnecessaria e aproveita a base do projeto.

## Storage

`StorageService` abstrai persistencia de arquivos. A implementacao atual e local e usa nomes UUID, validacao de tamanho/extensao e caminho configuravel.

## Banco

JPA com MySQL como banco principal. Flyway foi adicionado para migrations incrementais. Em ambiente local, `ddl-auto=update` completa a transicao da base academica; em producao deve ser trocado para `validate` apos consolidacao das migrations.

O perfil `demo-h2` existe para smoke test e demonstracao rapida sem credenciais MySQL. Ele usa H2 em modo MySQL, arquivo em `target/` e seed demonstrativo.
