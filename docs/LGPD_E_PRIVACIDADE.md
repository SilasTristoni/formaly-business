# LGPD e privacidade

Este documento e tecnico e nao substitui validacao juridica.

## Categorias de dados

- Identificacao: nome, identificador, e-mail, telefone, WhatsApp.
- Dados opcionais: CPF e data de nascimento.
- Dados de responsavel legal: nome, parentesco e contato.
- Dados operacionais: turma, instituicao, status escolar, contratual, beca e documental.
- Arquivos: documentos e comprovantes enviados.
- Auditoria: entidade, acao, usuario, data e resumo.

## Finalidade

Operar formaturas, acompanhar pendencias, validar documentos, comunicar responsabilidades e gerar relatorios.

## Acesso por perfil

Admin e colaborador acessam dados da organizacao/turmas permitidas. Comissao acessa visao limitada da propria turma. Aluno acessa apenas os proprios dados.

## Minimizacao

CPF e opcional. Logs nao devem registrar CPF completo, senhas, tokens ou conteudo integral de documentos.

## Retencao e exclusao

Prazo de retencao precisa ser definido contratualmente. O prototipo favorece inativacao/exclusao logica em dados operacionais.

## Menores

Responsaveis legais sao armazenados para suporte ao processo de menores. A exposicao em listas publicas deve ser restrita.

## Pontos pendentes

- Base legal definitiva.
- Papeis de controlador/operador.
- Politica de retencao.
- Processo de exportacao/exclusao de titular.
- Termos de uso e consentimentos.
- Regras de backup e descarte.
