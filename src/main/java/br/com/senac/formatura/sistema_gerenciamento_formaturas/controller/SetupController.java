package br.com.senac.formatura.sistema_gerenciamento_formaturas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Organizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.StatusRegistro;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.UsuarioOrganizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.OrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioOrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/setup")
public class SetupController {
    private final OrganizacaoRepository organizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioOrganizacaoRepository usuarioOrganizacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(
        OrganizacaoRepository organizacaoRepository,
        UsuarioRepository usuarioRepository,
        UsuarioOrganizacaoRepository usuarioOrganizacaoRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioOrganizacaoRepository = usuarioOrganizacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/status")
    public SetupStatus status() {
        long organizacoes = organizacaoRepository.count();
        long usuarios = usuarioRepository.count();
        return new SetupStatus(organizacoes == 0 && usuarios == 0, organizacoes, usuarios);
    }

    @PostMapping("/initialize")
    @Transactional
    public synchronized SetupResult initialize(@RequestBody SetupRequest request) {
        if (organizacaoRepository.count() > 0 || usuarioRepository.count() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A configuracao inicial ja foi concluida ou o banco possui dados.");
        }

        String nomeEmpresa = required(request.organizationName(), "Informe o nome da empresa.");
        String nomeAdmin = required(request.adminName(), "Informe o nome do administrador.");
        String emailAdmin = required(request.adminEmail(), "Informe o email do administrador.").toLowerCase();
        String senhaAdmin = required(request.adminPassword(), "Informe a senha do administrador.");

        if (!emailAdmin.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um email valido para o administrador.");
        }
        if (senhaAdmin.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve possuir pelo menos 8 caracteres.");
        }

        Organizacao organizacao = new Organizacao();
        organizacao.setNome(nomeEmpresa);
        organizacao.setNomeFantasia(blankToNull(request.tradeName()));
        organizacao.setStatus(StatusRegistro.ATIVO);
        organizacao.setObservacoes("Organizacao criada pelo assistente de primeira execucao.");
        organizacao = organizacaoRepository.save(organizacao);

        Usuario admin = new Usuario();
        admin.setLogin(emailAdmin);
        admin.setEmail(emailAdmin);
        admin.setNome(nomeAdmin);
        admin.setSenha(passwordEncoder.encode(senhaAdmin));
        admin.setPerfil(Perfil.ROLE_ADMIN_ORGANIZACAO);
        admin.setOrganizacaoAtual(organizacao);
        admin.setAtivo(true);
        admin = usuarioRepository.save(admin);

        UsuarioOrganizacao vinculo = new UsuarioOrganizacao();
        vinculo.setUsuario(admin);
        vinculo.setOrganizacao(organizacao);
        vinculo.setPerfil(Perfil.ROLE_ADMIN_ORGANIZACAO);
        vinculo.setAtivo(true);
        usuarioOrganizacaoRepository.save(vinculo);

        return new SetupResult(organizacao.getId(), admin.getId(), emailAdmin);
    }

    private String required(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isBlank()) return null;
        return value.trim();
    }

    public record SetupStatus(boolean required, long organizations, long users) {}

    public record SetupRequest(
        String organizationName,
        String tradeName,
        String adminName,
        String adminEmail,
        String adminPassword
    ) {}

    public record SetupResult(Long organizationId, Long adminUserId, String adminLogin) {}
}
