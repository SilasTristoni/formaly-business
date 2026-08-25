$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$site = Join-Path $repoRoot '_site'
$static = Join-Path $repoRoot 'src/main/resources/static'
$demo = Join-Path $repoRoot 'pages-demo'

if (-not (Test-Path $static)) {
    throw "Pasta de arquivos estáticos não encontrada: $static"
}
if (-not (Test-Path $demo)) {
    throw "Pasta da demo não encontrada: $demo"
}

if (Test-Path $site) {
    Remove-Item $site -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $site | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $site 'css') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $site 'js') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $site 'assets') | Out-Null

Copy-Item (Join-Path $static 'css\*') (Join-Path $site 'css') -Recurse -Force
Copy-Item (Join-Path $static 'assets\*') (Join-Path $site 'assets') -Recurse -Force
Copy-Item (Join-Path $demo 'index.html') (Join-Path $site 'index.html') -Force
Copy-Item (Join-Path $demo 'aluno.html') (Join-Path $site 'aluno.html') -Force
Copy-Item (Join-Path $demo 'js\*') (Join-Path $site 'js') -Recurse -Force
Copy-Item (Join-Path $demo 'css\demo-signature.css') (Join-Path $site 'css\demo-signature.css') -Force
New-Item -ItemType File -Force -Path (Join-Path $site '.nojekyll') | Out-Null

$index = Join-Path $site 'index.html'
if (-not (Test-Path $index)) {
    throw "Build inválido: index.html não foi gerado em $site"
}

Write-Host ''
Write-Host 'Demo estática preparada com sucesso em:' -ForegroundColor Green
Write-Host "  $site"
Write-Host ''
Write-Host 'Iniciando servidor local automaticamente...' -ForegroundColor Cyan
Write-Host 'Acesse: http://localhost:5500/' -ForegroundColor Yellow
Write-Host 'Para encerrar o servidor, pressione Ctrl+C.' -ForegroundColor DarkGray
Write-Host ''

Set-Location $site
python -m http.server 5500
