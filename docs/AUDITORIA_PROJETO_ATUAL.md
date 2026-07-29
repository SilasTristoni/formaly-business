# Auditoria do projeto atual

Data: 2026-07-28.

## Arquitetura encontrada

- Java 17 com Spring Boot 4.0.0, Maven e empacotamento WAR.
- Backend MVC com Spring Security, JPA, MySQL em runtime e H2 em testes/smoke demo.
- Frontend estatico servido por `src/main/resources/static`, com HTML, CSS e JavaScript.
- Autenticacao por JWT (`java-jwt`) e senha com BCrypt.
- PDF via OpenPDF no modulo financeiro.
- Antes da mudanca nao havia Flyway nem migrations versionadas.

## Funcionalidades existentes

- Login de comissao e aluno.
- Dashboard financeiro/operacional academico.
- Cadastro de turmas, alunos, eventos, lancamentos financeiros e votacoes.
- Portal do aluno com agenda, votos e resumo financeiro.
- Relatorios financeiros com CSV/PDF.
- Testes de contexto, login e alguns calculos.

## Componentes reaproveitaveis

- Infra Spring Security e JWT.
- Repositorios JPA e padrao controller/service existente.
- Entidades `Turma`, `Aluno`, `Evento`, `Tarefa`, `Usuario`.
- Frontend estatico e componentes basicos de toast/modal.
- OpenPDF para uso futuro.

## Pontos a refatorar

- `CadastroController` concentrava varias responsabilidades.
- Endpoints legados retornavam entidades JPA diretamente.
- Perfis eram apenas comissao/aluno.
- `Aluno.identificador` era unico globalmente.
- Valores monetarios usavam `Double`.
- Configuracao trazia senha padrao e segredo JWT local em arquivo principal.
- `ddl-auto=update` era a unica estrategia de schema.

## Problemas criticos

- Ausencia de multiempresa.
- Ausencia de isolamento por organizacao/turma no backend.
- Upload documental inexistente.
- Importacao CSV simplificada e sem preview/transacao/duplicidade.
- Dados iniciais academicos com referencias ao Senac e a comissao.

## Riscos

- Endpoints academicos legados continuam existindo para compatibilidade.
- O frontend principal foi substituido, mas telas antigas ainda existem em arquivos nao navegados.
- Flyway foi adicionado com migration incremental; em bases legadas, revisar antes de producao.
- Storage local nao e adequado para producao.

## Recomendacao arquitetural

Evoluir a base existente em vez de migrar para React/Angular/Vue. A base estatica e suficiente para o prototipo e reduz risco. Novos fluxos empresariais ficam em `/api/business`, com DTOs e autorizacao real por organizacao/turma.

## Reaproveitamento qualitativo

Reaproveitamento medio: aproximadamente 55%. A infraestrutura Spring, auth, JPA, frontend estatico e parte das entidades foram mantidas; dominio empresarial, documentos, comprovantes, importacao e dashboard foram adicionados.

## Plano de implementacao

P0: identidade empresarial, login, dashboard, organizacoes/instituicoes, turmas, alunos, documentos, comprovantes, importacao, relatorios, seed, responsividade, README e testes principais.

P1: historico mais detalhado, Docker, experiencia completa do aluno, responsaveis e tarefas.

P2: pagamentos, WhatsApp, assinatura digital, app nativo e integracoes externas.
