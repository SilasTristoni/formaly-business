package br.com.senac.formatura.sistema_gerenciamento_formaturas.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Aluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.DocumentoAluno;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Instituicao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Organizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.RequisitoDocumentoTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusRegistro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusTurma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.TipoDocumento;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Turma;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.UsuarioOrganizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.AlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.ComprovanteRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.DocumentoAlunoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.HistoricoAuditoriaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.InstituicaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.OrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RequisitoDocumentoTurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.RevisaoDocumentoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TipoDocumentoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.TurmaRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioOrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.storage.local-path=${java.io.tmpdir}/formaly-business-test-uploads")
class BusinessControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired OrganizacaoRepository organizacaoRepository;
    @Autowired InstituicaoRepository instituicaoRepository;
    @Autowired TurmaRepository turmaRepository;
    @Autowired AlunoRepository alunoRepository;
    @Autowired TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired RequisitoDocumentoTurmaRepository requisitoRepository;
    @Autowired DocumentoAlunoRepository documentoRepository;
    @Autowired RevisaoDocumentoRepository revisaoRepository;
    @Autowired ComprovanteRepository comprovanteRepository;
    @Autowired HistoricoAuditoriaRepository historicoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired UsuarioOrganizacaoRepository usuarioOrganizacaoRepository;

    Organizacao orgA;
    Organizacao orgB;
    Turma turmaA;
    Turma turmaB;
    Aluno alunoA;
    TipoDocumento tipoA;
    ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        revisaoRepository.deleteAll();
        documentoRepository.deleteAll();
        comprovanteRepository.deleteAll();
        historicoRepository.deleteAll();
        requisitoRepository.deleteAll();
        usuarioOrganizacaoRepository.deleteAll();
        usuarioRepository.deleteAll();
        alunoRepository.deleteAll();
        tipoDocumentoRepository.deleteAll();
        turmaRepository.deleteAll();
        instituicaoRepository.deleteAll();
        organizacaoRepository.deleteAll();

        orgA = organizacao("Org A");
        orgB = organizacao("Org B");
        Instituicao instA = instituicao(orgA, "Instituicao A");
        Instituicao instB = instituicao(orgB, "Instituicao B");
        turmaA = turma(orgA, instA, "Turma A");
        turmaB = turma(orgB, instB, "Turma B");
        alunoA = aluno(turmaA, "Aluno A", "aluno.a");
        tipoA = tipo(orgA, "RG");
        requisito(turmaA, tipoA);
        usuario("admin.a@demo", "Admin A", Perfil.ROLE_ADMIN_ORGANIZACAO, orgA, null, null);
        usuario("admin.b@demo", "Admin B", Perfil.ROLE_ADMIN_ORGANIZACAO, orgB, null, null);
    }

    @Test
    void adminNaoAcessaTurmaDeOutraOrganizacao() throws Exception {
        String token = login("admin.a@demo");

        mockMvc.perform(get("/api/business/alunos")
                .param("turmaId", String.valueOf(turmaB.getId()))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void dashboardUsaDadosDoBanco() throws Exception {
        DocumentoAluno documento = new DocumentoAluno();
        documento.setAluno(alunoA);
        documento.setTipoDocumento(tipoA);
        documento.setNomeOriginal("rg.pdf");
        documento.setNomeArmazenado("rg.pdf");
        documento.setReferenciaArquivo("seed/rg.pdf");
        documento.setTamanho(100L);
        documento.setMimeType("application/pdf");
        documento.setDataEnvio(LocalDateTime.now());
        documento.setStatus(StatusDocumento.EM_ANALISE);
        documento.setVersao(1);
        documentoRepository.save(documento);

        String token = login("admin.a@demo");

        mockMvc.perform(get("/api/business/dashboard")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metrics[0].label").value("Turmas ativas"))
            .andExpect(jsonPath("$.statusDocumentais.EM_ANALISE").value(1));
    }

    @Test
    void uploadEAprovacaoDeDocumentoPreservamHistorico() throws Exception {
        String token = login("admin.a@demo");
        MockMultipartFile file = new MockMultipartFile("arquivo", "rg.pdf", "application/pdf", "conteudo".getBytes());

        String uploadBody = mockMvc.perform(multipart("/api/business/alunos/{alunoId}/documentos/{tipoId}/upload", alunoA.getId(), tipoA.getId())
                .file(file)
                .param("observacao", "envio teste")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EM_ANALISE"))
            .andReturn().getResponse().getContentAsString();

        Long documentoId = objectMapper.readTree(uploadBody).get("id").asLong();

        mockMvc.perform(post("/api/business/documentos/{id}/analisar", documentoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("status", "APROVADO", "observacao", "ok")))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APROVADO"))
            .andExpect(jsonPath("$.historico.length()").value(2));
    }

    @Test
    void importacaoDetectaDuplicidadeEIgnoraQuandoConfigurado() throws Exception {
        String token = login("admin.a@demo");
        String csv = "nome;identificador;email\nAluno Novo;novo;novo@demo\nAluno Duplicado;aluno.a;dup@demo\n";
        MockMultipartFile file = new MockMultipartFile("arquivo", "alunos.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/api/business/importacoes/alunos/preview")
                .file(file)
                .param("turmaId", String.valueOf(turmaA.getId()))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.duplicados").value(1));

        MockMultipartFile fileConfirm = new MockMultipartFile("arquivo", "alunos.csv", "text/csv", csv.getBytes());
        mockMvc.perform(multipart("/api/business/importacoes/alunos/confirmar")
                .file(fileConfirm)
                .param("turmaId", String.valueOf(turmaA.getId()))
                .param("estrategia", "IGNORAR_DUPLICADOS")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.importados").value(1))
            .andExpect(jsonPath("$.ignorados").value(1));
    }

    private String login(String login) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of("login", login, "senha", "senha-teste"))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private Organizacao organizacao(String nome) {
        Organizacao org = new Organizacao();
        org.setNome(nome);
        org.setStatus(StatusRegistro.ATIVO);
        return organizacaoRepository.save(org);
    }

    private Instituicao instituicao(Organizacao org, String nome) {
        Instituicao instituicao = new Instituicao();
        instituicao.setOrganizacao(org);
        instituicao.setNome(nome);
        instituicao.setStatus(StatusRegistro.ATIVO);
        return instituicaoRepository.save(instituicao);
    }

    private Turma turma(Organizacao org, Instituicao instituicao, String nome) {
        Turma turma = new Turma();
        turma.setOrganizacao(org);
        turma.setInstituicaoEntidade(instituicao);
        turma.setInstituicao(instituicao.getNome());
        turma.setNome(nome);
        turma.setCurso("Curso");
        turma.setAnoSemestre("2026");
        turma.setStatusTurma(StatusTurma.ATIVA);
        turma.setStatus("ATIVA");
        return turmaRepository.save(turma);
    }

    private Aluno aluno(Turma turma, String nome, String identificador) {
        Aluno aluno = new Aluno();
        aluno.setTurma(turma);
        aluno.setNome(nome);
        aluno.setIdentificador(identificador);
        aluno.setEmail(identificador + "@demo");
        aluno.setDataInclusao(LocalDateTime.now());
        aluno.setDataUltimaAtualizacao(LocalDateTime.now());
        return alunoRepository.save(aluno);
    }

    private TipoDocumento tipo(Organizacao org, String nome) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setOrganizacao(org);
        tipo.setNome(nome);
        tipo.setExtensoesPermitidas("pdf");
        tipo.setTamanhoMaximoBytes(1024L);
        tipo.setStatus(StatusRegistro.ATIVO);
        return tipoDocumentoRepository.save(tipo);
    }

    private void requisito(Turma turma, TipoDocumento tipo) {
        RequisitoDocumentoTurma requisito = new RequisitoDocumentoTurma();
        requisito.setTurma(turma);
        requisito.setTipoDocumento(tipo);
        requisito.setObrigatorio(true);
        requisito.setAtivo(true);
        requisitoRepository.save(requisito);
    }

    private void usuario(String login, String nome, Perfil perfil, Organizacao org, Turma turma, Aluno aluno) {
        Usuario usuario = new Usuario();
        usuario.setLogin(login);
        usuario.setEmail(login);
        usuario.setNome(nome);
        usuario.setPerfil(perfil);
        usuario.setSenha(passwordEncoder.encode("senha-teste"));
        usuario.setOrganizacaoAtual(org);
        usuario.setAluno(aluno);
        usuario = usuarioRepository.save(usuario);
        UsuarioOrganizacao vinculo = new UsuarioOrganizacao();
        vinculo.setUsuario(usuario);
        vinculo.setOrganizacao(org);
        vinculo.setTurmaPermitida(turma);
        vinculo.setPerfil(perfil);
        vinculo.setAtivo(true);
        usuarioOrganizacaoRepository.save(vinculo);
        assertThat(usuario.getId()).isNotNull();
    }
}
