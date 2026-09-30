$ErrorActionPreference = "Stop"
. "$PSScriptRoot\env.ps1"
& "$PSScriptRoot\..\mvnw.cmd" -B spring-boot:run
exit $LASTEXITCODE
