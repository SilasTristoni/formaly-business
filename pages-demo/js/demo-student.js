const STORAGE_KEY='formaly.timbe.demo.signature.v1';
const state={mode:'draw',drawing:false,hasStroke:false,lastPoint:null};
const $=id=>document.getElementById(id);

document.addEventListener('DOMContentLoaded',()=>{
  bindDialogs();
  bindSignature();
  restoreSignature();
});

function bindDialogs(){
  $('previewContractBtn')?.addEventListener('click',()=>openDialog('contractDialog'));
  $('signContractBtn')?.addEventListener('click',()=>openDialog('contractDialog'));
  $('startSignatureBtn')?.addEventListener('click',()=>{closeDialog('contractDialog');openDialog('signatureDialog');resizeCanvas();});
  $('certificateBtn')?.addEventListener('click',showCertificate);
  document.querySelectorAll('[data-close-dialog]').forEach(btn=>btn.addEventListener('click',()=>closeDialog(btn.dataset.closeDialog)));
}

function bindSignature(){
  $('drawTab')?.addEventListener('click',()=>setMode('draw'));
  $('typeTab')?.addEventListener('click',()=>setMode('type'));
  $('clearSignatureBtn')?.addEventListener('click',clearCanvas);
  $('signatureForm')?.addEventListener('submit',submitSignature);
  const canvas=$('signatureCanvas');
  if(!canvas)return;
  canvas.addEventListener('pointerdown',startDraw);
  canvas.addEventListener('pointermove',draw);
  canvas.addEventListener('pointerup',endDraw);
  canvas.addEventListener('pointercancel',endDraw);
  window.addEventListener('resize',resizeCanvas);
}

function setMode(mode){
  state.mode=mode;
  $('drawSignaturePanel').hidden=mode!=='draw';
  $('typeSignaturePanel').hidden=mode!=='type';
  $('drawTab').classList.toggle('is-active',mode==='draw');
  $('typeTab').classList.toggle('is-active',mode==='type');
}

function resizeCanvas(){
  const canvas=$('signatureCanvas');
  if(!canvas)return;
  const rect=canvas.getBoundingClientRect();
  const ratio=Math.max(window.devicePixelRatio||1,1);
  if(rect.width<1||rect.height<1)return;
  const saved=state.hasStroke?canvas.toDataURL('image/png'):null;
  canvas.width=Math.round(rect.width*ratio);
  canvas.height=Math.round(rect.height*ratio);
  const ctx=canvas.getContext('2d');
  ctx.scale(ratio,ratio);
  ctx.lineWidth=2.2;ctx.lineCap='round';ctx.lineJoin='round';ctx.strokeStyle='#111';
  if(saved){const img=new Image();img.onload=()=>ctx.drawImage(img,0,0,rect.width,rect.height);img.src=saved;}
}

function pointFromEvent(event){
  const rect=$('signatureCanvas').getBoundingClientRect();
  return{x:event.clientX-rect.left,y:event.clientY-rect.top};
}
function startDraw(event){
  event.preventDefault();
  const canvas=$('signatureCanvas');
  canvas.setPointerCapture?.(event.pointerId);
  state.drawing=true;state.lastPoint=pointFromEvent(event);
}
function draw(event){
  if(!state.drawing)return;
  event.preventDefault();
  const next=pointFromEvent(event);const ctx=$('signatureCanvas').getContext('2d');
  ctx.beginPath();ctx.moveTo(state.lastPoint.x,state.lastPoint.y);ctx.lineTo(next.x,next.y);ctx.stroke();
  state.lastPoint=next;state.hasStroke=true;
}
function endDraw(event){
  if(!state.drawing)return;
  state.drawing=false;state.lastPoint=null;
  try{$('signatureCanvas').releasePointerCapture?.(event.pointerId);}catch(_){ }
}
function clearCanvas(){
  const canvas=$('signatureCanvas');const ctx=canvas.getContext('2d');
  ctx.clearRect(0,0,canvas.width,canvas.height);state.hasStroke=false;
}

function submitSignature(event){
  event.preventDefault();
  if(!$('readConsent').checked)return toast('Confirme a leitura do documento.','error');
  const signerName=$('signerName').value.trim();
  if(signerName.length<3)return toast('Informe o nome completo.','error');
  let signatureValue='';
  if(state.mode==='draw'){
    if(!state.hasStroke)return toast('Desenhe sua assinatura no campo.','error');
    signatureValue=$('signatureCanvas').toDataURL('image/png');
  }else{
    signatureValue=$('typedSignature').value.trim();
    if(signatureValue.length<3)return toast('Digite sua assinatura.','error');
  }
  const signedAt=new Date().toISOString();
  const code='TMB-'+Math.random().toString(36).slice(2,8).toUpperCase()+'-'+Date.now().toString().slice(-5);
  const payload={signed:true,signerName,signedAt,code,mode:state.mode,signatureValue,document:'Contrato de prestação de serviços',version:'1.0'};
  localStorage.setItem(STORAGE_KEY,JSON.stringify(payload));
  closeDialog('signatureDialog');restoreSignature();toast('Documento assinado no ambiente demonstrativo.','success');showCertificate();
}

function restoreSignature(){
  const data=getSignature();
  const badge=$('contractBadge'),signBtn=$('signContractBtn'),certBtn=$('certificateBtn'),summary=$('signatureSummary');
  if(!data?.signed){
    badge.className='badge badge--warning';badge.textContent='PENDENTE DE ASSINATURA';
    signBtn.hidden=false;certBtn.hidden=true;summary.textContent='';$('pendingMetric').textContent='1';return;
  }
  badge.className='badge badge--success';badge.textContent='ASSINADO';
  signBtn.hidden=true;certBtn.hidden=false;$('pendingMetric').textContent='0';
  summary.textContent=`Assinado por ${data.signerName} em ${formatDateTime(data.signedAt)} • código ${data.code}`;
}

function showCertificate(){
  const data=getSignature();if(!data?.signed)return;
  const signature=data.mode==='draw'?`<img src="${data.signatureValue}" alt="Assinatura demonstrativa de ${escapeHtml(data.signerName)}">`:`<strong>${escapeHtml(data.signatureValue)}</strong>`;
  $('certificateContent').innerHTML=`<div class="certificate-box"><div class="certificate-box__header"><p class="eyebrow">Comprovante demonstrativo</p><h3>Assinatura registrada no protótipo</h3><p class="muted">Sem validade jurídica. Registro armazenado apenas neste navegador.</p></div><div class="certificate-grid"><div class="certificate-field"><span>Documento</span><strong>${escapeHtml(data.document)}</strong></div><div class="certificate-field"><span>Versão</span><strong>${escapeHtml(data.version)}</strong></div><div class="certificate-field"><span>Assinante</span><strong>${escapeHtml(data.signerName)}</strong></div><div class="certificate-field"><span>Data e hora</span><strong>${formatDateTime(data.signedAt)}</strong></div><div class="certificate-field"><span>Código demonstrativo</span><strong>${escapeHtml(data.code)}</strong></div><div class="certificate-field"><span>Status</span><strong>ASSINADO</strong></div></div><div class="certificate-signature">${signature}</div></div>`;
  openDialog('certificateDialog');
}

function getSignature(){try{return JSON.parse(localStorage.getItem(STORAGE_KEY)||'null')}catch(_){return null}}
function openDialog(id){const d=$(id);if(d&&!d.open)d.showModal()}
function closeDialog(id){const d=$(id);if(d?.open)d.close()}
function formatDateTime(value){return new Date(value).toLocaleString('pt-BR',{dateStyle:'short',timeStyle:'short'})}
function escapeHtml(value=''){return String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#39;')}
function toast(message,type='success'){
  const stack=$('toastContainer');const item=document.createElement('div');item.className=`toast is-${type}`;item.textContent=message;stack.appendChild(item);setTimeout(()=>item.remove(),3600);
}
