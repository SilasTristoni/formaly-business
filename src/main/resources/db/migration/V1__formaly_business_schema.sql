CREATE TABLE IF NOT EXISTS organizacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    status VARCHAR(40),
    observacoes TEXT,
    criado_em DATETIME(6),
    atualizado_em DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS instituicao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organizacao_id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    nome_abreviado VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(32),
    contato VARCHAR(255),
    status VARCHAR(40),
    observacoes TEXT,
    criado_em DATETIME(6),
    atualizado_em DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_instituicao_org (organizacao_id)
);

CREATE TABLE IF NOT EXISTS usuario_organizacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    organizacao_id BIGINT NOT NULL,
    turma_permitida_id BIGINT,
    perfil VARCHAR(80),
    ativo BIT DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_usuario_org_usuario (usuario_id),
    INDEX idx_usuario_org_org (organizacao_id),
    INDEX idx_usuario_org_turma (turma_permitida_id)
);

CREATE TABLE IF NOT EXISTS responsavel (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organizacao_id BIGINT NOT NULL,
    nome VARCHAR(255),
    parentesco VARCHAR(120),
    cpf VARCHAR(32),
    email VARCHAR(255),
    telefone VARCHAR(80),
    whatsapp VARCHAR(80),
    contato_principal BIT DEFAULT 0,
    observacao TEXT,
    PRIMARY KEY (id),
    INDEX idx_responsavel_org (organizacao_id)
);

CREATE TABLE IF NOT EXISTS tipo_documento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organizacao_id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    obrigatorio BIT DEFAULT 1,
    aplicavel_menor_idade BIT DEFAULT 1,
    permite_multiplos_arquivos BIT DEFAULT 0,
    validade_dias INT,
    extensoes_permitidas VARCHAR(255),
    tamanho_maximo_bytes BIGINT,
    status VARCHAR(40),
    PRIMARY KEY (id),
    INDEX idx_tipo_documento_org (organizacao_id)
);

CREATE TABLE IF NOT EXISTS requisito_documento_turma (
    id BIGINT NOT NULL AUTO_INCREMENT,
    turma_id BIGINT NOT NULL,
    tipo_documento_id BIGINT NOT NULL,
    obrigatorio BIT DEFAULT 1,
    ativo BIT DEFAULT 1,
    status VARCHAR(40),
    PRIMARY KEY (id),
    UNIQUE KEY uk_req_turma_tipo (turma_id, tipo_documento_id),
    INDEX idx_req_turma (turma_id),
    INDEX idx_req_tipo (tipo_documento_id)
);

CREATE TABLE IF NOT EXISTS documento_aluno (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aluno_id BIGINT NOT NULL,
    tipo_documento_id BIGINT NOT NULL,
    nome_original VARCHAR(255),
    nome_armazenado VARCHAR(255),
    referencia_arquivo VARCHAR(500),
    tamanho BIGINT,
    mime_type VARCHAR(160),
    data_envio DATETIME(6),
    usuario_envio_id BIGINT,
    status VARCHAR(40),
    data_analise DATETIME(6),
    usuario_analise_id BIGINT,
    justificativa TEXT,
    observacao TEXT,
    versao INT,
    PRIMARY KEY (id),
    INDEX idx_documento_aluno (aluno_id),
    INDEX idx_documento_tipo (tipo_documento_id),
    INDEX idx_documento_status (status)
);

CREATE TABLE IF NOT EXISTS revisao_documento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    documento_id BIGINT NOT NULL,
    status_anterior VARCHAR(40),
    status_novo VARCHAR(40),
    usuario_id BIGINT,
    data_revisao DATETIME(6),
    justificativa TEXT,
    observacao TEXT,
    PRIMARY KEY (id),
    INDEX idx_revisao_documento (documento_id)
);

CREATE TABLE IF NOT EXISTS comprovante (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organizacao_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    aluno_id BIGINT,
    descricao VARCHAR(255),
    nome_original VARCHAR(255),
    nome_armazenado VARCHAR(255),
    referencia_arquivo VARCHAR(500),
    tamanho BIGINT,
    mime_type VARCHAR(160),
    data_envio DATETIME(6),
    usuario_envio_id BIGINT,
    status VARCHAR(40),
    data_analise DATETIME(6),
    usuario_analise_id BIGINT,
    comentario TEXT,
    PRIMARY KEY (id),
    INDEX idx_comprovante_org (organizacao_id),
    INDEX idx_comprovante_turma (turma_id),
    INDEX idx_comprovante_aluno (aluno_id),
    INDEX idx_comprovante_status (status)
);

CREATE TABLE IF NOT EXISTS historico_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organizacao_id BIGINT,
    entidade VARCHAR(120),
    entidade_id VARCHAR(120),
    acao VARCHAR(120),
    usuario_id BIGINT,
    data_hora DATETIME(6),
    resumo TEXT,
    PRIMARY KEY (id),
    INDEX idx_historico_org_data (organizacao_id, data_hora),
    INDEX idx_historico_entidade (entidade, entidade_id)
);
