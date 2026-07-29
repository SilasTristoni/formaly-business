# Pesquisa de identidade publica da Timbe Formaturas

Data da consulta: 2026-07-29.

Fonte principal: `https://www.timbeeventos.com.br/`.

## Paginas e capturas analisadas

- Home publica completa da Timbe Formaturas.
- Screenshot desktop: `docs/visual/referencia/timbe-home-desktop-1440x900.webp`.
- Screenshot mobile: `docs/visual/referencia/timbe-home-mobile-390x844.webp`.
- Capturas adicionais geradas em `target/visual/reference/` para notebook `1280x800` e tablet `768x1024`.
- Inspecao computada salva durante a auditoria em `target/visual/reference/timbe-analysis.json`.

## Identidade observada

O site publico comunica formatura como experiencia emocional, celebracao e sonho realizado. A primeira dobra usa fotografia escura em tela ampla, alto contraste, logo preto/amarelo/branco, titulo grande em Poppins e destaque editorial em Prata amarelo.

A linguagem institucional encontrada no site destaca formaturas em Joinville, experiencia, estrutura, organizacao completa, equipe dedicada, transparencia e portfolio de eventos reais.

## Paleta final adotada

Valores extraidos do CSS publico:

- Azul institucional: `#0170B9`.
- Cinza texto Astra: `#4B4F58`.
- Cinza grafite: `#3A3A3A`.
- Fundo claro: `#F5F5F5`.
- Branco: `#FFFFFF`.
- Borda clara: `#E5E5E5`.
- Preto: `#000000`.

Valores extraidos visualmente do logo, hero e botoes:

- Amarelo celebrativo: `#FFBF27`.
- Preto profundo usado como base fotografica: `#030303`.
- Azul profundo para contraste: `#003F68`.
- Azul suave administrativo: `#E7F5FF`.
- Fundo quente do sistema: `#F7F6F2`.
- Superficie elevada quente: `#FFFDF8`.
- Borda quente: `#E4DED0`.

Tokens aplicados em `src/main/resources/static/css/tokens.css`:

- `--brand-primary`: `#0170b9`.
- `--brand-primary-dark`: `#003f68`.
- `--brand-primary-soft`: `#e7f5ff`.
- `--brand-secondary`: `#030303`.
- `--brand-accent`: `#ffbf27`.
- `--brand-background`: `#f7f6f2`.
- `--brand-surface`: `#ffffff`.
- `--brand-surface-elevated`: `#fffdf8`.
- `--brand-text`: `#202329`.
- `--brand-text-soft`: `#60646f`.
- `--brand-border`: `#e4ded0`.
- `--brand-success`: `#1f8a5b`.
- `--brand-warning`: `#a96800`.
- `--brand-danger`: `#be3d34`.

## Tipografia

- Site publico: Poppins em titulos e botoes; Prata no destaque "inesquecivel"; corpo em stack de sistema/Astra.
- Prototipo: Poppins como familia principal e Prata como display de marca.
- Decisao: carregar Poppins e Prata via Google Fonts por serem fontes publicas e compatíveis com a referencia. O sistema continua funcional com fallback de sistema.

Hierarquia aplicada:

- Display: Prata para palavras emocionais e celebrativas.
- Titulos de pagina/secao/card: Poppins em pesos 700/800.
- Corpo, formularios e tabelas: Poppins com tamanho maior e contraste controlado.
- Badges e botoes: Poppins em peso forte, caixa visual compacta.

## Padroes visuais do site

- Hero fotografico escuro, com overlay preto e texto branco/amarelo.
- Botoes sem arredondamento exagerado, amarelos ou brancos, texto preto forte.
- Seções de impacto com fundo preto e conteudo centralizado.
- Fotografia de estrutura, evento, palco, decoracao e portfolio.
- Uso de azul principalmente como navegacao/acento institucional.
- Logo em fundo escuro, com amarelo como sinal de destaque.
- Rodape institucional simples e direto.

## Componentes que inspiraram o sistema

- Login editorial com foto ampla, logo e frase curta inspirada no hero publico.
- Dashboard com fotografia da estrutura como capa operacional.
- Sidebar escura com logo e acento amarelo, para conversar com o topo do site.
- Botoes primarios amarelos e botoes secundarios brancos.
- Cards administrativos claros, com borda quente e sombra leve.
- Badges com fundo suave, borda e texto, sem depender apenas de cor.
- Area do aluno com hero fotografico e linguagem mais emocional.

## Diferencas entre site e prototipo antes do refinamento

- Antes: interface administrativa generica, predominio de azul frio e cards similares.
- Antes: login sem composicao fotografica forte.
- Antes: navegacao e cards nao remetiam ao logo, ao amarelo ou ao contraste do site publico.
- Antes: telas internas tinham pouca relacao visual entre si.
- Antes: mobile funcionava, mas sem narrativa visual adequada para demonstracao com a Timbe.

## Plano visual de transformacao aplicado

- Baixar e isolar logo e imagens publicas selecionadas em `src/main/resources/static/assets/brand/timbe/`.
- Converter fotografias para WebP e documentar autorizacao pendente.
- Reorganizar CSS em tokens, base, layout, componentes e paginas.
- Redesenhar login, sidebar, topbar, dashboard, tabelas, cards, badges, modais e portal do aluno.
- Validar screenshots antes/depois e responsividade com Playwright.
- Manter aviso de prototipo visivel em login, area administrativa e portal do aluno.

## Observacoes de licenca

Nao foi encontrada autorizacao publica para reutilizacao ampla de logo ou fotografias. Os ativos foram copiados localmente apenas para prototipo privado de validacao e estao documentados como pendentes de autorizacao em `docs/ATIVOS_E_LICENCAS.md` e em `src/main/resources/static/assets/brand/timbe/README.md`.
