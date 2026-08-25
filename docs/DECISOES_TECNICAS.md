# Decisoes tecnicas

## Frontend estatico em vez de React

Opcoes: manter HTML/CSS/JS, migrar para React/Vue/Angular.

Decisao: manter frontend estatico. A base ja servia paginas estaticas e a migracao nao era necessaria para um prototipo demonstravel.

## JWT mantido

Opcoes: sessao server-side ou JWT.

Decisao: manter JWT por ja existir no projeto e ser adequado ao frontend estatico. Foram adicionados emissor configuravel, expiracao configuravel e tratamento seguro de token invalido.

## Multiempresa por organizacao e turmas permitidas

Decisao: toda consulta nova parte da organizacao atual e das turmas permitidas pelo usuario. Filtros do frontend sao apenas conveniencia.

## Identificador de aluno

Decisao: regra de duplicidade por `turma + identificador`, pois a mesma matricula/RA pode se repetir entre instituicoes/turmas.

## Storage local com abstracao

Opcoes: salvar binario no banco, storage local, S3.

Decisao: `StorageService` com implementacao local para desenvolvimento. Nao salvar binarios no banco. S3 fica como proximo passo.

## Flyway

Decisao: adicionar Flyway por nao haver estrategia versionada. A migration inicial cria tabelas empresariais e indices; a consolidacao completa da base legada deve ocorrer antes de producao.

## PDF

Decisao: manter OpenPDF legado, mas priorizar CSV nos novos relatorios porque e mais confiavel para demonstracao operacional.

## Refinamento visual Timbe

Opcoes: apenas trocar variaveis no CSS existente, migrar para um framework frontend, ou reorganizar a base estatica atual em sistema de design.

Decisao: manter HTML/CSS/JS e separar a identidade em `tokens.css`, `base.css`, `layout.css`, `components.css` e arquivos por pagina. A mudanca reduz duplicacao, preserva a arquitetura existente e evita custo de migracao para React/Vue/Angular.

## Ativos de marca

Decisao: copiar localmente apenas o logo e poucas fotografias publicas necessarias para a demonstracao, convertendo fotos para WebP e documentando origem/autorizacao. Nao usar hotlink e manter os arquivos em `src/main/resources/static/assets/brand/timbe/` para substituicao futura por ativos autorizados ou white-label.
