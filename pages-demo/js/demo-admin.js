const STORAGE_KEY='formaly.timbe.demo.signature.v1';
const $=id=>document.getElementById(id);

document.addEventListener('DOMContentLoaded',()=>{
  bindNavigation();
  bindCertificate();
  refreshSignatureState();
});

function bindNavigation(){
  document.querySelectorAll('.nav-button').forEach(button=>button.addEventListener('click',()=>{
    const screen=button.dataset.screen;
    document.querySelectorAll('.nav-button').forEach(item=>item.classList.toggle('is-active',item===button));
    document.querySelectorAll('.screen').forEach(item=>item.classList.toggle('is-active',item.id===`screen-${screen}`));
    const titles={dashboard:'Dashboard operacional',documentos:'Documentos',alunos:'Formandos'};
    if($('pageTitle'))$('pageTitle').textContent=titles[screen]||'Formaly Business';
  }));
}

function bindCertificate(){
  $('adminCertificateBtn')?.addEventListener('click',showCertificate);
  $('closeCertificateBtn')?.addEventListener('click',()=>closeCertificate());
  $('finishCertificateBtn')?.addEventListener('click',()=>closeCertificate());
  window.addEventListener('storage',refreshSignatureState);
  window.addEventListener('focus',refreshSignatureState);
}

function refreshSignatureState(){
  const data=getSignature();
  const badge=$('adminContractBadge');
  const btn=$('adminCertificateBtn');
  const summary=$('adminSignatureSummary');
  const cell=$('studentContractCell');
  if(!data?.signed){
    if(badge){badge.className='badge badge--warning';badge.textContent='PENDENTE DE ASSINATURA';}
    if(btn)btn.hidden=true;
    if(summary)summary.textContent='Aguardando assinatura da formanda.';
    if(cell)cell.innerHTML='<span class="badge badge--warning">PENDENTE</span>';
    if($('dashboardPending'))$('dashboardPending').textContent='37';
    if($('dashboardSigned'))$('dashboardSigned').textContent='312';
    return;
  }
  if(badge){badge.className='badge badge--success';badge.textContent='ASSINADO';}
  if(btn)btn.hidden=false;
  if(summary)summary.textContent=`Assinado por ${data.signerName} em ${formatDateTime(data.signedAt)} • ${data.code}`;
  if(cell)cell.innerHTML='<span class="badge badge--success">ASSINADO</span>';
  if($('dashboardPending'))$('dashboardPending').textContent='36';
  if($('dashboardSigned'))$('dashboardSigned').textContent='313';
}

function showCertificate(){
  const data=getSignature();if(!data?.signed)return;
  const signature=data.mode==='draw'?`<img src="${data.signatureValue}" alt="Assinatura demonstrativa de ${escapeHtml(data.signerName)}">`:`<strong>${escapeHtml(data.signatureValue)}</strong>`;
  $('certificateContent').innerHTML=`<div class="certificate-box"><div class="certificate-box__header"><p class="eyebrow">Comprovante demonstrativo</p><h3>Assinatura registrada no protótipo</h3><p class="muted">Registro salvo somente neste navegador.</p></div><div class="certificate-grid"><div class="certificate-field"><span>Documento</span><strong>${escapeHtml(data.document)}</strong></div><div class="certificate-field"><span>Versão</span><strong>${escapeHtml(data.version)}</strong></div><div class="certificate-field"><span>Assinante</span><strong>${escapeHtml(data.signerName)}</strong></div><div class="certificate-field"><span>Data e hora</span><strong>${formatDateTime(data.signedAt)}</strong></div><div class="certificate-field"><span>Código</span><strong>${escapeHtml(data.code)}</strong></div><div class="certificate-field"><span>Status</span><strong>ASSINADO</strong></div></div><div class="certificate-signature">${signature}</div></div>`;
  $('certificateDialog').showModal();
}
function closeCertificate(){if($('certificateDialog')?.open)$('certificateDialog').close()}
function getSignature(){try{return JSON.parse(localStorage.getItem(STORAGE_KEY)||'null')}catch(_){return null}}
function formatDateTime(value){return new Date(value).toLocaleString('pt-BR',{dateStyle:'short',timeStyle:'short'})}
function escapeHtml(value=''){return String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#39;')}
