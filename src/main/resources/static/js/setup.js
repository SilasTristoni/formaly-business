import { showToast } from './components/toast.js';

const form = document.getElementById('setupForm');
const button = document.getElementById('setupButton');
const statusEl = document.getElementById('setupStatus');

const organizationName = document.getElementById('organizationName');
const tradeName = document.getElementById('tradeName');
const adminName = document.getElementById('adminName');
const adminEmail = document.getElementById('adminEmail');
const adminPassword = document.getElementById('adminPassword');
const adminPasswordConfirm = document.getElementById('adminPasswordConfirm');

const defaultButtonMarkup = button?.innerHTML || '';

function setLoading(loading) {
    if (!button) return;
    button.disabled = loading;
    button.innerHTML = loading
        ? '<i class="ph ph-spinner-gap animate-spin"></i> Criando ambiente...'
        : defaultButtonMarkup;
}

async function readJson(response) {
    const text = await response.text();
    if (!text) return null;
    try {
        return JSON.parse(text);
    } catch {
        return null;
    }
}

async function checkStatus() {
    try {
        const response = await fetch('./api/setup/status', { headers: { Accept: 'application/json' } });
        if (!response.ok) throw new Error('Falha ao verificar configuração.');
        const data = await response.json();

        if (!data.required) {
            window.location.replace('./login.html');
            return false;
        }

        if (statusEl) statusEl.textContent = 'Banco pronto para primeira configuração.';
        return true;
    } catch (error) {
        if (statusEl) statusEl.textContent = 'Não foi possível verificar o banco.';
        showToast('Não foi possível verificar o estado inicial do ambiente.', 'error');
        return false;
    }
}

form?.addEventListener('submit', async (event) => {
    event.preventDefault();

    if (adminPassword.value !== adminPasswordConfirm.value) {
        showToast('As senhas não coincidem.', 'error');
        adminPasswordConfirm.focus();
        return;
    }

    setLoading(true);

    try {
        const response = await fetch('./api/setup/initialize', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            },
            body: JSON.stringify({
                organizationName: organizationName.value.trim(),
                tradeName: tradeName.value.trim(),
                adminName: adminName.value.trim(),
                adminEmail: adminEmail.value.trim(),
                adminPassword: adminPassword.value
            })
        });

        const data = await readJson(response);
        if (!response.ok) {
            const message = data?.detail || data?.message || 'Não foi possível criar o ambiente.';
            throw new Error(message);
        }

        showToast('Ambiente criado com sucesso. Faça login com o administrador cadastrado.');
        window.setTimeout(() => {
            const login = encodeURIComponent(data?.adminLogin || adminEmail.value.trim());
            window.location.href = `./login.html?setup=done&login=${login}`;
        }, 650);
    } catch (error) {
        showToast(error.message || 'Erro ao criar o ambiente.', 'error');
        setLoading(false);
    }
});

checkStatus().catch(console.error);
