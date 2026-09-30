$ErrorActionPreference = "Stop"

function Invoke-Gate([string]$Name, [string]$Script) {
    Write-Output "== $Name =="
    & $Script
    if ($LASTEXITCODE -ne 0) {
        Write-Output "$Name failed with exit $LASTEXITCODE"
        exit $LASTEXITCODE
    }
}

Invoke-Gate "build" "$PSScriptRoot\build.ps1"
Invoke-Gate "test" "$PSScriptRoot\test.ps1"
Invoke-Gate "lint" "$PSScriptRoot\lint.ps1"
Invoke-Gate "secret-scan" "$PSScriptRoot\secret-scan.ps1"
Write-Output "verify passed"
exit 0
