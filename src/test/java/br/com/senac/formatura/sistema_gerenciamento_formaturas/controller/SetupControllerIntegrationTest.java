package br.com.senac.formatura.sistema_gerenciamento_formaturas.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Perfil;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.OrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioOrganizacaoRepository;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SetupControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired OrganizacaoRepository organizacaoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired UsuarioOrganizacaoRepository usuarioOrganizacaoRepository;

    ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void primeiraExecucaoCriaOrganizacaoAdministradorEPermiteLogin() throws Exception {
        mockMvc.perform(get("/api/setup/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.required").value(true));

        var request = new SetupController.SetupRequest(
            "Empresa Teste",
            "Empresa",
            "Administrador Teste",
            "admin@empresa.test",
            "SenhaTeste123!"
        );

        mockMvc.perform(post("/api/setup/initialize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.adminLogin").value("admin@empresa.test"));

        assertThat(organizacaoRepository.count()).isEqualTo(1);
        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(usuarioOrganizacaoRepository.count()).isEqualTo(1);
        assertThat(usuarioRepository.findUsuarioByEmail("admin@empresa.test")).get()
            .extracting(usuario -> usuario.getPerfil())
            .isEqualTo(Perfil.ROLE_ADMIN_ORGANIZACAO);

        mockMvc.perform(get("/api/setup/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.required").value(false));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"admin@empresa.test\",\"senha\":\"SenhaTeste123!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.perfil").value("ROLE_ADMIN_ORGANIZACAO"));
    }

    @Test
    void configuracaoInicialNaoPodeSerExecutadaDuasVezes() throws Exception {
        var request = new SetupController.SetupRequest(
            "Empresa Teste",
            null,
            "Administrador Teste",
            "admin@empresa.test",
            "SenhaTeste123!"
        );

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/setup/initialize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/setup/initialize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict());
    }
}
