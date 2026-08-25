$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$site = Join-Path $repoRoot '_site'
$static = Join-Path $repoRoot 'src/main/resources/static'
$demo = Join-Path $repoRoot 'pages-demo'

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

Write-Host ''
Write-Host 'Demo estática preparada em:' -ForegroundColor Green
Write-Host "  $site"
Write-Host ''
Write-Host 'Agora execute:' -ForegroundColor Cyan
Write-Host '  python -m http.server 5500 --directory _site'
Write-Host ''
Write-Host 'Depois acesse:' -ForegroundColor Cyan
Write-Host '  http://localhost:5500/'
