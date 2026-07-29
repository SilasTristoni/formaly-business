# Auditoria visual antes/depois

Data: 2026-07-29.

## Objetivo

Registrar a segunda etapa de refinamento visual do Formaly Business, comparando o estado anterior com a versao reformulada para demonstracao conceitual com a Timbe Formaturas.

## Problemas visuais atuais encontrados

- Identidade generica de painel administrativo, com baixa relacao visual com a Timbe.
- Login funcional, mas sem impacto editorial, fotografia ou conexao clara com formaturas.
- Dashboard com cards similares e pouca hierarquia entre indicadores.
- Sidebar e navegacao com aparencia comum de sistema interno.
- Tabelas e listas corretas, mas sem padrao visual proprietario.
- Mobile funcional, porem pouco memoravel e sem tratamento especifico para experiencia do aluno.
- Estados e modais sem suficiente coerencia com cards, badges e botoes.
- Aviso de prototipo presente, mas com pouca integracao visual.

## Decisoes tomadas

- Usar o logo publico da Timbe apenas como ativo substituivel e pendente de autorizacao.
- Adotar fotografia real do site como base do login, dashboard e portal do aluno, com copias locais em WebP.
- Derivar paleta do site publico: azul `#0170B9`, amarelo `#FFBF27`, preto `#030303`, brancos/cinzas e fundos quentes.
- Usar Poppins e Prata para replicar a logica visual do hero publico: texto forte e palavra editorial em amarelo.
- Manter area administrativa clara, sem fotografia atras de tabelas, para preservar legibilidade.
- Aplicar sidebar escura com acento amarelo e uso moderado do azul.
- Reorganizar CSS em tokens, base, layout, componentes e paginas.
- Padronizar botoes, inputs, cards, tabelas, badges, modais, estados vazios, metric cards e foco.
- Ajustar mobile com menu drawer, cards responsivos e portal do aluno simplificado.

## Telas redesenhadas

- Login.
- Dashboard operacional.
- Sidebar e topbar.
- Instituicoes.
- Turmas.
- Alunos.
- Detalhe/checklist do aluno.
- Documentos.
- Comprovantes.
- Importacao.
- Relatorios.
- Permissoes.
- Portal do aluno.
- Modais, badges, tabelas e estados vazios.

## Comparacao visual

### Referencia publica

- Desktop: `docs/visual/referencia/timbe-home-desktop-1440x900.webp`.
- Mobile: `docs/visual/referencia/timbe-home-mobile-390x844.webp`.

### Antes

- Login desktop: `docs/visual/antes/login-desktop-1440x900.webp`.
- Login mobile: `docs/visual/antes/login-mobile-390x844.webp`.
- Dashboard desktop: `docs/visual/antes/dashboard-desktop-1440x900.webp`.
- Dashboard mobile: `docs/visual/antes/dashboard-mobile-390x844.webp`.
- Portal do aluno mobile: `docs/visual/antes/portal-aluno-mobile-390x844.webp`.

### Depois

- Login desktop: `docs/visual/depois/login-desktop-1440x900.webp`.
- Login mobile: `docs/visual/depois/login-mobile-390x844.webp`.
- Dashboard desktop: `docs/visual/depois/dashboard-desktop-1440x900.webp`.
- Dashboard mobile: `docs/visual/depois/dashboard-mobile-390x844.webp`.
- Turmas desktop: `docs/visual/depois/turmas-desktop-1440x900.webp`.
- Alunos desktop: `docs/visual/depois/alunos-desktop-1440x900.webp`.
- Detalhe do aluno desktop: `docs/visual/depois/aluno-detalhe-desktop-1440x900.webp`.
- Documentos desktop: `docs/visual/depois/documentos-desktop-1440x900.webp`.
- Relatorios desktop: `docs/visual/depois/relatorios-desktop-1440x900.webp`.
- Portal do aluno mobile: `docs/visual/depois/portal-aluno-mobile-390x844.webp`.

## Resultado

O prototipo deixou de parecer um dashboard administrativo generico e passou a combinar:

- fotografia e linguagem emocional de formatura;
- logo e acentos visuais coerentes com a Timbe;
- interface administrativa clara para uso operacional;
- experiencia mobile do aluno com mais acolhimento e foco em progresso;
- componentes mais consistentes e reutilizaveis.

## Validacao responsiva

Smoke visual executado com Playwright em:

- `1920x1080`
- `1440x900`
- `1280x800`
- `1024x768`
- `768x1024`
- `430x932`
- `390x844`
- `360x800`

Comando:

```powershell
./scripts/visual-smoke.ps1 -BaseUrl http://localhost:18080 -OutputDir target/visual/final-smoke
```

O script valida login, dashboard, lista de turmas, detalhe do aluno, documentos, portal do aluno, quatro perfis demo, ausencia de overflow horizontal evidente e presenca do aviso de prototipo.

## Validacao de contraste

Pares principais calculados a partir dos tokens:

- Texto forte `#050505` em superficie `#FFFDF8`: 20.05:1.
- Texto base `#202329` em branco `#FFFFFF`: 15.74:1.
- Branco `#FFFFFF` em preto hero `#030303`: 20.62:1.
- Preto `#050505` em botao amarelo `#FFBF27`: 12.36:1.
- Branco `#FFFFFF` em azul institucional `#0170B9`: 5.22:1.
- Azul `#0170B9` em azul suave `#E7F5FF`: 4.70:1.

## Limitacoes

- O uso do logo e das fotografias ainda depende de autorizacao formal.
- A conversao de imagens foi feita para o prototipo; um ambiente publico deve usar pipeline de assets definitivo.
- O smoke visual nao substitui uma suite completa de regressao visual por comparacao pixel-a-pixel.
