param(
  [string]$BaseUrl = "http://localhost:18080",
  [string]$OutputDir = "target/visual/smoke"
)

$ErrorActionPreference = "Stop"

$repo = Resolve-Path (Join-Path $PSScriptRoot "..")
$out = Join-Path $repo $OutputDir
$pwDir = Join-Path $env:TEMP "formaly-pw-run"

New-Item -ItemType Directory -Force -Path $out | Out-Null
New-Item -ItemType Directory -Force -Path $pwDir | Out-Null

Push-Location $pwDir
try {
  if (-not (Test-Path (Join-Path $pwDir "package.json"))) {
    npm init -y | Out-Null
  }

  if (-not (Test-Path (Join-Path $pwDir "node_modules\playwright"))) {
    npm install playwright@1.55.0 | Out-Null
  }

  npx playwright install chromium | Out-Null

  $nodeScript = Join-Path $pwDir "formaly-visual-smoke.mjs"
  @'
import { chromium } from "playwright";
import fs from "node:fs";
import path from "node:path";

const base = process.env.FORMALY_BASE_URL;
const out = process.env.FORMALY_VISUAL_OUT;
const prototypeText = "plataforma oficial";

const admin = ["admin.demo@formaly.local", "DemoAdmin2026!"];
const collaborator = ["colaborador.demo@formaly.local", "DemoColaborador2026!"];
const committee = ["comissao.demo@formaly.local", "DemoComissao2026!"];
const student = ["demo001", "DemoAluno2026!"];

const viewports = [
  ["1920x1080", 1920, 1080],
  ["1440x900", 1440, 900],
  ["1280x800", 1280, 800],
  ["1024x768", 1024, 768],
  ["768x1024", 768, 1024],
  ["430x932", 430, 932],
  ["390x844", 390, 844],
  ["360x800", 360, 800]
];

function ensureDir(file) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
}

async function login(page, user, password) {
  await page.goto(`${base}/login.html`, { waitUntil: "networkidle" });
  await page.fill("#loginEmail", user);
  await page.fill("#loginSenha", password);
  await Promise.all([
    page.waitForURL(/index\.html|aluno\.html/, { timeout: 15000 }),
    page.click("#btnLogin")
  ]);
  await page.waitForLoadState("networkidle");
}

async function screenshot(page, name, fullPage = true) {
  const file = path.join(out, `${name}.png`);
  ensureDir(file);
  await page.screenshot({ path: file, fullPage });
}

async function assertNoHorizontalOverflow(page, label) {
  const overflow = await page.evaluate(() => {
    const doc = document.documentElement;
    const body = document.body;
    return Math.max(doc.scrollWidth, body.scrollWidth) - window.innerWidth;
  });
  if (overflow > 2) {
    throw new Error(`${label} has horizontal overflow of ${overflow}px`);
  }
}

async function assertPrototypeNotice(page, label) {
  const present = await page.evaluate((text) => document.body.innerText.includes(text), prototypeText);
  if (!present) {
    throw new Error(`${label} does not show the prototype notice`);
  }
}

async function adminContext(browser, width, height) {
  const context = await browser.newContext({
    viewport: { width, height },
    isMobile: width < 768,
    deviceScaleFactor: 1
  });
  const page = await context.newPage();
  await login(page, admin[0], admin[1]);
  return { context, page };
}

const browser = await chromium.launch();

try {
  for (const [name, width, height] of viewports) {
    const context = await browser.newContext({
      viewport: { width, height },
      isMobile: width < 768,
      deviceScaleFactor: 1
    });
    const page = await context.newPage();
    await page.goto(`${base}/login.html`, { waitUntil: "networkidle" });
    await assertPrototypeNotice(page, `login ${name}`);
    await assertNoHorizontalOverflow(page, `login ${name}`);
    if (name === "1440x900") await screenshot(page, "login-desktop-1440x900");
    if (name === "390x844") await screenshot(page, "login-mobile-390x844");
    await context.close();

    const adminRun = await adminContext(browser, width, height);
    await assertPrototypeNotice(adminRun.page, `dashboard ${name}`);
    await assertNoHorizontalOverflow(adminRun.page, `dashboard ${name}`);
    await screenshot(adminRun.page, `responsive/dashboard-${name}`, name === "1440x900" || name === "390x844");
    if (name === "1440x900") await screenshot(adminRun.page, "dashboard-desktop-1440x900");
    if (name === "390x844") await screenshot(adminRun.page, "dashboard-mobile-390x844");
    await adminRun.context.close();
  }

  let run = await adminContext(browser, 1440, 900);
  await run.page.click("[data-screen=\"turmas\"]");
  await run.page.waitForTimeout(700);
  await assertNoHorizontalOverflow(run.page, "turmas desktop");
  await screenshot(run.page, "turmas-desktop-1440x900", false);

  await run.page.click("[data-screen=\"alunos\"]");
  await run.page.waitForTimeout(700);
  await run.page.locator("[data-action=\"checklist-aluno\"]").first().click();
  await run.page.waitForTimeout(700);
  await assertNoHorizontalOverflow(run.page, "aluno detalhe desktop");
  await screenshot(run.page, "aluno-detalhe-desktop-1440x900", false);
  await run.page.locator("#formDialog").evaluate((dialog) => dialog.close());

  await run.page.click("[data-screen=\"documentos\"]");
  await run.page.waitForTimeout(700);
  await assertNoHorizontalOverflow(run.page, "documentos desktop");
  await screenshot(run.page, "documentos-desktop-1440x900", false);
  await run.context.close();

  for (const [role, credentials, expected] of [
    ["admin", admin, /index\.html/],
    ["colaborador", collaborator, /index\.html/],
    ["comissao", committee, /index\.html/],
    ["aluno", student, /aluno\.html/]
  ]) {
    const context = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true });
    const page = await context.newPage();
    await login(page, credentials[0], credentials[1]);
    if (!expected.test(page.url())) {
      throw new Error(`profile ${role} redirected to unexpected URL ${page.url()}`);
    }
    await assertPrototypeNotice(page, `profile ${role}`);
    await assertNoHorizontalOverflow(page, `profile ${role}`);
    await screenshot(page, `profiles/${role}-mobile-390x844`);
    await context.close();
  }

  const studentContext = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true });
  const studentPage = await studentContext.newPage();
  await login(studentPage, student[0], student[1]);
  await assertNoHorizontalOverflow(studentPage, "portal aluno mobile");
  await screenshot(studentPage, "portal-aluno-mobile-390x844");
  await studentContext.close();
} finally {
  await browser.close();
}
'@ | Set-Content -Encoding UTF8 $nodeScript

  $env:FORMALY_BASE_URL = $BaseUrl.TrimEnd("/")
  $env:FORMALY_VISUAL_OUT = $out
  node $nodeScript
  if ($LASTEXITCODE -ne 0) {
    throw "Visual smoke failed with exit code $LASTEXITCODE"
  }
}
finally {
  Pop-Location
}

Get-ChildItem $out -Recurse -File | Sort-Object FullName | Select-Object FullName, Length
