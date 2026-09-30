$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$dockerBin = Join-Path $env:ProgramFiles "Docker\Docker\resources\bin"
if ((Test-Path (Join-Path $dockerBin "docker.exe")) -and -not (Get-Command docker -ErrorAction SilentlyContinue)) {
    $env:Path = "$dockerBin;" + $env:Path
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker is not on PATH. Install Docker Desktop, start it, then rerun scripts/docker-deploy.ps1."
}

$tag = "sentinelflow:0.1.0"
$archive = Join-Path $root "artifacts\sentinelflow-0.1.0.tar"
if (-not (Test-Path $archive)) {
    throw "Missing $archive. Run scripts/docker-build.ps1 first."
}

Write-Output "== docker load =="
& docker load -i $archive
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "== compose up =="
& docker compose up -d sentinelflow
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "sentinelflow is deployed from $tag on port 8080"
exit 0
