package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;

@Service
public class TokenService {
    @Value("${app.security.jwt-secret:}")
    private String secret;
    @Value("${app.security.jwt-issuer:Formaly Business API}")
    private String issuer;
    @Value("${app.security.jwt-expiration-hours:2}")
    private long expirationHours;

    public String gerarToken(Usuario usuario) {
        try {
            validarConfiguracao();
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(usuario.getUsername())
                    .withClaim("id", usuario.getId())
                    .withClaim("perfil", usuario.getPerfil().name())
                    .withClaim("organizacaoId", usuario.getOrganizacaoAtual() != null ? usuario.getOrganizacaoAtual().getId() : null)
                    .withExpiresAt(dataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token jwt", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            validarConfiguracao();
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer(issuer)
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(expirationHours).toInstant(ZoneOffset.of("-03:00"));
    }

    private void validarConfiguracao() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Segredo JWT nao configurado.");
        }
    }
}
