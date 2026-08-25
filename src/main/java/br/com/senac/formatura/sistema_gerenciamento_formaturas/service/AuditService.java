package br.com.senac.formatura.sistema_gerenciamento_formaturas.service;

import org.springframework.stereotype.Service;

import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.HistoricoAuditoria;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Organizacao;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.model.Usuario;
import br.com.senac.formatura.sistema_gerenciamento_formaturas.repository.HistoricoAuditoriaRepository;

@Service
public class AuditService {
    private final HistoricoAuditoriaRepository historicoRepository;

    public AuditService(HistoricoAuditoriaRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public void registrar(Organizacao organizacao, Usuario usuario, String entidade, Object entidadeId, String acao, String resumo) {
        HistoricoAuditoria historico = new HistoricoAuditoria();
        historico.setOrganizacao(organizacao);
        historico.setUsuario(usuario);
        historico.setEntidade(entidade);
        historico.setEntidadeId(entidadeId == null ? null : String.valueOf(entidadeId));
        historico.setAcao(acao);
        historico.setResumo(resumo == null ? "" : resumo);
        historicoRepository.save(historico);
    }
}
