# AGENTS

## Objetivo

Evoluir o Formaly Business como prototipo B2B para organizadoras de formaturas, mantendo fluxos demonstraveis de login, dashboard, instituicoes, turmas, alunos, documentos, comprovantes, importacao e relatorios.

## Branches

- Branch-base obrigatoria: `prototype/timbe-v0`.
- Nunca editar `main` diretamente.
- Branch de trabalho padrao: `feat/prototipo-empresarial-timbe-v0`.
- Nao usar force push nem reescrever historico.

## Arquitetura

- Backend: Spring Boot, MVC, Security, JPA.
- Frontend: HTML/CSS/JS estatico em `src/main/resources/static`.
- Novos fluxos empresariais usam `/api/business`.
- DTOs devem ser usados nos endpoints novos.
- Multiempresa deve ser aplicado no backend via organizacao/turma/perfil.

## Convencoes

- Dados operacionais devem ter vinculo claro com `Organizacao`.
- Identificador de aluno nao deve ser unico globalmente; use `turma_id + identificador`.
- Valores monetarios devem usar `BigDecimal` no modelo.
- Uploads passam por `StorageService`.
- Exclusao operacional deve preferir status/inativacao.
- Dados de demonstracao devem ser ficticios.

## Comandos

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
docker compose up --build
```

## Testes obrigatorios

Antes de concluir tarefa relevante, executar ao menos `.\mvnw.cmd test`. Para mudancas de empacotamento ou config, executar tambem `.\mvnw.cmd package`.

## Seguranca

- Nao versionar `.env`, tokens, chaves ou senhas reais.
- Credenciais demo so podem ser usadas em ambiente local/demo.
- Nao registrar CPF completo, senha, token ou conteudo de documentos em logs/auditoria.
- Validar tamanho, extensao e caminho de uploads.

## Migrations

- Criar migrations em `src/main/resources/db/migration`.
- Nao usar `ddl-auto=create` ou `create-drop` fora de testes.
- Migrations devem ser incrementais e nao destrutivas.

## Marca e ativos

- O prototipo pode se inspirar na identidade publica da Timbe, mas deve conter o disclaimer.
- Nao fazer hotlink de imagens.
- Nao incorporar fotos de terceiros sem autorizacao documentada.
- Logo publico, se usado, deve ficar isolado e substituivel.

## Commits

Use commits pequenos por area, com prefixos como `chore:`, `feat:`, `test:` e `docs:`.

## Criterios minimos

- Compila.
- Testes passam.
- Aplicacao inicia.
- Login demo funciona.
- Nao ha segredos versionados.
- README e docs refletem as mudancas.
