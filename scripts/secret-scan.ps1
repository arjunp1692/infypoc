$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$skipDirs = @('.git', 'target', 'data', '.mvn-tmp', '.idea')
$patterns = @(
    '-----BEGIN [A-Z ]*PRIVATE KEY-----',
    'AKIA[0-9A-Z]{16}',
    'sk-[A-Za-z0-9]{20,}',
    '(?i)(api[_-]?key|secret|token|password)\s*[:=]\s*[''"][A-Za-z0-9+/=_\-]{16,}'
)

$findings = @()
$files = Get-ChildItem -Path $root -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
    $relative = $_.FullName.Substring($root.Length)
    foreach ($dir in $skipDirs) {
        if ($relative -match "[\\/]$([regex]::Escape($dir))[\\/]") { return $false }
    }
    $_.Extension -in @('.java', '.yml', '.yaml', '.xml', '.md', '.json', '.properties', '.ps1', '.txt', '.example') -or
        $_.Name -in @('Dockerfile', '.env.example', 'mvnw')
}

foreach ($file in $files) {
    $text = Get-Content -Raw -LiteralPath $file.FullName -ErrorAction SilentlyContinue
    if (-not $text) { continue }
    foreach ($pattern in $patterns) {
        if ($text -match $pattern) {
            $findings += "$($file.FullName) matched $pattern"
        }
    }
}

if ($findings.Count -gt 0) {
    $findings | ForEach-Object { Write-Output $_ }
    exit 1
}

Write-Output "secret scan clean"
exit 0
