import { api } from './services/api.js';
import { auth } from './services/auth.js';

const state = {
  me: null,
  screen: 'dashboard',
  instituicoes: [],
  turmas: [],
  alunos: [],
  documentos: [],
  comprovantes: [],
  tipos: [],
  dashboard: null,
  importFile: null,
  report: null
};

const titles = {
  dashboard: 'Dashboard operacional',
  instituicoes: 'Instituições',
  turmas: 'Turmas',
  alunos: 'Alunos',
  documentos: 'Documentos',
  comprovantes: 'Comprovantes',
  importacao: 'Importação de alunos',
  relatorios: 'Relatórios',
  permissoes: 'Permissões'
};

const statusTone = {
  ATIVO: 'success',
  ATIVA: 'success',
  COMPLETO: 'success',
  APROVADO: 'success',
  CONCLUIDA: 'success',
  EM_ANALISE: 'info',
  ENVIADO: 'info',
  PLANEJAMENTO: 'info',
  PENDENTE: 'warning',
  INCOMPLETO: 'warning',
  REPROVADO: 'danger',
  CANCELADA: 'danger'
};

document.addEventListener('DOMContentLoaded', init);

async function init() {
  bindShellEvents();
  try {
    state.me = await api.me();
    if (state.me.perfil === 'ROLE_ALUNO') {
      window.location.href = './aluno.html';
      return;
    }
    renderUser();
    await loadReferenceData();
    await loadScreen('dashboard');
    document.body.classList.remove('auth-pending');
  } catch (error) {
    handleError(error);
  }
}

function bindShellEvents() {
  document.querySelectorAll('.nav-button').forEach(button => {
    button.addEventListener('click', () => loadScreen(button.dataset.screen));
  });
  document.getElementById('logoutBtn')?.addEventListener('click', () => {
    auth.logout();
    window.location.href = './login.html';
  });
  document.getElementById('refreshBtn')?.addEventListener('click', () => loadScreen(state.screen, true));
  document.getElementById('menuToggle')?.addEventListener('click', () => document.body.classList.toggle('menu-open'));
  document.getElementById('sidebarCollapse')?.addEventListener('click', () => document.body.classList.toggle('sidebar-collapsed'));
  document.getElementById('globalInstituicaoFilter')?.addEventListener('change', async () => {
    populateTurmaFilters();
    await loadScreen(state.screen, true);
  });
  document.getElementById('globalTurmaFilter')?.addEventListener('change', () => loadScreen(state.screen, true));
  document.getElementById('instituicaoBusca')?.addEventListener('input', renderInstituicoes);
  document.getElementById('alunoBusca')?.addEventListener('input', renderAlunos);
  document.getElementById('turmaStatusFilter')?.addEventListener('change', () => loadScreen('turmas', true));
  document.getElementById('documentoStatusFilter')?.addEventListener('change', () => loadScreen('documentos', true));
  document.getElementById('comprovanteStatusFilter')?.addEventListener('change', () => loadScreen('comprovantes', true));
  document.getElementById('importForm')?.addEventListener('submit', previewImport);
  document.getElementById('confirmImportBtn')?.addEventListener('click', confirmImport);
  document.getElementById('generateReportBtn')?.addEventListener('click', loadReport);
  document.getElementById('exportReportBtn')?.addEventListener('click', exportReport);
  document.body.addEventListener('click', handleAction);
}

async function loadReferenceData() {
  const [instituicoes, turmas, tipos] = await Promise.all([
    api.businessBuscar('/instituicoes'),
    api.businessBuscar('/turmas'),
    api.businessBuscar('/tipos-documentos')
  ]);
  state.instituicoes = instituicoes;
  state.turmas = turmas;
  state.tipos = tipos;
  populateFilters();
}

async function loadScreen(screen, keepRefs = false) {
  state.screen = screen;
  document.body.classList.remove('menu-open');
  document.querySelectorAll('.screen').forEach(item => item.classList.toggle('is-active', item.id === `screen-${screen}`));
  document.querySelectorAll('.nav-button').forEach(item => item.classList.toggle('is-active', item.dataset.screen === screen));
  setText('pageTitle', titles[screen] || 'Formaly Business');
  setText('breadcrumbs', `Formaly Business / ${titles[screen] || screen}`);
  if (!keepRefs && screen !== 'dashboard') await loadReferenceData();

  try {
    if (screen === 'dashboard') await loadDashboard();
    if (screen === 'instituicoes') renderInstituicoes();
    if (screen === 'turmas') await loadTurmas();
    if (screen === 'alunos') await loadAlunos();
    if (screen === 'documentos') await loadDocumentos();
    if (screen === 'comprovantes') await loadComprovantes();
    if (screen === 'importacao') renderImportScreen();
    if (screen === 'relatorios') await loadReport();
    if (screen === 'permissoes') renderPermissions();
    enhanceResponsiveTables();
  } catch (error) {
    handleError(error);
  }
}

async function loadDashboard() {
  const dashboardState = document.getElementById('dashboardState');
  if (dashboardState) dashboardState.hidden = false;
  setHtml('dashboardState', 'Carregando indicadores...');
  state.dashboard = await api.businessBuscar('/dashboard', currentFilters());
  if (dashboardState) dashboardState.hidden = true;
  renderMetrics('metricsGrid', state.dashboard.metrics);
  renderTurmasPendencias();
  renderStatusDocumentais();
  renderAtividades();
  renderEventos();
}

async function loadTurmas() {
  state.turmas = await api.businessBuscar('/turmas', { instituicaoId: currentFilters().instituicaoId, status: valueOf('turmaStatusFilter') });
  populateFilters();
  renderTurmas();
}

async function loadAlunos() {
  state.alunos = await api.businessBuscar('/alunos', { turmaId: currentFilters().turmaId, busca: valueOf('alunoBusca') });
  renderAlunos();
}

async function loadDocumentos() {
  const turmaId = currentFilters().turmaId;
  const status = valueOf('documentoStatusFilter');
  const [documentos, tipos, alunos] = await Promise.all([
    api.businessBuscar('/documentos', { turmaId, status }),
    api.businessBuscar('/tipos-documentos'),
    api.businessBuscar('/alunos', { turmaId })
  ]);
  state.documentos = documentos;
  state.tipos = tipos;
  state.alunos = alunos;
  renderDocumentos();
  renderTiposDocumentos();
}

async function loadComprovantes() {
  state.comprovantes = await api.businessBuscar('/comprovantes', { turmaId: currentFilters().turmaId, status: valueOf('comprovanteStatusFilter') });
  if (!state.alunos.length) state.alunos = await api.businessBuscar('/alunos', { turmaId: currentFilters().turmaId });
  renderComprovantes();
}

async function loadReport() {
  const tipo = valueOf('reportType') || 'alunos';
  state.report = await api.businessBuscar(`/relatorios/${tipo}`, { turmaId: currentFilters().turmaId });
  renderMetrics('reportSummary', state.report.resumo);
  renderReportTable();
}

function populateFilters() {
  fillSelect('globalInstituicaoFilter', [{ id: '', nome: 'Todas' }, ...state.instituicoes], 'id', 'nome');
  populateTurmaFilters();
  fillSelect('importTurma', state.turmas, 'id', 'nome');
}

function populateTurmaFilters() {
  const instId = valueOf('globalInstituicaoFilter');
  const turmas = state.turmas.filter(t => !instId || String(t.instituicaoId) === String(instId));
  const current = valueOf('globalTurmaFilter');
  fillSelect('globalTurmaFilter', [{ id: '', nome: 'Todas' }, ...turmas], 'id', 'nome');
  if (current && turmas.some(t => String(t.id) === current)) document.getElementById('globalTurmaFilter').value = current;
}

function renderUser() {
  setText('userNameDisplay', state.me.nome || 'Usuario');
  setText('userRoleDisplay', roleLabel(state.me.perfil));
  setText('userLoginDisplay', state.me.login || state.me.email || '');
}

function renderMetrics(containerId, metrics = []) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = metrics.length ? metrics.map(metric => `
    <article class="metric-card" data-tone="${escapeHtml(metric.tone || 'neutral')}">
      <div>
        <span>${escapeHtml(metric.label)}</span>
        <strong>${escapeHtml(metric.value)}</strong>
        <small>${escapeHtml(metric.hint || '')}</small>
      </div>
      <div class="metric-card__icon" aria-hidden="true"><i class="ph ${metricIcon(metric.label)}"></i></div>
    </article>
  `).join('') : emptyHtml('Sem indicadores para este filtro.');
}

function renderTurmasPendencias() {
  setList('turmasPendencias', state.dashboard.turmasComPendencias, item => `
    <div class="activity-item">
      <strong>${escapeHtml(item.turma)}</strong>
      <span>${escapeHtml(item.instituicao)} | ${item.pendencias} pendencias | ${Number(item.documentacaoConcluida || 0).toFixed(0)}% concluido</span>
    </div>
  `, 'Nenhuma pendencia encontrada.');
}

function renderStatusDocumentais() {
  const items = Object.entries(state.dashboard.statusDocumentais || {});
  setList('statusDocumentais', items, ([status, total]) => `
    <div class="activity-item">
      <strong>${badge(status)}</strong>
      <span>${total} registros</span>
    </div>
  `, 'Sem documentos no filtro.');
}

function renderAtividades() {
  setList('ultimasAtividades', state.dashboard.ultimasAtividades, item => `
    <div class="activity-item">
      <strong>${escapeHtml(item.acao)} em ${escapeHtml(item.entidade)}</strong>
      <span>${escapeHtml(item.resumo || '')}</span>
      <span>${escapeHtml(item.usuario || 'Sistema')} | ${formatDateTime(item.dataHora)}</span>
    </div>
  `, 'Sem atividades registradas.');
}

function renderEventos() {
  const destaque = state.dashboard.proximosEventos?.[0];
  setText('dashboardHeroEvent', destaque
    ? `${destaque.nome} | ${formatDate(destaque.dataEvento)} | ${destaque.turma || 'Turma a definir'}`
    : 'Nenhum evento próximo no filtro atual.');
  setList('proximosEventos', state.dashboard.proximosEventos, item => `
    <div class="activity-item">
      <strong>${escapeHtml(item.nome)}</strong>
      <span>${escapeHtml(item.turma || '')} | ${formatDate(item.dataEvento)} | ${escapeHtml(item.local || 'Local a definir')}</span>
    </div>
  `, 'Sem eventos futuros.');
}

function renderInstituicoes() {
  const busca = normalize(valueOf('instituicaoBusca'));
  const rows = state.instituicoes.filter(item => !busca || normalize(`${item.nome} ${item.cidade}`).includes(busca));
  setTable('instituicoesBody', rows, item => `
    <tr>
      <td><strong>${escapeHtml(item.nome)}</strong><br><span class="muted">${escapeHtml(item.nomeAbreviado || '')}</span></td>
      <td>${escapeHtml([item.cidade, item.estado].filter(Boolean).join(' / '))}</td>
      <td>${escapeHtml(item.contato || '-')}</td>
      <td>${badge(item.status)}</td>
      <td>${item.turmas}</td>
      <td><div class="table-actions"><button class="icon-button" data-action="editar-instituicao" data-id="${item.id}" aria-label="Editar"><i class="ph ph-pencil"></i></button><button class="icon-button" data-action="toggle-instituicao" data-id="${item.id}" aria-label="Ativar ou inativar"><i class="ph ph-power"></i></button></div></td>
    </tr>
  `, 6);
}

function renderTurmas() {
  setTable('turmasBody', state.turmas, item => `
    <tr>
      <td><strong>${escapeHtml(item.nome)}</strong><br><span class="muted">${escapeHtml(item.anoSemestre || '')}</span></td>
      <td>${escapeHtml(item.instituicao || '-')}</td>
      <td>${escapeHtml(item.curso || '-')}</td>
      <td>${formatDate(item.dataPrevistaFormatura)}</td>
      <td>${item.quantidadeAlunos}</td>
      <td>${Number(item.percentualDocumentacao || 0).toFixed(0)}%</td>
      <td>${item.pendencias}</td>
      <td>${badge(item.status)}</td>
      <td><div class="table-actions"><button class="icon-button" data-action="editar-turma" data-id="${item.id}" aria-label="Editar"><i class="ph ph-pencil"></i></button><button class="icon-button" data-action="abrir-turma" data-id="${item.id}" aria-label="Abrir"><i class="ph ph-folder-open"></i></button></div></td>
    </tr>
  `, 9);
}

function renderAlunos() {
  const busca = normalize(valueOf('alunoBusca'));
  const rows = state.alunos.filter(item => !busca || normalize(`${item.nome} ${item.identificador} ${item.email}`).includes(busca));
  setTable('alunosBody', rows, item => `
    <tr>
      <td><strong>${escapeHtml(item.nome)}</strong><br><span class="muted">@${escapeHtml(item.identificador || '')}</span></td>
      <td>${escapeHtml(item.turma || '-')}</td>
      <td>${escapeHtml(item.email || item.whatsapp || item.telefone || '-')}</td>
      <td>${badge(item.statusCadastro)}</td>
      <td>${badge(item.statusDocumental)}</td>
      <td>${badge(item.situacaoBeca)}</td>
      <td><div class="table-actions"><button class="icon-button" data-action="editar-aluno" data-id="${item.id}" aria-label="Editar"><i class="ph ph-pencil"></i></button><button class="icon-button" data-action="checklist-aluno" data-id="${item.id}" aria-label="Checklist"><i class="ph ph-list-checks"></i></button></div></td>
    </tr>
  `, 7);
}

function renderDocumentos() {
  setList('documentosList', state.documentos, item => `
    <div class="document-item">
      <div><strong>${escapeHtml(item.tipoDocumento)}</strong> ${badge(item.status)}</div>
      <span>${escapeHtml(item.aluno)} | versao ${item.versao || 1} | ${formatDateTime(item.dataEnvio)}</span>
      ${item.justificativa ? `<span>${escapeHtml(item.justificativa)}</span>` : ''}
      <div class="table-actions">
        <button class="button" data-action="aprovar-documento" data-id="${item.id}"><i class="ph ph-check"></i> Aprovar</button>
        <button class="button" data-action="reprovar-documento" data-id="${item.id}"><i class="ph ph-x"></i> Reprovar</button>
      </div>
    </div>
  `, 'Nenhum documento encontrado para o filtro.');
}

function renderTiposDocumentos() {
  setList('tiposDocumentosList', state.tipos, item => `
    <div class="activity-item">
      <strong>${escapeHtml(item.nome)} ${badge(item.status)}</strong>
      <span>${escapeHtml(item.extensoesPermitidas)} | limite ${formatBytes(item.tamanhoMaximoBytes)}</span>
    </div>
  `, 'Nenhum tipo documental configurado.');
}

function renderComprovantes() {
  setList('comprovantesList', state.comprovantes, item => `
    <div class="document-item">
      <div><strong>${escapeHtml(item.descricao || 'Comprovante')}</strong> ${badge(item.status)}</div>
      <span>${escapeHtml(item.aluno || 'Sem aluno')} | ${escapeHtml(item.turma || '')} | ${formatDateTime(item.dataEnvio)}</span>
      ${item.comentario ? `<span>${escapeHtml(item.comentario)}</span>` : ''}
      <div class="table-actions">
        <button class="button" data-action="aprovar-comprovante" data-id="${item.id}"><i class="ph ph-check"></i> Aprovar</button>
        <button class="button" data-action="reprovar-comprovante" data-id="${item.id}"><i class="ph ph-x"></i> Reprovar</button>
      </div>
    </div>
  `, 'Nenhum comprovante encontrado.');
}

function renderImportScreen() {
  fillSelect('importTurma', state.turmas, 'id', 'nome');
}

function renderReportTable() {
  const table = document.getElementById('reportTable');
  const rows = state.report?.linhas || [];
  if (!table) return;
  const headers = rows.length ? Object.keys(rows[0]) : [];
  table.querySelector('thead').innerHTML = headers.length ? `<tr>${headers.map(h => `<th>${escapeHtml(h)}</th>`).join('')}</tr>` : '';
  table.querySelector('tbody').innerHTML = rows.length
    ? rows.map(row => `<tr>${headers.map(h => `<td>${escapeHtml(row[h] || '')}</td>`).join('')}</tr>`).join('')
    : `<tr><td colspan="1">Sem registros para o filtro.</td></tr>`;
}

function renderPermissions() {
  const rows = [
    ['ADMIN_ORGANIZACAO', 'Instituicoes, turmas, colaboradores, alunos, documentos, relatorios e requisitos.', 'Escopo limitado a organizacao atual.'],
    ['COLABORADOR', 'Turmas autorizadas, alunos, documentos, comprovantes e relatorios permitidos.', 'Nao gerencia configuracoes globais.'],
    ['COMISSAO', 'Indicadores e pendencias agregadas da propria turma.', 'Sem observacoes internas e sem documentos privados sem autorizacao.'],
    ['ALUNO', 'Proprio cadastro, documentos, comprovantes, eventos e pendencias.', 'Sem acesso a dados de outros alunos.']
  ];
  setTable('permissionsBody', rows, row => `<tr><td>${badge(row[0])}</td><td>${escapeHtml(row[1])}</td><td>${escapeHtml(row[2])}</td></tr>`, 3);
}

async function handleAction(event) {
  const button = event.target.closest('[data-action]');
  if (!button) return;
  const action = button.dataset.action;
  const id = Number(button.dataset.id || 0);
  if (action === 'nova-instituicao') return openInstituicaoForm();
  if (action === 'editar-instituicao') return openInstituicaoForm(state.instituicoes.find(i => i.id === id));
  if (action === 'toggle-instituicao') return toggleInstituicao(id);
  if (action === 'nova-turma') return openTurmaForm();
  if (action === 'editar-turma') return openTurmaForm(state.turmas.find(t => t.id === id));
  if (action === 'abrir-turma') {
    document.getElementById('globalTurmaFilter').value = String(id);
    return loadScreen('alunos', true);
  }
  if (action === 'novo-aluno') return openAlunoForm();
  if (action === 'editar-aluno') return openAlunoForm(state.alunos.find(a => a.id === id));
  if (action === 'checklist-aluno') return openChecklist(id);
  if (action === 'upload-documento') return openUploadDocumento();
  if (action === 'novo-tipo-documento') return openTipoDocumentoForm();
  if (action === 'aprovar-documento') return openAnalysis('documento', id, 'APROVADO');
  if (action === 'reprovar-documento') return openAnalysis('documento', id, 'REPROVADO');
  if (action === 'upload-comprovante') return openUploadComprovante();
  if (action === 'aprovar-comprovante') return openAnalysis('comprovante', id, 'APROVADO');
  if (action === 'reprovar-comprovante') return openAnalysis('comprovante', id, 'REPROVADO');
}

function openInstituicaoForm(item = {}) {
  openEntityDialog('Instituição', `
    ${input('nome', 'Nome', item.nome, true)}
    ${input('nomeAbreviado', 'Nome abreviado', item.nomeAbreviado)}
    ${input('cidade', 'Cidade', item.cidade)}
    ${input('estado', 'Estado', item.estado)}
    ${input('contato', 'Contato', item.contato)}
    ${select('status', 'Status', ['ATIVO', 'INATIVO'], item.status || 'ATIVO')}
    ${textarea('observacoes', 'Observações', item.observacoes)}
  `, async form => {
    await api.businessSalvar(item.id ? `/instituicoes/${item.id}` : '/instituicoes', formToObject(form), item.id ? 'PUT' : 'POST');
    toast('Instituicao salva.', 'success');
    await loadReferenceData();
    await loadScreen('instituicoes', true);
  });
}

function openTurmaForm(item = {}) {
  openEntityDialog('Turma', `
    ${select('instituicaoId', 'Instituição', state.instituicoes.map(i => [i.id, i.nome]), item.instituicaoId, true)}
    ${input('nome', 'Nome da turma', item.nome, true)}
    ${input('curso', 'Curso ou série', item.curso, true)}
    ${input('anoSemestre', 'Ano ou semestre', item.anoSemestre)}
    ${input('dataPrevistaFormatura', 'Data prevista da formatura', item.dataPrevistaFormatura, false, 'date')}
    ${input('responsavelComercial', 'Responsável comercial', item.responsavelComercial)}
    ${input('responsavelOperacional', 'Responsável operacional', item.responsavelOperacional)}
    ${input('representante', 'Representante', item.representante)}
    ${select('status', 'Status', ['PLANEJAMENTO', 'ATIVA', 'EM_FINALIZACAO', 'CONCLUIDA', 'CANCELADA'], item.status || 'ATIVA')}
  `, async form => {
    await api.businessSalvar(item.id ? `/turmas/${item.id}` : '/turmas', formToObject(form), item.id ? 'PUT' : 'POST');
    toast('Turma salva.', 'success');
    await loadReferenceData();
    await loadScreen('turmas', true);
  });
}

function openAlunoForm(item = {}) {
  openEntityDialog('Aluno', `
    ${input('nome', 'Nome completo', item.nome, true)}
    ${input('identificador', 'Identificador', item.identificador)}
    ${select('turmaId', 'Turma', state.turmas.map(t => [t.id, t.nome]), item.turmaId || valueOf('globalTurmaFilter'), true)}
    ${input('email', 'E-mail', item.email)}
    ${input('telefone', 'Telefone', item.telefone)}
    ${input('whatsapp', 'WhatsApp', item.whatsapp)}
    ${input('dataNascimento', 'Data de nascimento', item.dataNascimento, false, 'date')}
    ${select('situacaoEscolar', 'Situação escolar', ['REGULAR', 'PENDENTE', 'CONCLUIDO', 'DESISTENTE'], item.situacaoEscolar || 'REGULAR')}
    ${select('situacaoContratual', 'Situação contratual', ['NAO_INFORMADA', 'CONTRATO_PENDENTE', 'CONTRATO_ASSINADO', 'INADIMPLENTE', 'CANCELADO'], item.situacaoContratual || 'NAO_INFORMADA')}
    ${select('situacaoBeca', 'Situação da beca', ['NAO_INFORMADA', 'MEDIDAS_PENDENTES', 'MEDIDAS_ENVIADAS', 'EM_PRODUCAO', 'ENTREGUE'], item.situacaoBeca || 'NAO_INFORMADA')}
    ${select('statusCadastro', 'Status do cadastro', ['COMPLETO', 'INCOMPLETO', 'PENDENTE_REVISAO'], item.statusCadastro || 'INCOMPLETO')}
    ${textarea('observacaoInterna', 'Observação interna', item.observacaoInterna)}
    ${input('senha', 'Senha temporária', '', false, 'password')}
  `, async form => {
    await api.businessSalvar(item.id ? `/alunos/${item.id}` : '/alunos', formToObject(form), item.id ? 'PUT' : 'POST');
    toast('Aluno salvo.', 'success');
    await loadScreen('alunos', true);
  });
}

function openTipoDocumentoForm() {
  openEntityDialog('Tipo documental', `
    ${input('nome', 'Nome', '', true)}
    ${textarea('descricao', 'Descrição')}
    ${select('obrigatorio', 'Obrigatório', [['true', 'Sim'], ['false', 'Não']], 'true')}
    ${select('aplicavelMenorIdade', 'Aplicável a menor de idade', [['true', 'Sim'], ['false', 'Não']], 'true')}
    ${select('permiteMultiplosArquivos', 'Permite múltiplos arquivos', [['false', 'Não'], ['true', 'Sim']], 'false')}
    ${input('extensoesPermitidas', 'Extensões permitidas', 'pdf,jpg,jpeg,png')}
    ${input('tamanhoMaximoBytes', 'Tamanho máximo em bytes', '5242880', false, 'number')}
  `, async form => {
    const payload = formToObject(form);
    ['obrigatorio', 'aplicavelMenorIdade', 'permiteMultiplosArquivos'].forEach(key => payload[key] = payload[key] === 'true');
    payload.tamanhoMaximoBytes = Number(payload.tamanhoMaximoBytes || 5242880);
    await api.businessSalvar('/tipos-documentos', payload);
    toast('Tipo documental criado.', 'success');
    await loadScreen('documentos', true);
  });
}

function openUploadDocumento() {
  openEntityDialog('Enviar documento', `
    ${select('alunoId', 'Aluno', state.alunos.map(a => [a.id, `${a.nome} - ${a.turma}`]), '', true)}
    ${select('tipoDocumentoId', 'Tipo documental', state.tipos.map(t => [t.id, t.nome]), '', true)}
    <label class="full"><span>Arquivo</span><input name="arquivo" type="file" accept=".pdf,.jpg,.jpeg,.png" required></label>
    ${textarea('observacao', 'Observação')}
  `, async form => {
    const fd = new FormData();
    const alunoId = form.alunoId.value;
    const tipoId = form.tipoDocumentoId.value;
    fd.append('arquivo', form.arquivo.files[0]);
    fd.append('observacao', form.observacao.value || '');
    await api.businessUpload(`/alunos/${alunoId}/documentos/${tipoId}/upload`, fd);
    toast('Documento enviado para analise.', 'success');
    await loadScreen('documentos', true);
  });
}

function openUploadComprovante() {
  openEntityDialog('Enviar comprovante', `
    ${select('alunoId', 'Aluno', state.alunos.map(a => [a.id, `${a.nome} - ${a.turma}`]), '', true)}
    <label class="full"><span>Arquivo</span><input name="arquivo" type="file" accept=".pdf,.jpg,.jpeg,.png" required></label>
    ${input('descricao', 'Descrição', 'Comprovante de pagamento')}
  `, async form => {
    const fd = new FormData();
    fd.append('arquivo', form.arquivo.files[0]);
    fd.append('descricao', form.descricao.value || '');
    await api.businessUpload(`/alunos/${form.alunoId.value}/comprovantes/upload`, fd);
    toast('Comprovante enviado.', 'success');
    await loadScreen('comprovantes', true);
  });
}

async function openChecklist(alunoId) {
  const checklist = await api.businessBuscar(`/alunos/${alunoId}/documentos`);
  openEntityDialog('Checklist documental', checklist.map(item => `
    <div class="document-item full">
      <strong>${escapeHtml(item.tipoDocumento)} ${badge(item.status)}</strong>
      <span>${escapeHtml(item.descricao || '')}</span>
      ${item.documentoAtual?.justificativa ? `<span>${escapeHtml(item.documentoAtual.justificativa)}</span>` : ''}
    </div>
  `).join('') || emptyHtml('Nenhum requisito configurado.'), async () => {});
  document.querySelector('#entityForm [data-submit]')?.remove();
}

function openAnalysis(kind, id, status) {
  const form = document.getElementById('analysisForm');
  const title = `${status === 'APROVADO' ? 'Aprovar' : 'Reprovar'} ${kind}`;
  form.innerHTML = `
    <div class="modal__head"><h2 class="panel__title">${title}</h2></div>
    <div class="modal__body form-grid">
      <label><span>Justificativa ou comentário</span><textarea name="texto" ${status === 'REPROVADO' ? 'required' : ''}></textarea></label>
    </div>
    <div class="modal__footer">
      <button class="button" type="button" data-close-analysis>Cancelar</button>
      <button class="button ${status === 'APROVADO' ? 'button--primary' : 'button--danger'}" type="submit">${title}</button>
    </div>
  `;
  document.getElementById('analysisDialog').showModal();
  form.querySelector('[data-close-analysis]').addEventListener('click', () => document.getElementById('analysisDialog').close(), { once: true });
  form.onsubmit = async event => {
    event.preventDefault();
    const texto = form.texto.value.trim();
    try {
      if (kind === 'documento') {
        await api.businessSalvar(`/documentos/${id}/analisar`, { status, justificativa: texto, observacao: texto });
        await loadScreen('documentos', true);
      } else {
        await api.businessSalvar(`/comprovantes/${id}/analisar`, { status, comentario: texto });
        await loadScreen('comprovantes', true);
      }
      document.getElementById('analysisDialog').close();
      toast('Analise registrada.', 'success');
    } catch (error) {
      toast(error.message, 'error');
    }
  };
}

async function toggleInstituicao(id) {
  const item = state.instituicoes.find(i => i.id === id);
  const status = item?.status === 'ATIVO' ? 'INATIVO' : 'ATIVO';
  await api.businessPatch(`/instituicoes/${id}/status`, { status });
  toast('Status atualizado.', 'success');
  await loadReferenceData();
  await loadScreen('instituicoes', true);
}

async function previewImport(event) {
  event.preventDefault();
  const file = document.getElementById('importFile').files[0];
  const turmaId = valueOf('importTurma');
  if (!file || !turmaId) return toast('Selecione turma e arquivo.', 'error');
  const fd = new FormData();
  fd.append('arquivo', file);
  fd.append('turmaId', turmaId);
  const preview = await api.businessUpload('/importacoes/alunos/preview', fd);
  state.importFile = file;
  document.getElementById('importPreviewPanel').hidden = false;
  setHtml('importPreview', `
    <div class="metric-grid">
      ${metricHtml('Validos', preview.validos, 'linhas prontas', 'green')}
      ${metricHtml('Incompletos', preview.incompletos, 'avisos/erros', preview.incompletos ? 'amber' : 'green')}
      ${metricHtml('Duplicados', preview.duplicados, 'possiveis conflitos', preview.duplicados ? 'amber' : 'green')}
    </div>
    <div class="stack">
      ${(preview.issues || []).map(issue => `<div class="activity-item"><strong>Linha ${issue.linha} - ${escapeHtml(issue.coluna)}</strong><span>${escapeHtml(issue.mensagem)}</span></div>`).join('') || emptyHtml('Sem erros no preview.')}
    </div>
  `);
}

async function confirmImport() {
  if (!state.importFile) return toast('Gere a pre-visualizacao antes de confirmar.', 'error');
  const fd = new FormData();
  fd.append('arquivo', state.importFile);
  fd.append('turmaId', valueOf('importTurma'));
  fd.append('estrategia', valueOf('importStrategy'));
  const result = await api.businessUpload('/importacoes/alunos/confirmar', fd);
  toast(`Importacao concluida: ${result.importados} novos, ${result.atualizados} atualizados, ${result.ignorados} ignorados.`, 'success');
  await loadReferenceData();
  await loadScreen('alunos', true);
}

async function exportReport() {
  const tipo = valueOf('reportType') || 'alunos';
  const file = await api.businessDownload(`/relatorios/${tipo}/export.csv`, { turmaId: currentFilters().turmaId });
  const url = URL.createObjectURL(file.blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = file.filename;
  link.click();
  URL.revokeObjectURL(url);
}

function openEntityDialog(title, fieldsHtml, onSubmit) {
  const dialog = document.getElementById('formDialog');
  const form = document.getElementById('entityForm');
  form.innerHTML = `
    <div class="modal__head"><h2 class="panel__title">${escapeHtml(title)}</h2></div>
    <div class="modal__body form-columns">${fieldsHtml}</div>
    <div class="modal__footer">
      <button class="button" type="button" data-close-dialog>Cancelar</button>
      <button class="button button--primary" type="submit" data-submit>Salvar</button>
    </div>
  `;
  form.querySelector('[data-close-dialog]').addEventListener('click', () => dialog.close(), { once: true });
  form.onsubmit = async event => {
    event.preventDefault();
    try {
      await onSubmit(form);
      dialog.close();
    } catch (error) {
      toast(error.message, 'error');
    }
  };
  dialog.showModal();
}

function input(name, label, value = '', required = false, type = 'text') {
  return `<label><span>${escapeHtml(label)}</span><input name="${name}" type="${type}" value="${escapeHtml(value || '')}" ${required ? 'required' : ''}></label>`;
}

function textarea(name, label, value = '') {
  return `<label class="full"><span>${escapeHtml(label)}</span><textarea name="${name}">${escapeHtml(value || '')}</textarea></label>`;
}

function select(name, label, options, value = '', required = false) {
  const opts = options.map(option => {
    const pair = Array.isArray(option) ? option : [option, option];
    return `<option value="${escapeHtml(pair[0])}" ${String(pair[0]) === String(value) ? 'selected' : ''}>${escapeHtml(pair[1])}</option>`;
  }).join('');
  return `<label><span>${escapeHtml(label)}</span><select name="${name}" ${required ? 'required' : ''}>${opts}</select></label>`;
}

function formToObject(form) {
  const data = new FormData(form);
  const obj = {};
  data.forEach((value, key) => {
    if (value instanceof File) return;
    obj[key] = value === '' ? null : value;
  });
  ['instituicaoId', 'turmaId', 'responsavelId', 'tipoDocumentoId', 'tamanhoMaximoBytes'].forEach(key => {
    if (obj[key] != null) obj[key] = Number(obj[key]);
  });
  return obj;
}

function currentFilters() {
  return {
    instituicaoId: valueOf('globalInstituicaoFilter'),
    turmaId: valueOf('globalTurmaFilter')
  };
}

function fillSelect(id, items, valueKey, labelKey) {
  const selectEl = document.getElementById(id);
  if (!selectEl) return;
  const current = selectEl.value;
  selectEl.innerHTML = items.map(item => `<option value="${escapeHtml(item[valueKey])}">${escapeHtml(item[labelKey])}</option>`).join('');
  if (items.some(item => String(item[valueKey]) === current)) selectEl.value = current;
}

function setTable(id, rows, mapper, columns) {
  const body = document.getElementById(id);
  if (!body) return;
  body.innerHTML = rows.length ? rows.map(mapper).join('') : `<tr><td colspan="${columns}">${emptyHtml('Sem registros para exibir.')}</td></tr>`;
  enhanceResponsiveTables();
}

function setList(id, rows, mapper, emptyMessage) {
  const el = document.getElementById(id);
  if (!el) return;
  el.innerHTML = rows?.length ? rows.map(mapper).join('') : emptyHtml(emptyMessage);
}

function metricHtml(label, value, hint, tone) {
  return `<article class="metric-card" data-tone="${tone}">
    <div><span>${escapeHtml(label)}</span><strong>${escapeHtml(String(value))}</strong><small>${escapeHtml(hint)}</small></div>
    <div class="metric-card__icon" aria-hidden="true"><i class="ph ${metricIcon(label)}"></i></div>
  </article>`;
}

function emptyHtml(message) {
  return `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function badge(status = '') {
  const normalized = String(status || 'NAO_INFORMADO').toUpperCase();
  const tone = statusTone[normalized] || 'info';
  return `<span class="badge badge--${tone}">${escapeHtml(normalized)}</span>`;
}

function roleLabel(role) {
  return {
    ROLE_ADMIN_ORGANIZACAO: 'Admin organizacao',
    ROLE_COLABORADOR: 'Colaborador',
    ROLE_COMISSAO: 'Comissao',
    ROLE_ALUNO: 'Aluno'
  }[role] || role;
}

function handleError(error) {
  if ((error.message || '').includes('Sessao')) {
    window.location.href = './login.html';
    return;
  }
  if ((error.message || '').includes('Acesso negado')) {
    setHtml(`screen-${state.screen}`, `<div class="state state--permission">Sem permissao para esta area.</div>`);
    return;
  }
  toast(error.message || 'Erro inesperado.', 'error');
}

function toast(message, type = 'success') {
  const stack = document.getElementById('toastContainer');
  const item = document.createElement('div');
  item.className = `toast is-${type}`;
  item.textContent = message;
  stack.appendChild(item);
  window.setTimeout(() => item.remove(), 4200);
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function setHtml(id, html) {
  const el = document.getElementById(id);
  if (el) el.innerHTML = html;
}

function metricIcon(label = '') {
  const value = String(label).toLowerCase();
  if (value.includes('turma')) return 'ph-graduation-cap';
  if (value.includes('institui')) return 'ph-bank';
  if (value.includes('aluno')) return 'ph-student';
  if (value.includes('cadastro')) return 'ph-identification-card';
  if (value.includes('doc')) return 'ph-files';
  if (value.includes('comprovante')) return 'ph-receipt';
  if (value.includes('evento')) return 'ph-calendar-check';
  if (value.includes('pend')) return 'ph-warning-circle';
  return 'ph-sparkle';
}

function enhanceResponsiveTables() {
  document.querySelectorAll('table').forEach(table => {
    const headers = Array.from(table.querySelectorAll('thead th')).map(th => th.textContent.trim());
    table.querySelectorAll('tbody tr').forEach(row => {
      Array.from(row.children).forEach((cell, index) => {
        if (headers[index]) cell.setAttribute('data-label', headers[index]);
      });
    });
  });
}

function valueOf(id) {
  return document.getElementById(id)?.value || '';
}

function escapeHtml(value = '') {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function normalize(value = '') {
  return String(value).normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();
}

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleDateString('pt-BR');
}

function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

function formatBytes(value = 0) {
  const bytes = Number(value || 0);
  if (bytes >= 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  if (bytes >= 1024) return `${(bytes / 1024).toFixed(0)} KB`;
  return `${bytes} B`;
}
