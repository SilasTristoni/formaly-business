import { api } from './services/api.js';
import { auth } from './services/auth.js';

let me;
let profile;
let checklist = [];
let painel;

document.addEventListener('DOMContentLoaded', init);

async function init() {
  document.getElementById('logoutBtn')?.addEventListener('click', () => {
    auth.logout();
    window.location.href = './login.html';
  });
  document.getElementById('studentReceiptForm')?.addEventListener('submit', uploadReceipt);

  try {
    me = await api.me();
    if (me.perfil !== 'ROLE_ALUNO') {
      window.location.href = './index.html';
      return;
    }
    await loadStudent();
  } catch (error) {
    if ((error.message || '').includes('Sessao')) window.location.href = './login.html';
    else showState(error.message || 'Nao foi possivel carregar seu painel.', true);
  }
}

async function loadStudent() {
  const [student, studentChecklist, studentPanel] = await Promise.all([
    api.businessBuscar(`/alunos/${me.alunoId}`),
    api.businessBuscar(`/alunos/${me.alunoId}/documentos`),
    api.alunoPainel().catch(() => null)
  ]);
  profile = student;
  checklist = studentChecklist;
  painel = studentPanel;
  render();
}

function render() {
  document.getElementById('studentState').hidden = true;
  document.getElementById('studentContent').hidden = false;
  setText('studentName', profile.nome);
  setHtml('studentProfile', `
    <div class="student-card"><strong>Turma</strong><span>${escapeHtml(profile.turma)} | ${escapeHtml(profile.instituicao)}</span></div>
    <div class="student-card"><strong>Contato</strong><span>${escapeHtml(profile.email || profile.whatsapp || profile.telefone || '-')}</span></div>
    <div class="student-card"><strong>Status</strong><span>${badge(profile.statusCadastro)} ${badge(profile.statusDocumental)} ${badge(profile.situacaoBeca)}</span></div>
  `);
  const pendentes = checklist.filter(item => !['APROVADO'].includes(item.status)).length;
  const aprovados = checklist.filter(item => item.status === 'APROVADO').length;
  setHtml('studentMetrics', `
    ${metric('Documentos aprovados', aprovados, 'checklist')}
    ${metric('Pendências', pendentes, 'acompanhar')}
    ${metric('Eventos', painel?.eventos?.length || 0, 'agenda')}
    ${metric('Votações abertas', painel?.resumo?.votacoesAbertas || 0, 'turma')}
  `);
  renderChecklist();
  renderEvents();
}

function renderChecklist() {
  setHtml('studentChecklist', checklist.length ? checklist.map(item => `
    <div class="document-item">
      <strong>${escapeHtml(item.tipoDocumento)} ${badge(item.status)}</strong>
      <span>${escapeHtml(item.descricao || '')}</span>
      ${item.documentoAtual?.justificativa ? `<span>${escapeHtml(item.documentoAtual.justificativa)}</span>` : ''}
      <button class="button button--primary" data-upload-doc="${item.tipoDocumentoId}"><i class="ph ph-upload"></i> Enviar nova versão</button>
    </div>
  `).join('') : empty('Nenhum requisito documental configurado.'));
  document.querySelectorAll('[data-upload-doc]').forEach(button => {
    button.addEventListener('click', () => openDocumentUpload(button.dataset.uploadDoc));
  });
}

function renderEvents() {
  const eventos = painel?.eventos || [];
  setHtml('studentEvents', eventos.length ? eventos.slice(0, 5).map(evento => `
    <div class="activity-item">
      <strong>${escapeHtml(evento.nome)}</strong>
      <span>${formatDate(evento.data)} | ${escapeHtml(evento.local || 'Local a definir')} | ${escapeHtml(evento.presencaStatus || 'pendente')}</span>
    </div>
  `).join('') : empty('Nenhum evento futuro no momento.'));
}

function openDocumentUpload(tipoDocumentoId) {
  const dialog = document.getElementById('uploadDialog');
  const form = document.getElementById('studentUploadForm');
  form.innerHTML = `
    <div class="modal__head"><h2 class="panel__title">Enviar documento</h2></div>
    <div class="modal__body form-grid">
      <label><span>Arquivo</span><input name="arquivo" type="file" accept=".pdf,.jpg,.jpeg,.png" required></label>
      <label><span>Observação</span><textarea name="observacao"></textarea></label>
    </div>
    <div class="modal__footer">
      <button class="button" type="button" data-close>Cancelar</button>
      <button class="button button--primary" type="submit">Enviar</button>
    </div>
  `;
  form.querySelector('[data-close]').addEventListener('click', () => dialog.close(), { once: true });
  form.onsubmit = async event => {
    event.preventDefault();
    const fd = new FormData();
    fd.append('arquivo', form.arquivo.files[0]);
    fd.append('observacao', form.observacao.value || '');
    try {
      await api.businessUpload(`/alunos/${me.alunoId}/documentos/${tipoDocumentoId}/upload`, fd);
      toast('Documento enviado para analise.', 'success');
      dialog.close();
      await loadStudent();
    } catch (error) {
      toast(error.message, 'error');
    }
  };
  dialog.showModal();
}

async function uploadReceipt(event) {
  event.preventDefault();
  const file = document.getElementById('studentReceiptFile').files[0];
  if (!file) return toast('Selecione um arquivo.', 'error');
  const fd = new FormData();
  fd.append('arquivo', file);
  fd.append('descricao', document.getElementById('studentReceiptDescription').value || 'Comprovante');
  try {
    await api.businessUpload(`/alunos/${me.alunoId}/comprovantes/upload`, fd);
    document.getElementById('studentReceiptFile').value = '';
    toast('Comprovante enviado para analise.', 'success');
  } catch (error) {
    toast(error.message, 'error');
  }
}

function showState(message, error = false) {
  const state = document.getElementById('studentState');
  state.hidden = false;
  state.classList.toggle('state--error', error);
  state.textContent = message;
}

function metric(label, value, hint) {
  return `<article class="metric-card"><span>${escapeHtml(label)}</span><strong>${escapeHtml(String(value))}</strong><small>${escapeHtml(hint)}</small></article>`;
}

function badge(status = '') {
  const value = String(status || 'PENDENTE').toUpperCase();
  const tone = value.includes('APROVADO') || value.includes('COMPLETO') ? 'success' : value.includes('REPROVADO') ? 'danger' : value.includes('ANALISE') ? 'info' : 'warning';
  return `<span class="badge badge--${tone}">${escapeHtml(value)}</span>`;
}

function empty(message) {
  return `<div class="empty-state">${escapeHtml(message)}</div>`;
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
  if (el) el.textContent = value || '';
}

function setHtml(id, html) {
  const el = document.getElementById(id);
  if (el) el.innerHTML = html;
}

function escapeHtml(value = '') {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function formatDate(value) {
  if (!value) return '-';
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleDateString('pt-BR');
}
