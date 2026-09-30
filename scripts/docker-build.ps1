$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$dockerBin = Join-Path $env:ProgramFiles "Docker\Docker\resources\bin"
if ((Test-Path (Join-Path $dockerBin "docker.exe")) -and -not (Get-Command docker -ErrorAction SilentlyContinue)) {
    $env:Path = "$dockerBin;" + $env:Path
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker is not on PATH. Install Docker Desktop, start it, then rerun scripts/docker-build.ps1."
}

Write-Output "== verify =="
& "$PSScriptRoot\verify.ps1"
if ($LASTEXITCODE -ne 0) {
    throw "verify failed; image build was not started"
}

$tag = "sentinelflow:0.1.0"
$archive = Join-Path $root "artifacts\sentinelflow-0.1.0.tar"
New-Item -ItemType Directory -Force -Path (Join-Path $root "artifacts") | Out-Null

Write-Output "== docker build =="
& docker build -t $tag $root
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "== docker save =="
if (Test-Path $archive) { Remove-Item $archive -Force }
& docker save -o $archive $tag
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$item = Get-Item $archive
Write-Output "artifact $($item.FullName) ($($item.Length) bytes)"
exit 0
