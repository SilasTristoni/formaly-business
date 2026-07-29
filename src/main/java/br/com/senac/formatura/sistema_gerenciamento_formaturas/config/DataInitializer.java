package br.com.senac.formatura.sistema_gerenciamento_formaturas.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Comprovante;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.DocumentoAluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Evento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.HistoricoAuditoria;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Instituicao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Organizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RequisitoDocumentoTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Responsavel;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoBeca;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoCadastro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoContratual;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.SituacaoEscolar;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusComprovante;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusRegistro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Tarefa;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.TipoDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Turma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.UsuarioOrganizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.AlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.ComprovanteRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.DocumentoAlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.EventoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.HistoricoAuditoriaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.InstituicaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.OrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RequisitoDocumentoTurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.ResponsavelRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TipoDocumentoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TarefaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioOrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);
    private static final String ORG_NAME = "Timbe Eventos - Ambiente Demonstrativo";

    private final OrganizacaoRepository organizacaoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final RequisitoDocumentoTurmaRepository requisitoRepository;
    private final DocumentoAlunoRepository documentoRepository;
    private final ComprovanteRepository comprovanteRepository;
    private final EventoRepository eventoRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioOrganizacaoRepository usuarioOrganizacaoRepository;
    private final HistoricoAuditoriaRepository historicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;
    private final String adminPassword;
    private final String colaboradorPassword;
    private final String comissaoPassword;
    private final String alunoPassword;

    public DataInitializer(
        OrganizacaoRepository organizacaoRepository,
        InstituicaoRepository instituicaoRepository,
        TurmaRepository turmaRepository,
        AlunoRepository alunoRepository,
        ResponsavelRepository responsavelRepository,
        TipoDocumentoRepository tipoDocumentoRepository,
        RequisitoDocumentoTurmaRepository requisitoRepository,
        DocumentoAlunoRepository documentoRepository,
        ComprovanteRepository comprovanteRepository,
        EventoRepository eventoRepository,
        TarefaRepository tarefaRepository,
        UsuarioRepository usuarioRepository,
        UsuarioOrganizacaoRepository usuarioOrganizacaoRepository,
        HistoricoAuditoriaRepository historicoRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.demo.seed-enabled:false}") boolean seedEnabled,
        @Value("${app.demo.admin-password:}") String adminPassword,
        @Value("${app.demo.collaborator-password:}") String colaboradorPassword,
        @Value("${app.demo.committee-password:}") String comissaoPassword,
        @Value("${app.demo.student-password:}") String alunoPassword
    ) {
        this.organizacaoRepository = organizacaoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.turmaRepository = turmaRepository;
        this.alunoRepository = alunoRepository;
        this.responsavelRepository = responsavelRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.requisitoRepository = requisitoRepository;
        this.documentoRepository = documentoRepository;
        this.comprovanteRepository = comprovanteRepository;
        this.eventoRepository = eventoRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioOrganizacaoRepository = usuarioOrganizacaoRepository;
        this.historicoRepository = historicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
        this.adminPassword = adminPassword;
        this.colaboradorPassword = colaboradorPassword;
        this.comissaoPassword = comissaoPassword;
        this.alunoPassword = alunoPassword;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            LOGGER.info("Seed demonstrativo desativado.");
            return;
        }
        if (adminPassword.isBlank() || colaboradorPassword.isBlank() || comissaoPassword.isBlank() || alunoPassword.isBlank()) {
            throw new IllegalStateException("Seed demonstrativo habilitado sem todas as senhas demo configuradas.");
        }
        Organizacao org = ensureOrganizacao();
        List<Instituicao> instituicoes = ensureInstituicoes(org);
        List<Turma> turmas = ensureTurmas(org, instituicoes);
        List<Responsavel> responsaveis = ensureResponsaveis(org);
        List<TipoDocumento> tipos = ensureTiposDocumento(org);
        ensureRequisitos(turmas, tipos);
        List<Aluno> alunos = ensureAlunos(turmas, responsaveis);
        ensureDocumentos(alunos, tipos);
        ensureComprovantes(org, alunos);
        ensureEventos(org, turmas);
        ensureTarefas(org, turmas, alunos);
        ensureUsuarios(org, turmas, alunos);
        ensureHistorico(org);
        LOGGER.info("Seed demonstrativo do Formaly Business verificado com {} turmas e {} alunos.", turmas.size(), alunos.size());
    }

    private Organizacao ensureOrganizacao() {
        return organizacaoRepository.findByNomeIgnoreCase(ORG_NAME).orElseGet(() -> {
            Organizacao org = new Organizacao();
            org.setNome(ORG_NAME);
            org.setNomeFantasia("Timbe Demo");
            org.setStatus(StatusRegistro.ATIVO);
            org.setObservacoes("Organizacao ficticia criada exclusivamente para demonstracao local.");
            return organizacaoRepository.save(org);
        });
    }

    private List<Instituicao> ensureInstituicoes(Organizacao org) {
        if (!instituicaoRepository.findByOrganizacaoIdOrderByNomeAsc(org.getId()).isEmpty()) {
            return instituicaoRepository.findByOrganizacaoIdOrderByNomeAsc(org.getId());
        }
        return List.of(
            instituicao(org, "Colegio Horizonte Norte", "Horizonte", "Joinville", "SC"),
            instituicao(org, "Instituto Aurora", "Aurora", "Joinville", "SC"),
            instituicao(org, "Centro Educacional Vale Verde", "Vale Verde", "Sao Francisco do Sul", "SC")
        );
    }

    private Instituicao instituicao(Organizacao org, String nome, String abreviado, String cidade, String estado) {
        Instituicao instituicao = new Instituicao();
        instituicao.setOrganizacao(org);
        instituicao.setNome(nome);
        instituicao.setNomeAbreviado(abreviado);
        instituicao.setCidade(cidade);
        instituicao.setEstado(estado);
        instituicao.setContato("contato@" + abreviado.toLowerCase().replace(" ", "") + ".demo");
        instituicao.setStatus(StatusRegistro.ATIVO);
        instituicao.setObservacoes("Instituicao ficticia para demonstracao.");
        return instituicaoRepository.save(instituicao);
    }

    private List<Turma> ensureTurmas(Organizacao org, List<Instituicao> instituicoes) {
        List<Turma> existentes = turmaRepository.findByOrganizacaoIdOrderByNomeAsc(org.getId());
        if (!existentes.isEmpty()) return existentes;
        List<Turma> turmas = new ArrayList<>();
        turmas.add(turma(org, instituicoes.get(0), "Terceirao A", "Ensino Medio", "2026", StatusTurma.ATIVA, 0));
        turmas.add(turma(org, instituicoes.get(0), "Tecnico Eventos 2026", "Tecnico em Eventos", "2026/2", StatusTurma.PLANEJAMENTO, 1));
        turmas.add(turma(org, instituicoes.get(1), "Administracao 2026", "Administracao", "2026/2", StatusTurma.ATIVA, 2));
        turmas.add(turma(org, instituicoes.get(1), "Design 2027", "Design", "2027/1", StatusTurma.EM_FINALIZACAO, 3));
        turmas.add(turma(org, instituicoes.get(2), "Pedagogia 2026", "Pedagogia", "2026/2", StatusTurma.ATIVA, 4));
        return turmas;
    }

    private Turma turma(Organizacao org, Instituicao instituicao, String nome, String curso, String ano, StatusTurma status, int offset) {
        Turma turma = new Turma();
        turma.setOrganizacao(org);
        turma.setInstituicaoEntidade(instituicao);
        turma.setInstituicao(instituicao.getNome());
        turma.setNome(nome);
        turma.setCurso(curso);
        turma.setAnoSemestre(ano);
        turma.setDataPrevistaFormatura(LocalDate.now().plusMonths(5 + offset));
        turma.setResponsavelComercial("Equipe Comercial Demo");
        turma.setResponsavelOperacional("Coordenacao Operacional Demo");
        turma.setRepresentante("Representante da turma");
        turma.setStatusTurma(status);
        turma.setStatus(status.name());
        turma.setMetaArrecadacao(0.0);
        turma.setTotalArrecadado(0.0);
        turma.setAtivo(status != StatusTurma.CANCELADA);
        return turmaRepository.save(turma);
    }

    private List<Responsavel> ensureResponsaveis(Organizacao org) {
        List<Responsavel> existentes = responsavelRepository.findByOrganizacaoIdOrderByNomeAsc(org.getId());
        if (!existentes.isEmpty()) return existentes;
        String[] nomes = {"Marina Duarte", "Rafael Campos", "Helena Moura", "Sandro Lima", "Patricia Nunes", "Leandro Costa", "Bianca Farias", "Gustavo Rocha"};
        List<Responsavel> responsaveis = new ArrayList<>();
        for (int i = 0; i < nomes.length; i++) {
            Responsavel responsavel = new Responsavel();
            responsavel.setOrganizacao(org);
            responsavel.setNome(nomes[i]);
            responsavel.setParentesco(i % 2 == 0 ? "Mae" : "Pai");
            responsavel.setEmail("responsavel" + (i + 1) + "@formaly.demo");
            responsavel.setTelefone("(47) 3000-10" + String.format("%02d", i));
            responsavel.setWhatsapp("(47) 99000-10" + String.format("%02d", i));
            responsavel.setContatoPrincipal(i % 3 == 0);
            responsavel.setObservacao("Responsavel ficticio para validacao de fluxo.");
            responsaveis.add(responsavelRepository.save(responsavel));
        }
        return responsaveis;
    }

    private List<TipoDocumento> ensureTiposDocumento(Organizacao org) {
        List<TipoDocumento> existentes = tipoDocumentoRepository.findByOrganizacaoIdOrderByNomeAsc(org.getId());
        if (!existentes.isEmpty()) return existentes;
        return List.of(
            tipo(org, "Documento de identidade", "Identificacao do formando.", true, true),
            tipo(org, "Autorizacao de uso de imagem", "Autorizacao para materiais do evento.", true, true),
            tipo(org, "Ficha cadastral", "Dados basicos revisados.", true, true),
            tipo(org, "Termo de responsavel legal", "Obrigatorio para menores.", false, true),
            tipo(org, "Comprovante de pagamento", "Comprovante financeiro vinculado ao pacote.", false, true),
            tipo(org, "Medidas da beca", "Informacoes para producao e conferencia da beca.", true, true)
        );
    }

    private TipoDocumento tipo(Organizacao org, String nome, String descricao, boolean obrigatorio, boolean menor) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setOrganizacao(org);
        tipo.setNome(nome);
        tipo.setDescricao(descricao);
        tipo.setObrigatorio(obrigatorio);
        tipo.setAplicavelMenorIdade(menor);
        tipo.setPermiteMultiplosArquivos(false);
        tipo.setExtensoesPermitidas("pdf,jpg,jpeg,png");
        tipo.setTamanhoMaximoBytes(5 * 1024 * 1024L);
        tipo.setStatus(StatusRegistro.ATIVO);
        return tipoDocumentoRepository.save(tipo);
    }

    private void ensureRequisitos(List<Turma> turmas, List<TipoDocumento> tipos) {
        for (Turma turma : turmas) {
            for (TipoDocumento tipo : tipos) {
                requisitoRepository.findByTurmaIdAndTipoDocumentoId(turma.getId(), tipo.getId()).orElseGet(() -> {
                    RequisitoDocumentoTurma requisito = new RequisitoDocumentoTurma();
                    requisito.setTurma(turma);
                    requisito.setTipoDocumento(tipo);
                    requisito.setObrigatorio(Boolean.TRUE.equals(tipo.getObrigatorio()));
                    requisito.setAtivo(true);
                    requisito.setStatus(StatusRegistro.ATIVO);
                    return requisitoRepository.save(requisito);
                });
            }
        }
    }

    private List<Aluno> ensureAlunos(List<Turma> turmas, List<Responsavel> responsaveis) {
        List<Aluno> existentes = alunoRepository.findByTurmaOrganizacaoIdOrderByNomeAsc(turmas.get(0).getOrganizacao().getId());
        if (existentes.size() >= 40) return existentes;
        String[] primeiros = {"Alice", "Bruno", "Carla", "Diego", "Elisa", "Felipe", "Gabriela", "Henrique", "Isabela", "Joao", "Karen", "Lucas", "Marina", "Nicolas", "Olivia", "Pedro", "Quelen", "Rafaela", "Samuel", "Tatiane", "Uriel", "Valentina", "Wesley", "Yasmin", "Andre", "Beatriz", "Caio", "Daniela", "Eduardo", "Fernanda", "Giovana", "Hugo", "Igor", "Julia", "Leonardo", "Manuela", "Natanael", "Paula", "Renato", "Sofia"};
        String[] sobrenomes = {"Almeida", "Barros", "Cardoso", "Dias", "Esteves", "Freitas", "Gomes", "Lopes"};
        List<Aluno> alunos = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Turma turma = turmas.get(i % turmas.size());
            String nome = primeiros[i] + " " + sobrenomes[i % sobrenomes.length];
            Aluno aluno = new Aluno();
            aluno.setNome(nome);
            aluno.setIdentificador("demo" + String.format("%03d", i + 1));
            aluno.setEmail("aluno" + String.format("%03d", i + 1) + "@formaly.demo");
            aluno.setTelefone("(47) 3200-" + String.format("%04d", 1000 + i));
            aluno.setWhatsapp("(47) 99000-" + String.format("%04d", 2000 + i));
            aluno.setContato(aluno.getEmail());
            aluno.setDataNascimento(LocalDate.now().minusYears(i % 5 == 0 ? 17 : 20).minusDays(i));
            aluno.setTurma(turma);
            aluno.setResponsavelLegal(i < responsaveis.size() * 2 ? responsaveis.get(i % responsaveis.size()) : null);
            aluno.setSituacaoEscolar(i % 9 == 0 ? SituacaoEscolar.PENDENTE : SituacaoEscolar.REGULAR);
            aluno.setSituacaoContratual(i % 11 == 0 ? SituacaoContratual.CONTRATO_PENDENTE : SituacaoContratual.CONTRATO_ASSINADO);
            aluno.setSituacaoBeca(i % 4 == 0 ? SituacaoBeca.MEDIDAS_PENDENTES : SituacaoBeca.MEDIDAS_ENVIADAS);
            aluno.setStatusDocumental(StatusDocumento.PENDENTE);
            aluno.setStatusCadastro(i % 7 == 0 ? SituacaoCadastro.INCOMPLETO : SituacaoCadastro.COMPLETO);
            aluno.setObservacaoInterna("Observacao interna ficticia para fluxo operacional.");
            aluno.setPrecisaTrocarSenha(true);
            aluno.setStatus("ATIVO");
            aluno.setAtivo(true);
            aluno.setDataInclusao(LocalDateTime.now().minusDays(40 - i));
            aluno.setDataUltimaAtualizacao(LocalDateTime.now().minusDays(i % 8));
            alunos.add(alunoRepository.save(aluno));
        }
        return alunos;
    }

    private void ensureDocumentos(List<Aluno> alunos, List<TipoDocumento> tipos) {
        if (!documentoRepository.findByAlunoTurmaOrganizacaoIdOrderByDataEnvioDesc(alunos.get(0).getTurma().getOrganizacao().getId()).isEmpty()) return;
        StatusDocumento[] statuses = {StatusDocumento.EM_ANALISE, StatusDocumento.APROVADO, StatusDocumento.REPROVADO, StatusDocumento.ENVIADO};
        for (int i = 0; i < alunos.size(); i++) {
            Aluno aluno = alunos.get(i);
            int quantidade = 2 + (i % tipos.size());
            boolean temReprovado = false;
            boolean temAnalise = false;
            boolean todosAprovados = true;
            for (int j = 0; j < quantidade && j < tipos.size(); j++) {
                StatusDocumento status = statuses[(i + j) % statuses.length];
                DocumentoAluno documento = new DocumentoAluno();
                documento.setAluno(aluno);
                documento.setTipoDocumento(tipos.get(j));
                documento.setNomeOriginal("documento-demo-" + aluno.getIdentificador() + "-" + j + ".pdf");
                documento.setNomeArmazenado("seed-" + aluno.getIdentificador() + "-" + j + ".pdf");
                documento.setReferenciaArquivo("seed/documentos/" + aluno.getIdentificador() + "-" + j + ".pdf");
                documento.setTamanho(128000L + (i * 100L));
                documento.setMimeType("application/pdf");
                documento.setDataEnvio(LocalDateTime.now().minusDays((i + j) % 12));
                documento.setStatus(status);
                documento.setDataAnalise(status == StatusDocumento.APROVADO || status == StatusDocumento.REPROVADO ? LocalDateTime.now().minusDays(j) : null);
                documento.setJustificativa(status == StatusDocumento.REPROVADO ? "Imagem ilegivel no arquivo demonstrativo." : "");
                documento.setObservacao("Registro ficticio de demonstracao.");
                documento.setVersao(1);
                documentoRepository.save(documento);
                if (status == StatusDocumento.REPROVADO) temReprovado = true;
                if (status == StatusDocumento.EM_ANALISE || status == StatusDocumento.ENVIADO) temAnalise = true;
                if (status != StatusDocumento.APROVADO) todosAprovados = false;
            }
            if (temReprovado) aluno.setStatusDocumental(StatusDocumento.REPROVADO);
            else if (temAnalise) aluno.setStatusDocumental(StatusDocumento.EM_ANALISE);
            else if (todosAprovados) aluno.setStatusDocumental(StatusDocumento.APROVADO);
            alunoRepository.save(aluno);
        }
    }

    private void ensureComprovantes(Organizacao org, List<Aluno> alunos) {
        if (!comprovanteRepository.findByOrganizacaoIdOrderByDataEnvioDesc(org.getId()).isEmpty()) return;
        StatusComprovante[] statuses = {StatusComprovante.EM_ANALISE, StatusComprovante.APROVADO, StatusComprovante.REPROVADO, StatusComprovante.PENDENTE};
        for (int i = 0; i < 18; i++) {
            Aluno aluno = alunos.get(i);
            Comprovante comprovante = new Comprovante();
            comprovante.setOrganizacao(org);
            comprovante.setTurma(aluno.getTurma());
            comprovante.setAluno(aluno);
            comprovante.setDescricao("Comprovante demonstrativo " + (i + 1));
            comprovante.setNomeOriginal("comprovante-demo-" + (i + 1) + ".pdf");
            comprovante.setNomeArmazenado("seed-comprovante-" + (i + 1) + ".pdf");
            comprovante.setReferenciaArquivo("seed/comprovantes/" + (i + 1) + ".pdf");
            comprovante.setTamanho(96000L + i);
            comprovante.setMimeType("application/pdf");
            comprovante.setDataEnvio(LocalDateTime.now().minusDays(i % 9));
            comprovante.setStatus(statuses[i % statuses.length]);
            comprovante.setComentario(comprovante.getStatus() == StatusComprovante.REPROVADO ? "Valor nao identificado no comprovante ficticio." : "");
            comprovanteRepository.save(comprovante);
        }
    }

    private void ensureEventos(Organizacao org, List<Turma> turmas) {
        if (!eventoRepository.findByOrganizacaoIdOrderByDataEventoAscNomeAsc(org.getId()).isEmpty()) return;
        for (int i = 0; i < 5; i++) {
            Turma turma = turmas.get(i % turmas.size());
            Evento evento = new Evento();
            evento.setOrganizacao(org);
            evento.setTurma(turma);
            evento.setNome("Marco operacional " + (i + 1));
            evento.setDescricao("Evento futuro ficticio para acompanhamento da turma.");
            evento.setTipo(i % 2 == 0 ? "REUNIAO_GERAL" : "PRAZO_IMPORTANTE");
            evento.setDataEvento(LocalDate.now().plusDays(7 + i * 9L));
            evento.setHorario(LocalTime.of(19, 0));
            evento.setLocalEvento("Local de demonstracao " + (i + 1));
            evento.setResponsavel("Equipe Operacional Demo");
            evento.setStatus("AGENDADO");
            eventoRepository.save(evento);
        }
    }

    private void ensureTarefas(Organizacao org, List<Turma> turmas, List<Aluno> alunos) {
        if (!tarefaRepository.findByOrganizacaoIdOrderByDataLimiteAsc(org.getId()).isEmpty()) return;
        for (int i = 0; i < 10; i++) {
            Tarefa tarefa = new Tarefa();
            tarefa.setOrganizacao(org);
            tarefa.setTurma(turmas.get(i % turmas.size()));
            tarefa.setResponsavel(alunos.get(i));
            tarefa.setTitulo("Validar pendencias da turma " + (i + 1));
            tarefa.setDescricao("Tarefa operacional ficticia para demonstracao.");
            tarefa.setStatus(i % 3 == 0 ? "em_andamento" : "a_fazer");
            tarefa.setDataLimite(LocalDate.now().plusDays(3 + i));
            tarefaRepository.save(tarefa);
        }
    }

    private void ensureUsuarios(Organizacao org, List<Turma> turmas, List<Aluno> alunos) {
        Usuario admin = ensureUsuario("admin.demo@formaly.local", "admin.demo@formaly.local", "Ana Administradora", adminPassword, Perfil.ROLE_ADMIN_ORGANIZACAO, org, null);
        Usuario colaborador = ensureUsuario("colaborador.demo@formaly.local", "colaborador.demo@formaly.local", "Carlos Operacional", colaboradorPassword, Perfil.ROLE_COLABORADOR, org, null);
        Usuario comissao = ensureUsuario("comissao.demo@formaly.local", "comissao.demo@formaly.local", "Lia Comissao", comissaoPassword, Perfil.ROLE_COMISSAO, org, turmas.get(0));
        ensureVinculo(admin, org, Perfil.ROLE_ADMIN_ORGANIZACAO, null);
        ensureVinculo(colaborador, org, Perfil.ROLE_COLABORADOR, null);
        ensureVinculo(comissao, org, Perfil.ROLE_COMISSAO, turmas.get(0));
        for (int i = 0; i < Math.min(6, alunos.size()); i++) {
            Aluno aluno = alunos.get(i);
            Usuario usuario = ensureUsuario(aluno.getIdentificador(), aluno.getEmail(), aluno.getNome(), alunoPassword, Perfil.ROLE_ALUNO, org, aluno.getTurma());
            usuario.setAluno(aluno);
            usuarioRepository.save(usuario);
            ensureVinculo(usuario, org, Perfil.ROLE_ALUNO, aluno.getTurma());
        }
    }

    private Usuario ensureUsuario(String login, String email, String nome, String password, Perfil perfil, Organizacao org, Turma turma) {
        return usuarioRepository.findUsuarioByLogin(login).or(() -> usuarioRepository.findUsuarioByEmail(email)).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setLogin(login);
            usuario.setEmail(email);
            usuario.setNome(nome);
            usuario.setSenha(passwordEncoder.encode(password));
            usuario.setPerfil(perfil);
            usuario.setOrganizacaoAtual(org);
            usuario.setAtivo(true);
            return usuarioRepository.save(usuario);
        });
    }

    private void ensureVinculo(Usuario usuario, Organizacao org, Perfil perfil, Turma turma) {
        boolean exists = usuarioOrganizacaoRepository.findByUsuarioIdAndAtivoTrue(usuario.getId()).stream()
            .anyMatch(v -> v.getOrganizacao() != null
                && org.getId().equals(v.getOrganizacao().getId())
                && ((turma == null && v.getTurmaPermitida() == null) || (turma != null && v.getTurmaPermitida() != null && turma.getId().equals(v.getTurmaPermitida().getId()))));
        if (exists) return;
        UsuarioOrganizacao vinculo = new UsuarioOrganizacao();
        vinculo.setUsuario(usuario);
        vinculo.setOrganizacao(org);
        vinculo.setPerfil(perfil);
        vinculo.setTurmaPermitida(turma);
        vinculo.setAtivo(true);
        usuarioOrganizacaoRepository.save(vinculo);
    }

    private void ensureHistorico(Organizacao org) {
        if (!historicoRepository.findTop20ByOrganizacaoIdOrderByDataHoraDesc(org.getId()).isEmpty()) return;
        for (int i = 0; i < 8; i++) {
            HistoricoAuditoria historico = new HistoricoAuditoria();
            historico.setOrganizacao(org);
            historico.setEntidade(i % 2 == 0 ? "DocumentoAluno" : "Aluno");
            historico.setEntidadeId(String.valueOf(i + 1));
            historico.setAcao(i % 2 == 0 ? "APROVACAO" : "ALTERACAO");
            historico.setDataHora(LocalDateTime.now().minusHours(i + 1));
            historico.setResumo("Atividade ficticia registrada para demonstracao.");
            historicoRepository.save(historico);
        }
    }
}
