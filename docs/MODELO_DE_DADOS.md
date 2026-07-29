# Modelo de dados

Hierarquia principal:

Organizacao -> Instituicao -> Turma -> Aluno -> Documentos/Comprovantes/Historico.

Entidades preparadas:

- `Organizacao`
- `Instituicao`
- `Usuario`
- `UsuarioOrganizacao`
- `Turma`
- `Aluno`
- `Responsavel`
- `TipoDocumento`
- `RequisitoDocumentoTurma`
- `DocumentoAluno`
- `RevisaoDocumento`
- `Comprovante`
- `Evento`
- `Tarefa`
- `HistoricoAuditoria`

## Identificador do aluno

O identificador nao e chave primaria e nao deve ser unico globalmente. A regra adotada no dominio empresarial e `turma_id + identificador`. O login tecnico do usuario pode receber sufixo quando houver conflito global.

## Dados sensiveis

CPF e opcional e nao aparece completo nas respostas novas. O seed nao usa CPF real.
