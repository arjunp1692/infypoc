$ErrorActionPreference = "Stop"
. "$PSScriptRoot\env.ps1"
& "$PSScriptRoot\..\mvnw.cmd" -B -DskipTests checkstyle:check
exit $LASTEXITCODE
