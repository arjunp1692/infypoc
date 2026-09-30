$ErrorActionPreference = "Stop"

$adoptium = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like "jdk-21*" } |
    Select-Object -First 1

if (-not $adoptium) {
    throw "Temurin 21 was not found under C:\Program Files\Eclipse Adoptium"
}

$env:JAVA_HOME = $adoptium.FullName
$env:Path = "$($adoptium.FullName)\bin;" + $env:Path
